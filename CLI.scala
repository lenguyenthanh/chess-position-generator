import cats.data.ValidatedNel
import cats.syntax.all.*

import com.monovore.decline.*
import chess.variant.*

object CLI:

  enum Args:
    case Gen(variant: Option[Variant], moves: Int, positions: Int)
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

  val genOpts = (variantOpt, movesOpt, positionsOpt).mapN(Args.Gen.apply)

  val parse: Opts[Args] = Opts.subcommand(
    "gen",
    help = "Generate positions for a given variant(s)"
  ) {
    genOpts
  }
