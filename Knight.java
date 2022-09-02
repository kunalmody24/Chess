import java.util.ArrayList;
public class Knight extends Piece {
	
	private static final int[] VERTICAL = {1,2};
	private static final int[] HORIZONTAL = {2,1};
	
	public Knight(char name, boolean white, String pos) {
		super(name, white, pos);
	}
	
	@Override
	/*
	 * This implementation of the method is specific to the knight class: checks
	 * to see if the move attempting to be made is a possible legal move, then
	 * a call to the checkMove method in the Piece class is made to see if the
	 * move is actually possible.
	 */
	public boolean isValidMove(String move) {
		
		int columnDiff = Math.abs(move.charAt(0) - position.name.charAt(0));
		int rowDiff = Math.abs(move.charAt(1) - position.name.charAt(1));
		if (columnDiff == VERTICAL[0]) {
			if (rowDiff != VERTICAL[1]) {
				return false;
			}
		}
		else if (columnDiff == HORIZONTAL[0]) {
			if (rowDiff != HORIZONTAL[1]) {
				return false;
			}
		}
		else {
			return false;
		}
		
		// Here, we know the move is valid: check if we are trying to remove a piece
		// If not, return true (move is valid anyway), and if we are, call
		// checkForRemoving
		
		Square moveTo = board.getSquare(move);
		if (moveTo.occupiedBy != null) {
			return checkForRemoving(moveTo.occupiedBy);
		}
		return true;
	}
	
	@Override
	/*
	 * This method updates the calling object (knight) on the board to see if 
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
	 * This method creates a list of the squares from the knight's position to
	 * the position passed in as a parameter.
	 */
	public ArrayList<Square> grabAllSquares(Square position) {
		ArrayList<Square> squares = new ArrayList<>();
		squares.add(this.position);
		return squares;
	}
	
	@Override
	/*
	 * This method returns what a knight would be displayed as on the board.
	 */
	public String toString() {
		return isWhite ? " WN " : " BN ";
	}
}