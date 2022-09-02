public class Square {
	
	public final String name;
	public Piece occupiedBy;
	
	public Square (String n) {
		name = n;
		occupiedBy = null;
	}
	
	/*
	 * This method updates squares involved after a move. The old square's
	 * occupiedBy instance var is changed to null and the new square's occupiedBy
	 * instance var is changed to the piece that moved passed in as a parameter.
	 */
	public void changePieceOccupied(Piece piece, Square oldPos) {
		occupiedBy = piece;
		if (oldPos != null) {
			oldPos.occupiedBy = null;
		}
		piece.position = this;
	}
	
	/*
	 * If the square's occupiedBy instance var is null, no piece is on that
	 * square: return what the user sees as an empty square. If not, return
	 * what the user sees as the piece occupying the square.
	 */
	public String toString() {
		return occupiedBy == null ? " -- " : occupiedBy.toString();
	}
}