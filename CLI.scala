import cats.data.ValidatedNel
import cats.syntax.all.*

import com.monovore.decline.*
import chess.variant.*

import Domain.PositionGenConfig

object CLI:

  enum Args:
    case Gen(variant: Option[Variant], config: PositionGenConfig, output: String)
    case Perft(depth: Int)

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

  private val depthOpt = Opts
    .option[Int]("depth", "maximum depth for generated perfts", "d")
    .withDefault(10)

  private val configOpt = (movesOpt, positionsOpt).mapN(PositionGenConfig.apply)

  private val outputOpt = Opts
    .option[String]("output", "Output file", "o")
    .withDefault("positions.csv")

  private val inputOpt = Opts
    .option[String]("input", "Input file", "o")
    .withDefault("positions.csv")

  val genOpts = (variantOpt, configOpt, outputOpt).mapN(Args.Gen.apply)

  val genOpt: Opts[Args] = Opts.subcommand(
    "gen",
    help = "Generate positions for a given variant(s)"
  ) { genOpts }

  val perftOpt = Opts.subcommand(
    "perft",
    help = "Generate perft for all variants follows a fixed config"
  ) { depthOpt.map(Args.Perft.apply) }

  val parse: Opts[Args] = genOpt orElse perftOpt
