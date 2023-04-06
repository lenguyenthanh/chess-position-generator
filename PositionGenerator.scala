import chess.*
import chess.format.{ EpdFen, Fen, Uci }
import chess.variant.{ Crazyhouse, Variant }
import chess.MoveOrDrop.*
import cats.data.State

object PositionGenerator:
  import Domain.{ Position, PositionGenConfig }

  def generate(variant: Variant, config: PositionGenConfig): List[Result] =
    val situation = Situation(variant)
    (1 to config.positions)
      .map(_ => situation.gen(config.moves, Nil))
      .map((sit, moves) => Result(Position(variant, Fen.write(sit)), moves.map(_.toUci.uci)))
      .toList

  val nextMove: State[Situation, Option[Move]] = State(sit =>
    val legalMoves = sit.legalMoves
    if legalMoves.isEmpty then (sit, None)
    else
      val rnd  = scala.util.Random.nextInt(legalMoves.size)
      val move = legalMoves(rnd)
      (move.situationAfter, Some(move))
  )

  def gen(moves: Int): State[Situation, List[Move]] =
    if moves == 0 then State.pure(Nil)
    else nextMove.flatMap(move => gen(moves - 1).map(moves => move.toList ++ moves))

  case class Result(position: Position, moves: List[String]):
    override def toString(): String = s"$position;${moves.mkString(" ")}"

  extension (s: Situation)
    def next: Option[MoveOrDrop] =
      val legalMoves =
        if s.board.variant.crazyhouse then Crazyhouse.legalMoves(s)
        else s.legalMoves
      if legalMoves.isEmpty then None
      else
        val rnd = scala.util.Random.nextInt(legalMoves.size)
        Some(legalMoves(rnd))

    def gen(moves: Int, accumulation: List[MoveOrDrop]): (Situation, List[MoveOrDrop]) =
      if moves == 0 then (s, accumulation)
      else
        s.next match
          case None       => (s, accumulation)
          case Some(move) => move.situationAfter.gen(moves - 1, accumulation :+ move)
