import cats.effect.*
import fs2.*
import fs2.io.file.Files
import fs2.data.csv.*
import fs2.data.csv.generic.semiauto.*
import cats.syntax.all.*

import chess.format.EpdFen
import chess.variant.*

object Domain:

  given CellDecoder[EpdFen]  = CellDecoder[String].map(EpdFen(_))
  given CellDecoder[Variant] = CellDecoder(validate(_).leftMap(DecoderError(_)))
  given RowDecoder[Position] = deriveRowDecoder

  val supportedVariants = List(Crazyhouse, Atomic, Horde, RacingKings, Antichess, ThreeCheck, KingOfTheHill)

  case class PositionGenConfig(moves: Int, positions: Int)

  case class Position(variant: Variant, epd: EpdFen):
    def csv: String = s"${variant.key},$epd"

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
    def toPerftString: String =
      s"""
         |id $id
         |epd $epd
         |${cases.map(_.toPerftString).mkString("\n")}
         |""".stripMargin.stripLeading

  case class TestCase(depth: Int, nodes: Long):
    def toPerftString: String = s"$depth $nodes"

  given RowDecoder[PositionGenConfig] = deriveRowDecoder

  val configs = List(
    // PositionGenConfig(9, 10),
    // PositionGenConfig(16, 10),
    PositionGenConfig(69, 100),
  )

  def parse(file: String): Stream[IO, PositionGenConfig] =
    Files[IO]
      .readAll(fs2.io.file.Path(file))
      .through(text.utf8.decode)
      .through(decodeWithoutHeaders[PositionGenConfig]())
