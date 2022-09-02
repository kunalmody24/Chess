/*
 * This is the parent class for all the pieces used on the board. All piece
 * classes inherit from this one. This class implements the main method used
 * for making a move. After validating input, control is transferred to this
 * method, where it then moves to the appropriate subclass for checking
 * if the move is valid. Actually moving the piece and capturing other pieces
 * occur here, however.
 */
import java.util.ArrayList;
public class Piece implements chessInterface {
	
	public final boolean isWhite;
	public final char name;
	public boolean isChecking;
	public boolean onBoard;
	public Square position;
	
	public Piece(char n, boolean white, String start) {
		name = n;
		isWhite = white;
		onBoard = true;
		position = board.getSquare(start);
		position.changePieceOccupied(this, null);
		board.piecesOnBoard.add(this);
	}
	
	/**
	 * Removes the calling object from the board; this method is only called
	 * after the appropriate checks have been made.
	 */
	public void removePiece() {
		position.occupiedBy = null;
		onBoard = false;
		assert board.piecesOnBoard.remove(this);
	}

	/**
	 * First method that is called when a player wants to make a move. All calls
	 * to subsequent methods occur here (checking if a move is valid, if the 
	 * move is a check, if the game is over, etc). All updates after a move is 
	 * made also occur here (changing involved pieces' and squares' variables).
	 * @param move: move requested by player.
	 * @return true if the move is valid, false otherwise
	 */
	public boolean makeMove(String move) {
		// Quick check to make sure piece being moved isn't of the opposing
		// color
		//System.out.println(this);
		if (isWhite != board.internalWhiteTurn) {
			return false;
		}
		Piece tempEnPassantPawn = board.enPassantPawn;
		if (board.enPassantPawn != null) {
			if (board.enPassantPawn.isWhite && board.masterWhiteTurn) {
				// Here, the enPassantPawn has been "alive" for two rounds. At this
				// point, the black player has lost the ability to capture it. We
				// must null it out. This nulling out assumes the move about to played
				// is valid. We do not know that yet, which is why we have saved the
				// the enPassantPawn in case we need to restore the field.
				board.enPassantPawn = null;
			}
			else if (!board.enPassantPawn.isWhite && !board.masterWhiteTurn) {
				// Here the enPassantPawn has been "alive" for two rounds. At this
				// point, the white player has lost the ability to capture it.
				board.enPassantPawn = null;
			}
		}
		// Hold onto the current position, may need it in case we
		// undo the move about to be played
		Square oldPos = position;
		Square newPos = null;
		Piece possibleCapture = null;
		// if the move is not castling, newPos is the position the piece will
		// move to if the move is legal and valid. possibleCapture will be
		// the opposing piece that is on newPos, if any. If the position IS
		// castling, these fields remain null.
		if (Chess_Engine.castlingMove == 0) {
			newPos = board.getSquare(move);
			possibleCapture = newPos.occupiedBy;
		}
		// The main method to check if the move is valid and legal. If it's not
		// we return false (no harm, no foul; move wasn't made anyway)
		if (!isValidMove(move)) {
			// Restore enPassantPawn
			board.enPassantPawn = tempEnPassantPawn;
			return false;
		}
		// make the move if the move wasn't castling/pawn promotion. We already
		// moved all the pieces otherwise.
		if (Chess_Engine.castlingMove == 0 && !Chess_Engine.pawnPromotion) {
			newPos.changePieceOccupied(this, oldPos);
		}
		// Check if the player moved into a check; we need to see if this move
		// is valid so this check occurs regardless of whether actuallyMove is
		// true or not.
		if (board.isPieceChecking(board.internalWhiteTurn)) {
			// Here, the move by the player resulted in a check on themself;
			// we must undo the move and return false
			undoMove (oldPos, possibleCapture, true);
			// Restore enPassantPawn
			board.enPassantPawn = tempEnPassantPawn;
			return false;
		}
		// Check if the player made a check. We only care if the PLAYER made
		// a check, not if the program made a move and called this method;
		// hence, the extra condition for realMove to be true
		if (board.isPieceChecking(!board.internalWhiteTurn) && board.realMove) {
			// Looks like a check occurred: let's see if the player indicated
			// the move correctly
//			if (ChessUserInterface.checkIndicated) {
				// Looks like they indicated a check was going to occur: good.
				// Let's see if they also indicated the game was over
				King k = (King) (board.getPiece(kingName, !board.internalWhiteTurn, null));
				boolean actuallyCheckmate = board.isCheckMate(k, this);
				if (actuallyCheckmate) {
					board.isCheckmate = true;
				}
//				if (ChessUserInterface.checkmateIndicated && actuallyCheckmate) {
//					// Here, the player indicated the game was going to end.
//					if (actuallyMove) {
//						ChessUserInterface.gameOver = true;
//					}
				//}
//				else if (ChessUserInterface.checkmateIndicated ^ actuallyCheckmate) {
//					// Here, either checkmate was not indicated or it was
//					// indicated but the move did not result in checkmate
//					undoMove(oldPos, possibleCapture, move, true);
//					return false;
//				}
				// Here, it was a normal check and the player indicated it.
			//}
//			else {
//				// They did not indicate a check was going to occur. Undo the
//				// move and return false.
//				undoMove(oldPos, possibleCapture, move, true);
//				return false;
//			}
		}
		// If this method was called by an internal check, we don't want to 
		// make the move. Undo the move if realMove is false.
		if (!board.realMove) {
			undoMove(oldPos, possibleCapture, true);
			// Restore enPassantPawn
			board.enPassantPawn = tempEnPassantPawn;
		}
		// If the move was made by the player, then at this point, the move was
		// legal, valid, and reflected on the board. We can now change player
		// turn and reset all fields.
		else {
			board.masterWhiteTurn = !board.masterWhiteTurn;
			board.internalWhiteTurn = board.masterWhiteTurn;
			Chess_Engine.pawnPromotion = false;
			Chess_Engine.castlingMove = 0;
			// Check to see if the new player is now in stalemate
			if (board.isStaleMate()) {
				board.isStalemate = true;
			}
		}
		// Here the move is valid and legal. Return true
		// Only for randomized play
		//System.out.println("CALL board.changed in makeMove");
		Player.count++;
//		System.out.println(move);
//		System.out.println(this);
		//board.changed(move);
		return true;
	}
	
