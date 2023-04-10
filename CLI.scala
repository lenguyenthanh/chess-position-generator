import cats.data.ValidatedNel
import cats.syntax.all.*

import com.monovore.decline.*
import chess.variant.*

import Domain.PositionGenConfig

object CLI:

  enum Args:
    case Gen(variant: Option[Variant], config: PositionGenConfig, output: String)
    case Perft(variants: List[Variant], depth: Int, output: String, config: Option[String])

  given Argument[Variant] with
    def read(string: String): ValidatedNel[String, Variant] =
      Domain.validate(string).toValidatedNel

    def defaultMetavar: String = "variant"

  private val variantOpt: Opts[Option[Variant]] = Opts
    .option[Variant]("variant", "Variant to generate positions for", "v")
    .orNone

  private val variantsOpt: Opts[List[Variant]] = Opts
    .options[Variant]("variants", "Variants to generate perfts, empty means all", "v").orEmpty

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

  private val outputDirOpt = Opts
    .option[String]("output", "Output dir", "o")
    .withDefault("perfts")

  private val configFileOpt = Opts
    .option[String]("config", "Config file", "o")
    .orNone

  val genOpt: Opts[Args] = Opts.subcommand(
    "gen",
    help = "Generate positions for a given variant(s)"
  ) { (variantOpt, configOpt, outputOpt).mapN(Args.Gen.apply) }

  val perftOpt = Opts.subcommand(
    "perft",
    help = "Generate perft for all variants follows a fixed config"
  ) { (variantsOpt, depthOpt, outputDirOpt, configFileOpt).mapN(Args.Perft.apply) }

  val parse: Opts[Args] = genOpt <+> perftOpt
