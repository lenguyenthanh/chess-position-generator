import cats.data.ValidatedNel
import cats.syntax.all.*

import com.monovore.decline.*
import chess.variant.*

object CLI:

  enum Args:
    case Gen(variant: Variant, moves: Int, positions: Int) // todo --output option
    case Perft(file: String, depth: Int)

  def parse: Opts[Args] = Opts.subcommand(genCommand)

  private val variantOpt: Opts[Variant] = Opts
    .option[String]("variant", "Variant to generate positions for", "v")
    .mapValidated(validate)

  private val movesOpt = Opts
    .option[Int]("moves", "Number of moves to generate", "m")
    .withDefault(10)

  private val positionsOpt = Opts
    .option[Int]("positions", "Number of positions to generate", "p")
    .withDefault(10)

  private val genCommand = Command(
    name = "gen",
    header = "Generate positions for a given variant"
  ) {
    (variantOpt, movesOpt, positionsOpt).mapN(Args.Gen.apply)
  }

  private def validate(variant: String): ValidatedNel[String, Variant] =
    variant match
      case "crazyhouse"    => Crazyhouse.validNel
      case "atomic"        => Atomic.validNel
      case "horde"         => Horde.validNel
      case "racingkings"   => RacingKings.validNel
      case "antichess"     => Antichess.validNel
      case "3check"        => ThreeCheck.validNel
      case "threecheck"    => ThreeCheck.validNel
      case "kingofthehill" => KingOfTheHill.validNel
      case _ =>
        s"Unknown or unsupported variant: $variant.\nSupported variants: crazyhouse, atomic, horde, racingkings, antichess, threecheck, kingofthehill".invalidNel