	/**
	 * This method undoes the move after said move is deemed invalid.
	 * @param oldPos: the position the piece needs to move back to.
	 * @param removed: any piece that was removed as a result of the move; this
	 * parameter is null if no piece was removed.
	 * @param immediateSpecial: a parameter needed to separate different calls
	 * made by the program.
	 */
	public void undoMove(Square oldPos, Piece removed, boolean immediateSpecial) {
		if (Chess_Engine.castlingMove != 0 && immediateSpecial) {
			// we have to undo castling. That requires moving two pieces back,
			// not one.
			// Let's move the king back first.
			oldPos.changePieceOccupied(this, position);
			// Now let's move the rook back.
			// We have to first figure out which way the castling occurred.
			// If the field that indicates a castling move equals 1, then it
			// was a kingside castle, otherwise, it was a queenside castle.
			String rookpos = Chess_Engine.castlingMove == 1 ? isWhite ? "f1" : "f8" : isWhite ? "d1" : "d8";
			Square rookPos = board.getSquare(rookpos);
			Rook r = (Rook) rookPos.occupiedBy;
			assert r != null;
			// Find position to move back to
			String moveback = Chess_Engine.castlingMove == 1 ? isWhite ? "h1" : "h8" : isWhite ? "a1" : "a8";
			Square moveBack = board.getSquare(moveback);
			moveBack.changePieceOccupied(r, rookPos);
			return;
		}
		else if (Chess_Engine.pawnPromotion && immediateSpecial) {
			// We have to first get rid of the queen the pawn was promoted to
			// It is currently in the calling object's position.
			Square posQueen = position;
			Queen removeQueen = (Queen) board.getPiece(queenName, isWhite, posQueen);
			removeQueen.removePiece();
			// We have removed the queen from the board. We now have to add the
			// pawn back
			if (isWhite) {
				board.piecesOnBoard.add(new WhitePawn(pawnName, true, oldPos.name));
			}
			else {
				board.piecesOnBoard.add(new BlackPawn(pawnName, false, oldPos.name));
			}
			return;
		}
		// Worst case a piece was removed as a result of this move: let's add
		// that piece back
		if (removed != null) {
			board.piecesOnBoard.add(removed);
			removed.onBoard = true;
			position.changePieceOccupied(removed, null);
			// To move the piece back, we don't want to null out the position
			// it is at because a piece was just added there. Therefore,
			// we pass in null for the second parameter, indicating that no
			// change needs to occur at the position it is currently at.
			oldPos.changePieceOccupied(this, null);
			return;
		}
		// No piece was removed from this move, removed = null
		// Let's move the piece back. This time, because no piece was removed,
		// we need to null out the position it is at after moving the piece back.
		// Therefore, we pass in 'position' as the second parameter.
		oldPos.changePieceOccupied(this, position);
	}
	
