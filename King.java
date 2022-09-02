import java.util.Iterator;
import java.util.Map;

public class King extends Piece {
	
	final public int DIRECTIONS [][] = {{-1,1}, {-1,0}, {-1,-1}, {0,-1}, 
								  		{0,1}, {1,-1}, {1,0}, {1,1}};
	public boolean isChecked;
	private boolean hasMoved;
	final private static Map<String, String> castlingMoves = Map.of("w0-0-0", "c1",
																	"w0-0", "g1",
																	"b0-0-0", "c8",
																	"b0-0", "g8");
	
	public King(char name, boolean isWhite, String pos) {
		super(name, isWhite, pos);
	}
	
	@Override
	/*
	 * This method does all the heavy-lifting to check whether the move passed
	 * in as a parameter is a legal move for the king. Castling is handled in a
	 * separate method however this method does identify whether the move is
	 * attempting to castle.
	 */
	public boolean isValidMove(String move) {
		// Two conditions: first check if not reading a PGN file and trying to
		// castle
		if (!Chess_Engine.readingPGNFile) {
			// Check to see if the move is trying to castle.
			Iterator<String> iterator = castlingMoves.keySet().iterator();
			boolean moveFound = false;
			while (!moveFound && iterator.hasNext()) {
				String key = iterator.next();
				if (castlingMoves.get(key).equals(move)) {
					move = key.substring(1);
					moveFound = true;
				}
			}
			// At this point, either a castling move was found or was not. If it
			// was not, assume it's a normal king move and carry on. If it was,
			// call the checkCastling method.
			if (moveFound) {
				if (!hasMoved && !isChecked && checkCastling(move)) {
					return true;
				}
				return false;
			}
		}
		// Second condition: reading PGN file so field for castling must match
		// to continue validating the castling move.
		else if (Chess_Engine.readingPGNFile && Chess_Engine.castlingMove != 0) {
			if (!hasMoved && !isChecked && checkCastling(move)) {
				return true;
			}
			return false;
		}
		Square pos = position;
		boolean moveCross = pos.name.charAt(0) == move.charAt(0) || pos.name.charAt(1) == move.charAt(1);
		int factor = Math.abs(pos.name.charAt(0) - move.charAt(0));
		boolean moveDiagonal = factor == Math.abs(pos.name.charAt(1) - move.charAt(1));
		if (!moveCross && !moveDiagonal) {
			return false;
		}
		// Here, we know the king is moving vertically, horizontally, or diagonally
		// Let's eliminate moves that are trying to move the king more than one
		// square over.
		
		// Easy check for moving more than one square diagonally
		if (moveDiagonal && factor != 1) {
			return false;
		}
		// moving more than one square vertically
		if (pos.name.charAt(0) == move.charAt(0)) {
			if (Math.abs(pos.name.charAt(1) - move.charAt(1)) > 1) {
				return false;
			}
		}
		// moving horizontally: can't move more than one over
		if (pos.name.charAt(1) == move.charAt(1)) {
			if (Math.abs(pos.name.charAt(0) - move.charAt(0)) > 1) {
				return false;
			}
		}
		// Check to see if the move is trying to capture or move next to the
		// opposing king
		// Grab the other king first
		King otherKing = (King) board.getPiece(kingName, !isWhite, null);
		String otherKingPos = otherKing.position.name;
		// Easy check to see if the move is trying to capture the other king
		if (otherKingPos.equals(move)) {
			return false;
		}
		// Iterates 8 times, grabbing a different square around the other king's
		// position. If the move is equal to any one of these square names,
		// return false
		for (int dir[] : DIRECTIONS) {
			int nrow = dir[0] + Integer.parseInt(otherKingPos.substring(1));
			char ncol = (char) (dir[1] + otherKingPos.charAt(0));
			// doesn't matter if the displacement moves the position off the
			// board; the two strings will not match anyway
			String npos = ncol + "" + nrow;
			if (npos.equals(move)) {
				return false;
			}
		}
		
		// Here we know the move is valid: check if move is attempting to remove
		// a piece
		Square getMove = board.getSquare(move);
		if (getMove.occupiedBy != null) {
			if (checkForRemoving(getMove.occupiedBy)) {
				if (board.realMove) {
					hasMoved = true;
				}
				return true;
			}
			// trying to remove a piece failed
			return false;
		}
		if (board.realMove) {
			hasMoved = true;
		}
		return true;
	}
	
	/*
	 * This helper method checks whether the move passed in as a string parameter
	 * is a legal castle, given the where the pieces on the board, specifically 
	 * the locations of the king and its rooks. This method is only called if 
	 * isValidMove method identifies the move as an attempt to castle.
	 */
	private boolean checkCastling(String move) {
		// figure out which side player is trying to castle on: if move includes
		// 'G', then it's a queenside castle and the column numbers are getting bigger
		// hence the factor = 1
		int factor = move.equals(queenSideCastle) ? -1 : 1;
		// how many pieces do we have to worry about: 4 if queen-side castle, 3
		// otherwise
		int endValue = factor == 1 ? 3 : 4;
		int index = factor;
		// the side player is trying to castle on must be empty except for king
		// and rook
		while (Math.abs(index) < endValue) {
			char col = (char) ((int) ('e') + index);
			String name = col + "";
			String row = isWhite ? "1" : "8";
			name += row;
			Square s = board.getSquare(name);
			if (s.occupiedBy != null) {
				return false;
			}
			index = (Math.abs(index) + 1) * factor;
		}
		
		// Here the side we are castling on is empty: we must now make sure the
		// there is a rook in the right spot and it has not moved
		// grab the appropriate corner of the board
		String positionRook = isWhite ? factor == 1 ? "h1" : "a1" : factor == 1 ? "h8" : "a8";
		Square positionR = board.getSquare(positionRook);
		// check to make sure the piece there is actually a rook
		if (!(positionR.occupiedBy instanceof Rook)) {
			return false;
		}
		// we can now safely cast to rook
		Rook r = (Rook) (positionR.occupiedBy);
		// check to see if the rook has moved
		if (r.hasMoved) {
			return false;
		}
		
		// Here we are moving just the king to its position after castling
		// Let's see if this move results in a check
		Square posKing = position;
		String newpos = move.equals(kingSideCastle) ? isWhite ? "g1" : "g8" : isWhite ? "c1" : "c8";
		Square newPos = board.getSquare(newpos);
		newPos.changePieceOccupied(this, posKing);
		doCastling(r, factor);
		// Here, castling was fine, but the the move hasn't been made by the
		// player; it was an internal check by the program. Let's undo it.
		if (!board.realMove) {
			Chess_Engine.castlingMove = 0;
			undoMove(posKing, null, true);
			return true;
		}
		hasMoved = true;
		return true;
	}
	
	/*
	 * This method actually moves the pieces involved in a castle. This method
	 * is only called if the checkCastling method returns true, indicating that
	 * castling is a legal move. 
	 */
	private void doCastling(Rook r, int factor) {
		// We already moved the king
		// Let's move the rook
		Square oldPos = r.position;
		String rookPos = isWhite ? factor == 1 ? "f1" : "d1" : factor == 1 ? "f8" : "d8";
		Square rookP = board.getSquare(rookPos);
		rookP.changePieceOccupied(r, oldPos);
		Chess_Engine.castlingMove = rookPos.equals("f1") ? 1 : 2;
	}
	
	@Override
	/*
	 * This method returns what a king would be displayed as on the board.
	 */
	public String toString() {
		return isWhite ? " WK " : " BK ";
	}
}