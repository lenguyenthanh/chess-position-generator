import chess.*
import chess.format.{ EpdFen, Fen, Uci }
import chess.format.pgn.SanStr
import chess.variant.{ Crazyhouse, Variant }
import chess.MoveOrDrop.*
import cats.data.State

object PositionGenerator:
  import Domain.{ Position, PositionGenConfig }

  def generate(variant: Variant, config: PositionGenConfig): List[Result] =
    val game = Game(Situation(variant))
    (1 to config.positions)
      .map(_ => game.gen(config.moves))
      .map(game => Result(Position(variant, Fen.write(game)), game.sans.toList))
      .toList

  val nextMove: State[Situation, Option[Move]] = State(sit =>
    val legalMoves = sit.legalMoves
    if legalMoves.isEmpty then (sit, None)
    else
      val rnd  = scala.util.Random.nextInt(legalMoves.size)
      val move = legalMoves(rnd)
      (move.situationAfter, Some(move))
  )

  case class Result(position: Position, moves: List[SanStr]):
    override def toString(): String = s"$position;${moves.mkString(" ")}"

  extension (game: Game)
    def next: Option[MoveOrDrop] =
      val legalMoves =
        if game.board.variant.crazyhouse then Crazyhouse.legalMoves(game.situation)
        else game.situation.legalMoves
      if legalMoves.isEmpty then None
      else
        val rnd = scala.util.Random.nextInt(legalMoves.size)
        Some(legalMoves(rnd))

    def gen(moves: Int): Game =
      if moves == 0 then game
      else
        game.next match
          case None       => game
          case Some(move) => move.fold(game.apply, game.applyDrop).gen(moves - 1)
