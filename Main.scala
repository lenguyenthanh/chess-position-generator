import cats.syntax.all.*
import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.*
import fs2.io.file.Files
import fs2.data.csv.*

import chess.variant.*

import CLI.Args
import Domain.{ Position, PositionGenConfig }
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
      case Args.Gen(variant, config, output) => gen(variant, config, output)
      case Args.Perft(depth)                 => perft(depth)

  private def gen(variant: Option[Variant], config: PositionGenConfig, output: String): IO[Unit] =
    val positions = variant match
      case None    => Domain.supportedVariants.flatMap(generate(_, config))
      case Some(v) => generate(v, config)
    Stream
      .emits(positions)
      .map(_.position.csv)
      .write(output)

  private def perft(depth: Int): IO[Unit] =
    // Domain.supportedVariants.traverse_(perft(_, depth))
    perft(Atomic, depth)

  private def perft(variant: Variant, depth: Int): IO[Unit] =
    Domain.configs.traverse_(perft(variant, depth, _))

  private def perft(variant: Variant, depth: Int, config: PositionGenConfig): IO[Unit] =
    Stream
      .emits(generate(variant, config))
      .map(_.position)
      .zipWithIndex
      .evalMap((position, id) => PerftGenerator.gen(position, depth, id.toString))
      .map(_.toPerftString)
      .write(s"${variant.key}.perft")

  extension (lines: Stream[IO, String])
    def write(output: String): IO[Unit] =
      lines
        .intersperse("\n")
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(io.file.Path(output)))
        .compile
        .drain
