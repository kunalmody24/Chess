public interface chessInterface {
	
	// MAGIC NUMBERS
	final static int numRows = 8;
	final static int numColumns = 8;
	final static int sizeOfInput = 3;
	final static int sizeOfLoc = 2;
	final static int secondsInHour = 3600;
	final static int secondsInMinute = 60;
	final static int numPlayers = 2;
	final static int numTimeGauges = 3;
	final static int firstMovePawnMax = 2;
	final static int rowNumberGiven = 57;
	final static char firstLetter = 'a';
	final static char pawnName = '-';
	final static char rookName = 'R';
	final static char knightName = 'N';
	final static char bishopName = 'B';
	final static char queenName = 'Q';
	final static char kingName = 'K';
	final static char removePiece = 'x';
	final static char[] pawnMoveStarts = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', removePiece};
	final static char[] otherPieceMoves = {rookName, knightName, bishopName, queenName, kingName};
	final static String kingSideCastle = "0-0";
	final static String queenSideCastle = "0-0-0";
	final static String pawnPromotion = "=";
	final static String check = "+";
	final static String checkMate = "++";
	final Board board = new Board();
	final static boolean REAL_MOVE = true;
	final static boolean WHITE = true;
}