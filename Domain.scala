import fs2.data.csv.*
import fs2.data.csv.generic.semiauto.*
import cats.syntax.all.*

import chess.format.EpdFen
import chess.variant.*

object Domain:

  given CellDecoder[EpdFen]  = CellDecoder[String].map(EpdFen(_))
  given CellDecoder[Variant] = CellDecoder(validate(_).leftMap(DecoderError(_)))
  given RowDecoder[Position] = deriveRowDecoder

  case class Position(variant: Variant, epd: EpdFen):
    def csv: String = s"${variant.key},$epd"

  val supportedVariants = List(Crazyhouse, Atomic, Horde, RacingKings, Antichess, ThreeCheck, KingOfTheHill)

  def validate(variant: String): Either[String, Variant] =
    variant match
      case "crazyhouse"    => Right(Crazyhouse)
      case "atomic"        => Right(Atomic)
      case "horde"         => Right(Horde)
      case "racingkings"   => Right(RacingKings)
      case "antichess"     => Right(Antichess)
      case "3check"        => Right(ThreeCheck)
      case "threecheck"    => Right(ThreeCheck)
      case "kingofthehill" => Right(KingOfTheHill)
      case _ =>
        Left(
          s"Unknown or unsupported variant: $variant.\nSupported variants: crazyhouse, atomic, horde, racingkings, antichess, threecheck, kingofthehill"
        )


  case class Perft(id: String, epd: EpdFen, cases: List[TestCase]):
    def max = cases.last.nodes

  case class TestCase(depth: Int, nodes: Long)
