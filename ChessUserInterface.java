/*
 * This is the class for the user interface. All input is validated here and
 * calls to validating moves occur here as well. Input must be in the form
 * "pieceToMove currentPosition positionToMoveTo" where currentPosition and
 * positionToMoveTo are the simple square names on a chess board. Game setup
 * and pieces for both sides are initialized in their respective methods Times
 * and the number of moves for both sides are also tracked, and displayed in the
 * STATS section at the end of the game. To quit the game, simply input "q".
 */

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
public class ChessUserInterface implements chessInterface {
	
	private final static int whitePawnPosition = 2;
	private final static int blackPawnPosition = 7;
	public static Chess_GUI GUI;
	private static double whiteTimeTotal;
	private static double blackTimeTotal;
	private static int whiteMoves;
	private static int blackMoves;
	private static char ambiguityResolved;
	private static boolean pawnMove;
	private static char pieceMoving;
	private static boolean showStats;
	public static boolean readingPGNFile;
	public static boolean possiblePieceRemoval;
	public static boolean pawnPromotion;
	public static int possibleKingCastle;
	public static boolean checkmateIndicated;
	public static boolean checkIndicated;
	
	public static void main(String args []) {
		whiteSetUp();
		blackSetUp();
		Board.internalWhiteTurn = true;
		board.masterWhiteTurn = true;
		readingPGNFile = false;
		GUI = new Chess_GUI();
		boolean readingInputFromFile = false;
		Scanner in = new Scanner(System.in);
		System.out.println("Do you want to read moves from a file? (y) (n)");
		String answer = in.nextLine();
		if (answer.equals("y")) {
			System.out.println("Enter file name: ");
			String fileName = in.nextLine();
			File fileToRead = new File("/Users/kunal/eclipse-workspace/Chess/src/" + fileName);
			try {
				Scanner readFromFile = new Scanner(fileToRead);
				readingInputFromFile = true;
				String inputCopy = null;
				boolean exitEarly = false;
				readingPGNFile = true;
				while (readFromFile.hasNextLine() && !board.isCheckmate) {
					String input = readFromFile.nextLine();
					inputCopy = input;
					if (!validateInput(input)) {
						exitEarly = true;
						break;
					}
					if (possibleKingCastle == 0) {
						pieceMoving = pawnMove ? pawnName : input.charAt(0);
					}
					if (!initiateMove(input)) {
						exitEarly = true;
						break;
					}
					resetAllFields();
				}
				if (exitEarly) {
					System.out.println(inputCopy + " not a valid move. Stopped reading input from " + fileName + ".");
				}
				readFromFile.close();
			} catch (FileNotFoundException e) {
				System.out.println("File not found.");
			}
		}
		
		if (!readingInputFromFile) {
			System.out.println(board);
		}
		readingPGNFile = false;
		startGame(in);
	}
	
	private static void resetAllFields() {
		pawnMove = false;
		ambiguityResolved = 0;
		possiblePieceRemoval = false;
		pieceMoving = 0;
		possibleKingCastle = 0;
		checkmateIndicated = false;
		checkIndicated = false;
		pawnPromotion = false;
	}
	
	private static void startGame(Scanner in) {
		while (!board.isCheckmate) {
			showStats = true;
			resetAllFields();
			String input = readInput(in);
			if (!initiateMove(input)) {
				System.out.println("MOVE INVALID");
			}
		}
		showStats();
		in.close();
	}
	
	private static String readInput(Scanner in) {
		String playerTurn = board.masterWhiteTurn ? "White" : "Black";
		System.out.println(playerTurn + " player:");
		Stopwatch.start();
		String line = in.nextLine();
		quitGame(line);
		while (!validateInput(line)) {
			quitGame(line);
			System.out.println("INVALID INPUT");
			System.out.println(playerTurn);
			line = in.nextLine();
		}
		if (possibleKingCastle == 0) {
			pieceMoving = pawnMove ? pawnName : line.charAt(0);
		}
		return line;
	}
	
