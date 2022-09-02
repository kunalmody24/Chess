import java.util.ArrayList;
public class BlackPawn extends Piece{
	
	private boolean hasMoved;
	private int maxMoves;
	
	public BlackPawn(char name, boolean isWhite, String pos) {
		super(name, isWhite, pos);
		maxMoves = firstMovePawnMax;
	}
	
	@Override
	/*
	 * This method does all the heavy-lifting to see if the black pawn in
	 * question can make the move passed in as a parameter.
	 */
	public boolean isValidMove(String move) {
		// up move
		if (position.name.charAt(1) - move.charAt(1) <= 0) {
			return false;
		}
		// horizontal move
		boolean moveDiagonal = false;
		if (move.charAt(0) - position.name.charAt(0) != 0) {
			// moving more than one space over
			if (Math.abs(move.charAt(0) - position.name.charAt(0)) > 1) {
				return false;
			}
			// not moving down with the horizontal move (diagonal move)
			else if (Math.abs(move.charAt(1) - position.name.charAt(1)) != 1) {
				return false;
			}
			moveDiagonal = true;
		}
	
		// vertical move more than allowed
		if (position.name.charAt(1) - move.charAt(1) > maxMoves) {
			return false;
		}
		
		// Check to see if it's a vertical move and the space is empty. Let's
		// just call checkMove to do the rest of the work if it is.
		if (move.charAt(0) == position.name.charAt(0) && board.getSquare(move).occupiedBy == null) {
			if (move.contains("1") || (Chess_Engine.readingPGNFile && Chess_Engine.pawnPromotion)) {
				return checkForPawnPromotion(move);
			}
			else if (checkMove(move)) {
				if (!hasMoved && board.realMove) {
					// If this pawn is moving 2 tiles, it becomes the enPassantPawn
					// The move is already valid here.
					if (position.name.charAt(1) - move.charAt(1) == 2) {
						board.enPassantPawn = this;
					}
					hasMoved = true;
					maxMoves = 1;
				}
				return true;
			}
			return false;
		}
		
		Square getnewPos = board.getSquare(move);
		// Check for removal of piece
		if (moveDiagonal && getnewPos.occupiedBy != null) {
			// Is the piece being removed the king?
			if (getnewPos.occupiedBy instanceof King) {
				// It is. Return true without actually removing it. This is a
				// check
				if (board.realMove) {
					hasMoved = true;
					maxMoves = 1;
				}
				return true;
			}
			// Not trying to remove the king. Another piece is being removed.
			// Check for legality
			else if (checkForRemoving(getnewPos.occupiedBy)) {
				if (board.realMove) {
					hasMoved = true;
					maxMoves = 1;
				}
				// Are we trying to promote as well?
				if (move.contains("1") || (Chess_Engine.readingPGNFile && Chess_Engine.pawnPromotion)) {
					return checkForPawnPromotion(move);
				}
				// Here, removing a piece is a legal move. Return true
				return true;
			}
			// Here, removing a piece was not a legal move. Return false
		}
		// enPassant case
		else if (moveDiagonal) {
			// Grab the square where the enPassant pawn should be
			String possibleEnPassant = move.charAt(0) + "" + (Integer.parseInt(move.charAt(1) + "") + 1);
			Square enPassant = board.getSquare(possibleEnPassant);
			// Does the piece in that square equal the current enPassant pawn?
			if (enPassant.occupiedBy != null && enPassant.occupiedBy.equals(board.enPassantPawn)) {
				// It does, call the remove method
				return checkForRemoving(enPassant.occupiedBy);
			}
			// It did not. Return false
		}
		// Here, the move did not make it through any of the legal moves for a
		// pawn. Return false.
		return false;
	}
	
	private boolean checkForPawnPromotion(String move) {
		// The move is already valid, so no need to check for legality
		if (move.charAt(1) == '1') {
			// Here, a black pawn has reached the the opposite end of the board
			// Let's promote it to a queen.
			// First, let's add a queen to the board. The position will be where
			// the pawn is trying to move to.
			removePiece();
			new Queen(queenName, !WHITE, move);
			Chess_Engine.pawnPromotion = true;
			return true;
		}
		return false;
	}
	
	@Override
	/*
	 * This method updates the calling object (blackPawn) on the board to see if 
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
	 * This method creates a list of the squares from the calling object's
	 * position to the position passed in as a parameter.
	 */
	public ArrayList<Square> grabAllSquares(Square position) {
		ArrayList<Square> squares = new ArrayList<>();
		squares.add(this.position);
		if (this.position.name.charAt(1) - position.name.charAt(1) == 2 && maxMoves == 2) {
			String midSquare = position.name.charAt(0) + "" + (Integer.parseInt(position.name.charAt(1) + "") + 1);
			squares.add(board.getSquare(midSquare));
		}
		return squares;
	}
	
	@Override
	/*
	 * This method returns what a black pawn would be displayed as on the board.
	 */
	public String toString() {
		return " BP ";
	}
}