import cats.syntax.all.*
import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*

import chess.variant.*

import CLI.Args

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

  private def gen(variant: Option[Variant], moves: Int, positions: Int): IO[Unit] =
    variant match
      case None    => allVariants.traverse_(genPosition(_, moves, positions))
      case Some(v) => genPosition(v, moves, positions)

  private def genPosition(variant: Variant, moves: Int, positions: Int): IO[Unit] =
    IO.println(PositionGenerator.generate(variant, moves, positions).mkString("\n"))

  private val allVariants = List(Crazyhouse, Atomic, Horde, RacingKings, Antichess, ThreeCheck, KingOfTheHill)