	private static boolean validateInput(String input) {
		// Is the move castling??
		if (input.equals(kingSideCastle)) {
			pieceMoving = kingName;
			possibleKingCastle = 1;
			return true;
		}
		else if (input.equals(queenSideCastle)) {
			pieceMoving = kingName;
			possibleKingCastle = 2;
			return true;
		}
		// Is it a pawn move? Those start with [a,h] or x
		int index = 0;
		while (!pawnMove && index < pawnMoveStarts.length) {
			if (input.charAt(0) == (pawnMoveStarts[index])) {
				pawnMove = true;
			}
			index++;
		}
		// It's not castling, it's not a pawn move, so it can only be another
		// piece moving. Let's check.
		boolean otherPieceMoveFound = false;
		index = 0;
		while (!pawnMove && !otherPieceMoveFound && index < otherPieceMoves.length) {
			if (input.charAt(0) == otherPieceMoves[index]) {
				otherPieceMoveFound = true;
			}
			index++;
		}
		// Move indicating a piece removal?
		if (input.contains(removePiece + "")) {
			possiblePieceRemoval = true;
		}
		// Move indicating a checkmate?
		if (input.contains(checkMate)) {
			checkmateIndicated = true;
		}
		// Move indicating a check?
		if (input.contains(check)) {
			checkIndicated = true;
		}
		// Move indicating a pawn promotion?
		if (input.contains(chessInterface.pawnPromotion)) {
			pawnPromotion = true;
		}
		return pawnMove || otherPieceMoveFound;
	}
	
	private static boolean initiateMove(String input) {
		String move = input.substring(input.length() - 2);
		if (pawnPromotion) {
			move = input.substring(0,2);
		}
		else if (checkmateIndicated) {
			move = input.substring(input.length() - 4, input.length() - 2);
		}
		else if (checkIndicated) {
			move = input.substring(input.length() - 3, input.length() - 1);
		}
		else if (possibleKingCastle != 0) {
			move = input;
		}
		
		// First step: figure out which piece player wants to move (assume all
		// input is already validated)
		int offsetForPawn = pawnMove ? -1 : 0;
		
		// 2 possible cases for removing ambiguity from piece moves
		// If there was no information given. ambiguityResolved = 0
		if (!checkIndicated && possibleKingCastle == 0 && possiblePieceRemoval && input.length() == 5 + offsetForPawn) {
			ambiguityResolved = input.charAt(1 + offsetForPawn);
		}
		else if (!checkIndicated && possibleKingCastle == 0 && !possiblePieceRemoval && input.length() == 4 + offsetForPawn) {
			ambiguityResolved = input.charAt(1 + offsetForPawn);
		}
		// Ambiguity can be resolved in two ways: either extra row info is given
		// or extra column info is given. Depending on the case, we must look at
		// different indexes of a piece's position.
		int indexForAmbiguity = 0;
		if (ambiguityResolved != 0 && ambiguityResolved <= rowNumberGiven) {
			indexForAmbiguity = 1;
		}
		// Figure out which piece the player wants to move. The piece in question
		// must be the same color as the player moving, must get to the specified
		// square legally, and, if there was extra information given for
		// removing ambiguity, must satisfy that too.
		// We are going to iterate through all the pieces and see if any of them
		// can make the move indicated. If more than one piece can or if none of
		// the pieces can, return false.
		Piece toMove = null;
		int piecesFound = 0;
		for (int i = 0; i < board.piecesOnBoard.size(); i++) {
			Piece p = board.piecesOnBoard.get(i);
			String position = p.position.name;
			// Simple check: piece name needs to match name of piece in input
			// and piece also needs to be the same color as the player moving
			boolean correctPiece = p.onBoard && p.name == pieceMoving && p.isWhite == board.masterWhiteTurn;
			// Extra check for removing ambiguity; not necessary in input
			boolean extraCheck = ambiguityResolved != 0 && position.charAt(indexForAmbiguity) == ambiguityResolved;
			if (!p.equals(toMove) && correctPiece && (extraCheck || ambiguityResolved == 0) &&
				p.makeMove(move, !REAL_MOVE)) {
				toMove = p;
				piecesFound++;
			}
		}
		// We found the piece. No need to check for null, input is already
		// validated. We need to check for ambiguity
		if (piecesFound != 1) {
			// Still don't know which piece to move. Ask player to enter input
			// again
			return false;
		}
		
		// restore internal clock back to master clock before making the actual
		// move
		Piece.internalWhiteTurn = board.masterWhiteTurn;
		assert toMove.makeMove(move, REAL_MOVE);
		
		Stopwatch.stop();
		double time = Stopwatch.time();
		if (board.masterWhiteTurn) {
			whiteTimeTotal += time;
			whiteMoves++;
		}
		else {
			blackTimeTotal += time;
			blackMoves++;
		}
		// next player's turn
		board.masterWhiteTurn = !board.masterWhiteTurn;
		// update internal clock
		Piece.internalWhiteTurn = board.masterWhiteTurn;
		System.out.println(board);
		return true;
	}
	
