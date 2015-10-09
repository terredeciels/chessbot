package de.htwsaar.chessbot.engine.eval;

import de.htwsaar.chessbot.engine.model.Board;
import de.htwsaar.chessbot.engine.model.piece.Piece;

/**
 * Simple Bewertungsfunktion zählt Figurwerte.
 * 
 * @author Dominik Becker
 *
 */
public class MaterialEvaluator extends EvaluationFunction{

	@Override
	public int evaluate(Board b) {
		int materialCount = 0;
		int sign;
		for (Piece piece : b.getAllPieces()) {
			sign = piece.isWhite() ? 1 : -1;
			materialCount += sign * getPieceValue(piece.id());
		}
		return materialCount ;
	}
	
}