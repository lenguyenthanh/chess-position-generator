import cats.syntax.all.*
import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.*
import fs2.io.file.Files
import fs2.data.csv.*

import chess.variant.*

import CLI.Args
import Domain.Position
import PositionGenerator.generate

object Main
    extends CommandIOApp(
      name = "chess-gen",
      header = "Generate random chess positions",
      version = "0.0.1"
    ):

  override def main: Opts[IO[ExitCode]] = CLI.parse
    .map(execute(_).as(ExitCode.Success))

  private def execute(args: Args): IO[Unit] =
    args match
      case Args.Gen(variant, moves, positions) => gen(variant, moves, positions)
      case Args.Perft(file, depth)             => ???

  private def gen(variant: Option[Variant], moves: Int, total: Int): IO[Unit] =
    val positions = variant match
      case None    => Domain.supportedVariants.flatMap(generate(_, moves, total))
      case Some(v) => generate(v, moves, total)
    write(positions.map(_.position))

  private def write(positions: List[Position]): IO[Unit] =
    Stream
      .emits(positions)
      .map(_.csv)
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(Files[IO].writeAll(io.file.Path("positions.csv")))
      .compile
      .drain
