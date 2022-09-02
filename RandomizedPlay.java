import java.util.ArrayList;
import java.util.Random;

public class RandomizedPlay {
	
	public static void main(String[] args) {
		Random r = new Random();
		// We want to automize play for a chess game: random moves for both
		// players until the game ends in a checkmate or a stalemate
		// We first need to be able to randomly pick a piece to move, then pick
		// a place to move it to. If the move is legal, make it, otherwise, pick
		// another piece and square to move to.
		
		while (!(chessInterface.board.isCheckmate && chessInterface.board.isStalemate)) {
			// Keep picking pieces from the correct team
			Piece piece;
			String move;
			do {
				ArrayList<Piece> pieces = chessInterface.board.masterWhiteTurn ? Chess_Engine.getWhite() : Chess_Engine.getBlack();
				// Get random piece
				piece = pieces.get(r.nextInt(pieces.size() - 1));
				// Get random square
				char col = (char) (r.nextInt(8) + 'a');
				int row = r.nextInt(7) + 1;
				move = col + "" + row;
				Chess_GUI.TilePanel.
			}
			while (!piece.makeMove(move));
		}
	}
}
