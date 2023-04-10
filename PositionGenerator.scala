import cats.syntax.all.*

import chess.*
import chess.format.{ EpdFen, Fen, Uci }
import chess.format.pgn.SanStr
import chess.variant.{ Crazyhouse, Variant }
import chess.MoveOrDrop.*

object PositionGenerator:
  import Domain.{ Position, PositionGenConfig }

  def generate(variant: Variant, config: PositionGenConfig): List[Result] =
    val game = Game(Situation(variant))
    (1 to config.positions)
      .flatMap(_ => game.gen(config.moves, Some(5000)))
      .map(game => Result(Position(variant, Fen.write(game)), game.sans.toList))
      .toList

  case class Result(position: Position, moves: List[SanStr]):
    override def toString(): String = s"$position;${moves.mkString(" ")}"

  extension (game: Game)
    def gen(moves: Int, retry: Option[Int]): Option[Game] =
      gen(moves).recoverWith {
        case _ if retry.exists(_ > 0) => gen(moves, retry.map(_ - 1))
        case _                        => None
      }

    def gen(moves: Int): Option[Game] =
      if moves == 0 then Some(game)
      else
        for
          move <- game.next
          next = move.fold(game.apply, game.applyDrop)
          gen <- next.gen(moves - 1)
          if !gen.situation.end
        yield gen

    def next: Option[MoveOrDrop] =
      if game.situation.end then None
      else
        val legalMoves =
          if game.board.variant.crazyhouse then Crazyhouse.legalMoves(game.situation)
          else game.situation.legalMoves
        if legalMoves.isEmpty then None
        else
          val rnd = scala.util.Random.nextInt(legalMoves.size)
          Some(legalMoves(rnd))
