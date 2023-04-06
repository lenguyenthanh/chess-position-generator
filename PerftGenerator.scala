import cats.syntax.all.*
import cats.effect.syntax.all.*
import cats.effect.IO

import chess.format.{ EpdFen, Fen }
import chess.variant.*
import chess.{ MoveOrDrop, Situation }
import chess.MoveOrDrop.*

import Domain.*

object PerftGenerator:

  def gen(position: Position, depth: Int, id: String): IO[Perft] =
    val situation = Fen.read(position.variant, position.epd).getOrElse {
      throw RuntimeException(s"Invalid position: $position")
    }

    (1 to depth).toList
      .traverse(depth =>
        situation
          .perft(depth)
          .map(TestCase(depth, _))
      )
      .map(cases => Perft(id, position.epd, cases))

  extension (s: Situation)

    def perft(depth: Int): IO[Long] =
      if depth == 0 then IO(1L)
      else if s.perftEnd then IO(0L)
      else
        val moves = s.perftMoves
        if depth == 1 then moves.size.toLong.pure[IO]
        else moves.foldMapA(_.situationAfter.perft(depth - 1))

    private def perftMoves: List[MoveOrDrop] =
      if s.board.variant == chess.variant.Crazyhouse
      then Crazyhouse.legalMoves(s)
      else
        val legalMoves = s.legalMoves
        if s.board.variant.chess960 then legalMoves
        // if variant is not chess960 we need to deduplicated castlings moves
        // We filter out castling move that is Standard and king's dest is not in the rook position
        else legalMoves.filterNot(m => m.castle.exists(c => c.isStandard && m.dest != c.rook))

    private def crazyhousePerftMoves: List[MoveOrDrop] =
      Crazyhouse.legalMoves(s)

    // when calculate perft we don't do autoDraw
    def perftEnd = s.checkMate || s.staleMate || s.variantEnd || s.board.variant.specialDraw(s)
