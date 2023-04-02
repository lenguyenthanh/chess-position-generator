import chess.*
import chess.format.{Fen, EpdFen, Uci}
import chess.variant.Variant
import chess.MoveOrDrop.*
import cats.data.State

object PositionGenerator:

  case class Result(val fen: EpdFen, val moves: List[Uci])
  def generate(variant: Variant, moves: Int, positions: Int): List[Result] =
    val situation = Situation(variant)
    (1 to positions)
      .map(_ => situation.gen(moves, Nil))
      .map((sit, moves) => Result(Fen.write(sit), moves.map(_.toUci)))
      .toList

  val nextMove: State[Situation, Option[Move]] = State(sit =>
    val legalMoves = sit.legalMoves
    if legalMoves.isEmpty then (sit, None)
    else
      val rnd = scala.util.Random.nextInt(legalMoves.size)
      val move = legalMoves(rnd)
      (move.situationAfter, Some(move))
  )

  def gen(moves: Int): State[Situation, List[Move]] =
    if moves == 0 then State.pure(Nil)
    else
      nextMove.flatMap(move =>
        gen(moves - 1).map(moves => move.toList ++ moves)
      )

  extension (s: Situation)
    def next: Option[MoveOrDrop] =
      val legalMoves = s.legalMoves
      if legalMoves.isEmpty then None
      else
        val rnd = scala.util.Random.nextInt(legalMoves.size)
        Some(legalMoves(rnd))

    def gen(
        moves: Int,
        accumulation: List[MoveOrDrop]
    ): (Situation, List[MoveOrDrop]) =
      if moves == 0 then (s, accumulation)
      else
        s.next match
          case None       => (s, accumulation)
          case Some(move) => gen(moves - 1, accumulation :+ move)
