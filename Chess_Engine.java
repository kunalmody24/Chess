import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
public class Chess_Engine implements chessInterface {
	
	private final static int whitePawnPosition = 2;
	private final static int blackPawnPosition = 7;
	private static ArrayList<Piece> whitePieces;
	private static ArrayList<Piece> blackPieces;
	private static double whiteTimeTotal;
	private static double blackTimeTotal;
	public static Chess_GUI mainGUI;
	public static boolean readingPGNFile;
	public static boolean pawnPromotion;
	public static int castlingMove;
	
	public static void main(String args[]) {
		whitePieces = whiteSetUp();
		blackPieces = blackSetUp();
		System.out.println(whitePieces);
		System.out.println(blackPieces);
		mainGUI = new Chess_GUI();
		//board.changed(null);
		readingPGNFile = false;
		// Only for randomized play
		
		// All setup for the start of a game occur here.
		// Create the GUI here.
		// Add a method for only reading PGN files here.
		// Reseting fields may also occur here.
	}
	
	private static void readPGNFile(File file) {
		try {
			Scanner readingFile = new Scanner(file);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Piece> whiteSetUp() {
		whiteTimeTotal = 0;
		ArrayList<Piece> white = new ArrayList<>();
		white.add(new Rook(rookName, WHITE, "a1"));
		white.add(new Rook(rookName, WHITE,"h1"));
		
		white.add(new Bishop(bishopName, WHITE, "c1"));
		white.add(new Bishop(bishopName, WHITE, "f1"));
		
		white.add(new Queen(queenName, WHITE, "d1"));
		
		char letter = firstLetter;
		for (int i = 0; i < numColumns; i++) {
			String pos = letter + "" + whitePawnPosition;
			white.add(new WhitePawn(pawnName, WHITE, pos));
			letter = (char) (letter + 1);
		}
		
		white.add(new Knight(knightName, WHITE, "b1"));
		white.add(new Knight(knightName, WHITE, "g1"));
		
		white.add(new King(kingName, WHITE, "e1"));
		return white;
	}
	
	public static ArrayList<Piece> blackSetUp() {
		blackTimeTotal = 0;
		ArrayList<Piece> black = new ArrayList<>();
		black.add(new Rook(rookName, !WHITE, "a8"));
		black.add(new Rook(rookName, !WHITE,"h8"));
		
		black.add(new Bishop(bishopName, !WHITE, "c8"));
		black.add(new Bishop(bishopName, !WHITE, "f8"));
		
		black.add(new Queen(queenName, !WHITE, "d8"));
		
		char letter = firstLetter;
		for (int i = 0; i < numColumns; i++) {
			String pos = letter + "" + blackPawnPosition;
			black.add(new BlackPawn(pawnName, !WHITE, pos));
			letter = (char) (letter + 1);
		}
		
		black.add(new Knight(knightName, !WHITE, "b8"));
		black.add(new Knight(knightName, !WHITE, "g8"));
		
		black.add(new King(kingName, !WHITE, "e8"));
		return black;
	}
	
	public static ArrayList<Piece> getWhite() {
		return whitePieces;
	}
	
	public static ArrayList<Piece> getBlack() {
		return blackPieces;
	}
}