	/**
	 * This method checks if a move made by a piece is valid: because this method
	 * is general to all pieces, piece-specific checks have already been made
	 * in the respective classes before this method is called. The only check
	 * that occurs in this method is if the move is unobstructed and if the move
	 * is attempting to remove a piece.
	 * @param move
	 * @param actuallyRemove
	 * @return true if the move is valid, false otherwise.
	 */
	public boolean checkMove(String move) {
		// grab all squares leading to the move
		// this include the initial position of the piece being moved
		ArrayList<Square> squares = grabAllSquares(board.getSquare(move));
		// In this situation, we don't care about the very first position, so
		// let's remove it
		squares.remove(position);
		// Now we only have the moves that are leading up to the move; if there
		// is a piece in any of these squares, the move is invalid
		for (Square square : squares) {
			if (square.occupiedBy != null) {
				return false;
			}
		}
		// Here the move is unobstructed; let's check to see if the move is
		// trying to remove a piece
		Square moveTo = board.getSquare(move);
		if (moveTo.occupiedBy != null) {
			return checkForRemoving(moveTo.occupiedBy);

		}
		// The move is already valid here: return true
		return true;
	}
	
	/**
	 * This method is called to check if a piece can be removed validly.
	 * @param toRemove: the piece requested for removal
	 * @return true if a piece can be removed successfully, false otherwise.
	 */
	public boolean checkForRemoving(Piece toRemove) {
		// move is attempting to remove a piece. Check to make sure it's of the
		// opposite color
		if (toRemove.isWhite == !isWhite) {
			// Check to make sure the player isn't trying to remove the king
			if (!(toRemove instanceof King)) {
				// The player is removing a piece which is not the king. Get it
				// off the board.
				toRemove.removePiece();
			}
			return true;
//			if (ChessUserInterface.possiblePieceRemoval && !(toRemove instanceof King)) {
//				removePiece(toRemove);
//				return true;
//			}
//			return ChessUserInterface.checkIndicated && toRemove instanceof King;
		}
		
		// trying to move to a space occupied by a friendly piece or input never
		// specified a piece removal
		return false;
	}
	
	/**
	 * This method is overridden in each piece class. The implementation in this
	 * class is for dummy purposes only. Trying to run this implementation WILL 
	 * result in an error.
	 * @param name
	 * @param actuallyRemove
	 * @return true if the move is valid, false otherwise
	 */
	public boolean isValidMove(String name) {
		throw new IllegalStateException("IMPLEMENTATION ERROR");
	}
	
	/**
	 * This method is once again used for dummy purposes: all pieces except the
	 * King class override this method to check whether they can capture king.
	 * An important feature of the program is the way this method is implemented.
	 * To check if the calling piece can capture the opposing king, an attempt
	 * to actually capture is required; however, the player did not request this
	 * move. For this reason, a boolean value is flipped, indicating internal
	 * control requested the move. All moves carry on as usual, but at the end,
	 * the move is undone. Every call to changing piece positions made by program
	 * command changes the switch, makes the move, then reverts switch back to
	 * the old value (one exception in Board class: checking if King can get out
	 * of check).
	 */
	public void updateCaptureKing() {
		return;
	}
	
	/**
	 * This method is also used for dummy purposes: all pieces except the King
	 * class override this method to collect the squares leading up to the
	 * position passed as a parameter.
	 * @param position: position to end at when adding squares to list
	 * @return a list of squares leading up the position passed as a parameter.
	 */
	public ArrayList<Square> grabAllSquares(Square position) {
		return new ArrayList<Square>();
	}
	
	@Override
	/*
	 * Compares two objects and only returns true if the two are of the same
	 * piece class, the same color, and occupy the same square. Returns false
	 * if any of those conditions are not met.
	 */
	public boolean equals(Object obj) {
		// Of Piece type
		if (!(obj instanceof Piece)) {
			return false;
		}
		Piece compare = (Piece) (obj);
		// Of same subclass type (piece type)
		if (!(getClass() == compare.getClass())) {
			return false;
		}
		// Of same color
		if (!(isWhite == compare.isWhite)) {
			return false;
		}
		// Pieces are in the exact same position
		if (!position.name.equals(compare.position.name)) {
			return false;
		}
		// If all criteria are met, then the pieces are the same
		return true;
	}
}