import cats.syntax.all.*
import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.*
import fs2.io.file.Files
import fs2.data.csv.*

import chess.variant.*

import CLI.Args
import Domain.*
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
      case Args.Perft(depth, output, config) => perft(depth, output, config)

  private def gen(variant: Option[Variant], config: PositionGenConfig, output: String): IO[Unit] =
    val positions = variant match
      case None    => Domain.supportedVariants.flatMap(generate(_, config))
      case Some(v) => generate(v, config)
    Stream
      .emits(positions)
      .map(_.position.csv)
      .write(output)

  private def perft(depth: Int, outputDir: String, config: Option[String]): IO[Unit] =
    Files[IO].createDirectory(io.file.Path(outputDir)).void.handleError(_ => ()) *>
      Domain.supportedVariants.traverse_(perft(_, depth, outputDir, config))

  private def perft(variant: Variant, depth: Int, outputDir: String, config: Option[String]): IO[Unit] =
    getConfig(config)
      .flatMap(perft(variant, depth, _))
      .zipWithIndex
      .evalMap((position, id) => PerftGenerator.gen(position, depth, id.toString))
      .map(_.toPerftString)
      .write(s"$outputDir/${variant.key}.perft")

  private def getConfig(config: Option[String]): Stream[IO, PositionGenConfig] =
    config match
      case None       => Stream.emits(Domain.configs)
      case Some(file) => Domain.parse(file)

  private def perft(variant: Variant, depth: Int, config: PositionGenConfig): Stream[IO, Position] =
    Stream
      .emits(generate(variant, config))
      .map(_.position)

  extension (lines: Stream[IO, String])
    def write(output: String): IO[Unit] =
      lines
        .intersperse("\n")
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(io.file.Path(output)))
        .compile
        .drain
