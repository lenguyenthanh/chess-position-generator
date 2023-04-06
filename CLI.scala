import cats.data.ValidatedNel
import cats.syntax.all.*

import com.monovore.decline.*
import chess.variant.*

object CLI:

  enum Args:
    case Gen(variant: Option[Variant], moves: Int, positions: Int, output: String)
    case Perft(file: String, depth: Int)

  private val variantOpt: Opts[Option[Variant]] = Opts
    .option[String]("variant", "Variant to generate positions for", "v")
    .mapValidated(Domain.validate(_).toValidatedNel)
    .orNone

  private val movesOpt = Opts
    .option[Int]("moves", "Number of moves to generate", "m")
    .withDefault(10)

  private val positionsOpt = Opts
    .option[Int]("positions", "Number of positions to generate", "p")
    .withDefault(10)

  private val outputOpt = Opts
    .option[String]("output", "Output file", "o")
    .withDefault("positions.csv")

  private val inputOpt = Opts
    .option[String]("input", "Input file", "o")
    .withDefault("positions.csv")

  val genOpts = (variantOpt, movesOpt, positionsOpt, outputOpt).mapN(Args.Gen.apply)

  val parse: Opts[Args] = Opts.subcommand(
    "gen",
    help = "Generate positions for a given variant(s)"
  ) {
    genOpts
  }