	private static void quitGame(String line) {
		if (line.equals("q")) {
			showStats();
			System.exit(0);
		}
	}
	
	private static void showStats() {
		System.out.println("GAME OVER");
		if (board.isCheckmate) {
			String score = board.masterWhiteTurn ? "0-1" : "1-0";
			System.out.println("Score: " + score);
		}
		if (!showStats) {
			return;
		}
		double totalTimes[] = new double[numPlayers];
		// FIRST INDEX IS FOR WHITE TOTAL, SECOND INDEX IS FOR BLACK TOTAL
		totalTimes[0] = whiteTimeTotal;
		totalTimes[1] = blackTimeTotal;
		
		int separatedTimes [][] = new int[numPlayers][numTimeGauges];
		// FIRST ROW IS FOR WHITE TIMES, SECOND ROW IS FOR BLACK TIMES
		// FIRST COLUMN IS HOURS, SECOND COLUMN IS MINUTES, THIRD COLUMN IS SECONDS
		for (int i = 0; i < numPlayers; i++) {
			for (int j = 0; j < numTimeGauges; j++) {
				separatedTimes[i][j] = (int) (totalTimes[i] / (secondsInHour / Math.pow(secondsInMinute, j)));
				totalTimes[i] -= separatedTimes[i][j] * (secondsInHour / Math.pow(secondsInMinute, j));
			}
		}
		System.out.println("STATS: ");
		System.out.println();
		System.out.println("WHITE: ");
		System.out.println("Time: " + separatedTimes[0][0] + "hrs " + separatedTimes[0][1] + "mins " + separatedTimes[0][2] + "sec");
		System.out.println("Moves: " + whiteMoves);
		System.out.println();
		System.out.println("BLACK: ");
		System.out.println("Time: " + separatedTimes[1][0] + "hrs " + separatedTimes[1][1] + "mins " + separatedTimes[1][2] + "sec");
		System.out.println("Moves: " + blackMoves);
	}
	
	public static void whiteSetUp() {
		whiteTimeTotal = 0;
		board.piecesOnBoard.add(new Rook(rookName, true, "a1"));
		board.piecesOnBoard.add(new Rook(rookName, true,"h1"));
		
		board.piecesOnBoard.add(new Bishop(bishopName, true, "c1"));
		board.piecesOnBoard.add(new Bishop(bishopName, true, "f1"));
		
		board.piecesOnBoard.add(new Queen(queenName, true, "d1"));
		
		char letter = firstLetter;
		for (int i = 0; i < numColumns; i++) {
			String pos = letter + "" + whitePawnPosition;
			board.piecesOnBoard.add(new WhitePawn(pawnName, true, pos));
			letter = (char) (letter + 1);
		}
		
		board.piecesOnBoard.add(new Knight(knightName, true, "b1"));
		board.piecesOnBoard.add(new Knight(knightName, true, "g1"));
		
		board.piecesOnBoard.add(new King(kingName, true, "e1"));
	}
	
	public static void blackSetUp() {
		blackTimeTotal = 0;
		board.piecesOnBoard.add(new Rook(rookName, false, "a8"));
		board.piecesOnBoard.add(new Rook(rookName, false,"h8"));
		
		board.piecesOnBoard.add(new Bishop(bishopName, false, "c8"));
		board.piecesOnBoard.add(new Bishop(bishopName, false, "f8"));
		
		board.piecesOnBoard.add(new Queen(queenName, false, "d8"));
		
		char letter = firstLetter;
		for (int i = 0; i < numColumns; i++) {
			String pos = letter + "" + blackPawnPosition;
			board.piecesOnBoard.add(new BlackPawn(pawnName, false, pos));
			letter = (char) (letter + 1);
		}
		
		board.piecesOnBoard.add(new Knight(knightName, false, "b8"));
		board.piecesOnBoard.add(new Knight(knightName, false, "g8"));
		
		board.piecesOnBoard.add(new King(kingName, false, "e8"));
	}
}