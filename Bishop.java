import java.util.ArrayList;
public class Bishop extends Piece {
	
	public Bishop(char name, boolean white, String pos) {
		super(name, white, pos);
	}
	
	@Override
	/*
	 * This implementation of the method is specific to the bishop class: checks
	 * to see if the move attempting to be made is legal, then a call to the
	 * checkMove method in the Piece class is made to see if the move is
	 * actually possible.
	 */
	public boolean isValidMove(String move) {
		Square curr = this.position;
		int factor = Math.abs(curr.name.charAt(0) - move.charAt(0));
		boolean diagonalMove = factor == Math.abs(curr.name.charAt(1) - move.charAt(1));
		if (!diagonalMove) {
			return false;
		}
		
		return checkMove(move);
	}
	
	@Override
	/*
	 * This method updates the calling object (bishop) on the board to see if 
	 * they are in a spot to capture the opposing king.
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
	 * This method creates a list of the squares from the bishop's position to
	 * the position passed in as a parameter.
	 */
	public ArrayList<Square> grabAllSquares(Square position) {
		ArrayList<Square> squares = new ArrayList<>();
		squares.add(this.position);
		boolean pieceFound = this.position.name.equals(position.name);
		Square currPos = this.position;
		boolean isUp = position.name.charAt(1) - currPos.name.charAt(1) > 0;
		boolean isRight = position.name.charAt(0) - currPos.name.charAt(0) > 0;
		int rfactor = isUp ? 1 : -1;
		int cfactor = isRight ? 1 : -1;
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
	 * This method returns what a bishop would be displayed as on the board.
	 */
	public String toString() {
		return isWhite ? " WB " : " BB ";
	}
}