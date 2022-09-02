/*
 * This is the class for the board. All pieces are tracked here, and the getter
 * methods used by other classes are also implemented here. One important 
 * feature of this class is the way in which the check for checkmate is
 * implemented. An internal clock for whose turn it is is created in order to
 * simulate player turns before they occur. If a check occurs, the program
 * must check to see if the game is over (checkmate). It does this by changing
 * the player turn first, so now the player who is being checked must move,
 * according to the internal program clock, at least. By doing this, the program
 * can play different scenarios to see if the player can block or remove the
 * check. At the end of this simulation, either the internal clock is restored
 * to the masterWhiteTurn clock, which didn't change during the simulation,
 * or the game is over and STATS are displayed.
 */
import java.util.ArrayList;
import java.util.Observable;
@SuppressWarnings("deprecation")
public class Board extends Observable implements chessInterface {

	public final Square[][] board;
	public ArrayList<Piece> piecesOnBoard;
	public boolean masterWhiteTurn;
	public boolean internalWhiteTurn;
	public boolean realMove;
	public boolean isCheckmate;
	public boolean isStalemate;
	public Piece enPassantPawn;
	
	public Board() {
		board = new Square [numRows][numColumns];
		int actual = numRows;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numColumns; j++) {
				char letter = (char) (j + firstLetter);
				String name = letter + "" + actual;
				board[i][j] = new Square(name);
			}
			actual--;
		}
		piecesOnBoard = new ArrayList<>();
		internalWhiteTurn = true;
		masterWhiteTurn = true;
		realMove = REAL_MOVE;
//		addObserver(new Player("white"));
//		addObserver(new Player("black"));
	}
	
	// Only for randomized play
