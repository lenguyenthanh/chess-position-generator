import cats.syntax.all.*
import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*

import chess.variant.Variant

import CLI.Args

object Main
    extends CommandIOApp(
      name = "chess-gen",
      header = "Generate positions for a given variant",
      version = "0.0.1"
    ):

  override def main: Opts[IO[ExitCode]] = CLI.parse
    .map(execute(_).as(ExitCode.Success))

  private def execute(args: Args): IO[Unit] =
    args match
      case Args.Gen(variant, moves, positions) =>
        genPosition(variant, moves, positions)
      case Args.Perft(file, depth) => ???

  private def genPosition(
      variant: Variant,
      moves: Int,
      positions: Int
  ): IO[Unit] =
    IO.println(
      PositionGenerator.generate(
        variant,
        moves,
        positions
      ).mkString("\n")
    )
