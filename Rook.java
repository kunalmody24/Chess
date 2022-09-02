import java.util.ArrayList;
public class Rook extends Piece {
	
	public boolean hasMoved;
	
	public Rook(char n, boolean white, String start) {
		super(n, white, start);
		hasMoved = false;
	}
	
	@Override
	/*
	 * This implementation of the method is specific to the rook class: checks
	 * to see if the move attempting to be made is a possible legal move, then
	 * a call to the checkMove method in the Piece class is made to see if the
	 * move is actually possible.
	 */
	public boolean isValidMove(String move) {
		boolean horizontalMove = position.name.charAt(1) - move.charAt(1) == 0;
		boolean verticalMove = position.name.charAt(0) - move.charAt(0) == 0;
		if (!horizontalMove && !verticalMove) {
			return false;
		}
		
		if (checkMove(move)) {
			if (board.realMove) {
				hasMoved = true;
			}
			return true;
		}
		return false;
	}
	
	@Override
	/*
	 * This method updates the calling object (rook) on the board to see if they 
	 * are in a spot to capture the opposing king.
	 */
	public void updateCaptureKing() {
		King k = (King) (board.getPiece(kingName, !isWhite, null));
		Square pos = k.position;
		isChecking = false;
		// Hold old value of switch
		boolean temp = board.realMove;
		// Indicate to program move is not real
		board.realMove = !REAL_MOVE;
		// Make move
		if (isValidMove(pos.name)) {
			isChecking = true;
		}
		// Change switch back to old value
		board.realMove = temp;
	}
	
	@Override
	/*
	 * This method creates a list of the squares from the rook's position to
	 * the position passed in as a parameter.
	 */
	public ArrayList<Square> grabAllSquares(Square position) {
		ArrayList<Square> squares = new ArrayList<>();
		squares.add(this.position);
		boolean pieceFound = false;
		Square currPos = this.position;
		
		int rfactor = 0;
		int cfactor = 0;
		try {
			int factor = position.name.charAt(1) - currPos.name.charAt(1);
			rfactor = factor / Math.abs(factor);
		} catch (ArithmeticException e) {
			rfactor = 0;
		}
		
		try {
			int factor = position.name.charAt(0) - currPos.name.charAt(0);
			cfactor = factor / Math.abs(factor);
		} catch (ArithmeticException e) {
			cfactor = 0;
		}
		int row = board.getRow(currPos.name.substring(1));
		int col = board.getCol(currPos.name.substring(0,1));
		while (!pieceFound) {
			row += rfactor;
			col += cfactor;
			currPos = board.board[numRows - row][col - 1];
			if (position.equals(currPos)) {
				pieceFound = true;
			}
			else {
				squares.add(currPos);
			}
		}
		return squares;
	}
	
	@Override
	/*
	 * This method returns what a rook would be displayed as on the board.
	 */
	public String toString() {
		return isWhite ? " WR " : " BR ";
	}
}