//	public void changed(String move) {
//		//System.out.println("Inside board.changed");
//		// Move was just made. Let's update the board
//		if (move != null) {
//			Chess_GUI.TilePanel t = Chess_Engine.mainGUI.boardPanel.getTile(move);
//			assert t != null;
//			t.changed();
//		}
//		// The board just got updated. We have to notify the next player so it
//		// can make its move
//		setChanged();
//		String turn = masterWhiteTurn ? "white" : "black";
//		System.out.println(this);
//		notifyObservers(turn);
//	}
	
	/*  
	 *  This method will update only pieces of the player who has just moved and
	 *  their hypothetical capture of the king; it will also return true or false
	 *  depending on whether a check was made
	 */
	public boolean isPieceChecking(boolean whiteT) {
		boolean check = false;
		int index = 0;
		while (index < piecesOnBoard.size() && !check) {
			Piece p = piecesOnBoard.get(index);
			if (p.isWhite == !whiteT) {
				p.updateCaptureKing();
				check = p.isChecking;
			}
			index++;
		}
		King k = (King) (getPiece(kingName, whiteT, null));
		k.isChecked = check;
		return check;
	}
	
	public boolean isStaleMate() {
		// For a player to be in stalemate, they first cannot be in check. Let's
		// make sure. Grab the king of the player moving and check to see if it
		// is in check.
		King k = (King) getPiece(kingName, internalWhiteTurn, null);
		if (k.isChecked) {
			return false;
		}
		// Now we know that the player moving is not checked. We must now do the
		// most time consuming part: check to see if any pieces of the player
		// moving can make a legal and valid move anywhere on the board.
		
		// Indicate to program subsequent moves are not real
		realMove = !REAL_MOVE;
		// Iterate through all the pieces first
		for (int i = 0; i < piecesOnBoard.size(); i++) {
			Piece p = piecesOnBoard.get(i);
			// Make sure the piece is of the right color
			if (p.isWhite == internalWhiteTurn) {
				// Check to see if this piece can make any move. Iterate through 
				// all squares on the board, seeing if the piece can make it to 
				// any one of these squares legally.
				// Iterate through all rows
				for (int r = 0; r < numRows; r++) {
					// Iterate through all columns in row r
					for (int c = 0; c < numColumns; c++) {
						// Find square given row r and column c.
						String name = ((char) (firstLetter + c)) + "" + (numRows-r);
						Square s = getSquare(name);
						if (p.makeMove(s.name)) {
							// Only possibility of old value of switch is true.
							// Change it back
							realMove = REAL_MOVE;
							// Here a piece can make a move. Return false.
							return false;
						}
					}
				}
			}
		}
		// Here, no piece from the player moving can make a move anywhere on the
		// board legally, but the player is not checked. This is stalemate. No
		// need to change switch, game is over.
		return true;
	}
	
	/*
	 * This method will only be called if a check is made: the method checks
	 * whether the game is over
	 * The king being checked and the piece checking are passed as parameters
	 */
	public boolean isCheckMate(King k, Piece p) {
		// we have to first check if the king can move out of the check
		// this includes the king capturing the piece that gives the check
		// there are 8 possible moves the king can make: some may not be possible
		// given where the king is on the board (we must account for that)
		
		// The appropriate king is already passed as a parameter
		// if one of these moves gets the king out of the check, it's not checkmate
		Square currPos = k.position;
		int row = getRow(currPos.name.substring(1));
		int col = getCol(currPos.name.substring(0,1));
		// iterates through all directions
		for (int d[] : k.DIRECTIONS) {
			// checking to make sure the move is going to keep the king on the board
			if (row + d[0] >= 1 && row + d[0] <= numRows && col + d[1] >= 1 && col + d[1] <= numColumns) {
				char newCol = (char) (firstLetter + col - 1 + d[1]);
				String newName = newCol + "" + (row + d[0]);
				Square newPos = getSquare(newName);
				Piece capture = newPos.occupiedBy;
				// Is there a piece where the king is trying to move? That piece
				// needs to be of the opponent. Other option is there is no piece
				if ((capture != null && capture.isWhite != k.isWhite) || capture == null) {
					// Move is valid: check if this move gets the king out of
					// check
					if (canKingMove(k, newPos, currPos, capture)) {
						return false;
					}
				}
			}
		}
		// Here there are no possible positions for the king to move to that don't
		// result in a check; let's see if there are any friendly pieces that can
		// get rid of the check or pin themselves
		// To do this, we must first grab all the squares from the piece checking
		// to the king being checked.
		ArrayList<Square> squares = p.grabAllSquares(k.position);
		// *** internal clock changed here!!! ***
		internalWhiteTurn = !internalWhiteTurn;
		// Now let's see if there are any friendly pieces that can get to one of
		// these squares: if any piece can, then the check has been blocked or
		// removed.
		
		// Indicate to program subsequent moves are not real
		realMove = !REAL_MOVE;
		// iterate through all pieces on the board
		for (int i = 0; i < piecesOnBoard.size(); i++) {
			Piece piece = piecesOnBoard.get(i);
			// making sure the piece is friendly and not the King
			if (piece.isWhite == k.isWhite && !piece.equals(k)) {
				for (Square s : squares) {
					// if the piece can get to any one of the squares in the list,
					// the check has been either blocked or eliminated: the game
					// goes on!!!
					if (piece.makeMove(s.name)) {
						// Only possibility of old value of switch is true
						// Change it back
						realMove = REAL_MOVE;
						return false;
					}
				}
			}
		}
		// Here, the king cannot get out of the check by himself and there is
		// no friendly piece that can get rid of the check: this means there are
		// no valid moves left for the player being checked. OH NO, GAME OVER!!
		// THIS IS CHECKMATE!!!
		return true;
	}
	
	/*
	 * This method checks to see if the king in question can legally move to the
	 * position passed in as a parameter. The move is then undone and the method
	 * returns true if the move is a possible legal move and false otherwise.
	 * This method is only called when the king is in check and we are checking
	 * to see if the game is over (one criteria is that the king cannot move 
	 * anywhere).
	 */
	private boolean canKingMove(King k, Square newPos, Square currPos, Piece capture) {
		boolean newCheck = false;
		// Is the move valid?
		// Method is called through checkmate, so switch should already be
		// false; for safety purposes, explicitly set to false here.
		realMove = !REAL_MOVE;
		if (k.isValidMove(newPos.name)) {
			// make the move
			newPos.changePieceOccupied(k, currPos);
			// The move is valid, but does it get the king out of check?
			if (!isPieceChecking(!masterWhiteTurn)) {
				// The move is valid and gets the king out of check. Game goes
				// on!!
				newCheck = true;
			}
			// undo the move
			k.undoMove(currPos, capture, false);
		}
		// Don't change switch back to true; method is called through checkmate
		// and we don't know if it is done iterating through all possibilities.
		
		// Make sure we reset the king's isChecked field to true just in case
		// the move got the king out of check.
		k.isChecked = true;
		return newCheck;
	}
	
	/*
	 * This method works as a tool to other classes: it returns the square 
	 * corresponding to the name passed as a parameter.
	 */
	public Square getSquare(String name) {
		char first = Character.toLowerCase(name.charAt(0));
		String actualName = first + "" + name.charAt(1);
		int row = getRow(actualName.substring(1));
		int col = getCol(actualName.substring(0,1));
		return board[numRows - row][col - 1];
	}
	
	/*
	 * This method works as a tool to other classes: it returns the row of the
	 * square passed in as a parameter. The square's name is passed in, not the
	 * square itself.
	 */
	public int getRow(String row) {
		return Integer.parseInt(row);
	}
	
	/*
	 * This method works as a tool to other classes: it returns the column of
	 * the square passed in as a parameter.
	 */
	public int getCol(String col) {
		char column = Character.toLowerCase(col.charAt(0));
		return column - firstLetter + 1;
	}
	
	/*
	 * This method returns the piece given the piece name passed in
	 * as a string parameter. If the piece cannot be found, null is returned.
	 * @param name: the name of the piece trying to be found
	 * @param white: the color of the piece trying to be found.
	 * @param: position: the position of the piece trying to be found. If the
	 * position is null, then this criteria is disregarded when looking for the
	 * piece.
	 */
	public Piece getPiece(char name, boolean white, Square position) {
		for (Piece p : piecesOnBoard) {
			if (p.name == name && p.isWhite == white) {
				if ((position != null && p.position == position) || position == null) {
					return p;
				}
			}
		}
		// No piece found, return null
		return null;
	}
	
	/*
	 * Helper method used by the board's toString method. If it's black's turn,
	 * we need to reverse each row so it is in the perspective of black. This
	 * method does that.
	 */
	private Square[] reverseRow(Square[] row) {
		Square [] reversedRow = new Square[numColumns];
		for (int i = 0; i < numColumns / 2; i++) {
			Square last = row[numColumns - 1 - i];
			reversedRow[numColumns - 1 - i] = row[i];
			reversedRow[i] = last;
			
		}
		return reversedRow;
	}
	
	@Override
	/*
	 * This method builds the entire board that is then displayed to each user.
	 * The orientation of the board depends on the user: if white is playing, 
	 * the board will be shown from a white perspective, and if black is playing,
	 * the board will be shown from a black perspective.
	 */
	public String toString() {
		StringBuilder s = new StringBuilder();
		int r = masterWhiteTurn ? numRows : 1;
		int count = 0;
		
		while (masterWhiteTurn ? r > 0 : r <= board.length) {
			if (count > 0) {
				s.append("    -——————————————————————-—————————————————\n");
			}
			Square row [] = board[numRows-r];
			// We need to reverse the row if it's black perspective
			if (!masterWhiteTurn) {
				row = reverseRow(row);
			}
			s.append(r + "   |");
			for (int i = 0; i < row.length; i++) {
				s.append(row[i].toString() + "|");
			}
			r = masterWhiteTurn ? --r : ++r;
			s.append("\n");
			count++;
		}
		s.append("\n");
		String names = "       A    B    C    D    E    F    G    H \n";
		if (!masterWhiteTurn) {
			names = "       H    G    F    E    D    C    B    A \n";
		}
		s.append(names);
		return s.toString();
	}
}