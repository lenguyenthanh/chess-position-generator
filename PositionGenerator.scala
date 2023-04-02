import chess.*
import chess.format.{Fen, EpdFen}
import chess.variant.Variant

object PositionGenerator:
  def generate(variant: Variant, moves: Int, positions: Int): List[EpdFen] =
    val situation = Situation(variant)
    (1 to positions)
      .map(_ => situation.gen(moves))
      .map(Fen.write(_))
      .toList

  extension (s: Situation)
    def next: Situation =
      val legalMoves = s.legalMoves
      if legalMoves.isEmpty then s
      else
        val rnd = scala.util.Random.nextInt(legalMoves.size)
        legalMoves(rnd).situationAfter

    def gen(moves: Int): Situation =
      if moves == 0 then s
      else s.next.gen(moves - 1)
