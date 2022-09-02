import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class Player implements Observer {
	
	public String name;
	static int count;
	
	public Player (String name) {
		this.name = name;
		count = 0;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (count < 20) {
			// The board has been updated. Next player's turn
			// The arg will tell us whose turn it is
			arg = (String) arg;
			System.out.println(arg);
			ArrayList<Piece> pieces = arg.equals("white") ? Chess_Engine.getWhite() : Chess_Engine.getBlack();
			Random r = new Random();
			Piece piece = null;
			String move = "";
			//System.out.println("Inside player's update (making random move)");
			do {
				// Get random piece
				piece = pieces.get(r.nextInt(pieces.size() - 1));
				// Get random square
				char col = (char) (r.nextInt(8) + 'a');
				int row = r.nextInt(7) + 1;
				move = col + "" + row;
				System.out.println(piece);
//				System.out.println(piece);
//				System.out.println(move);
//				assert false;
			}
			//while (false);
			while (!piece.makeMove(move));
		}
		else {
			System.exit(0);
		}
		
		
		
		
	}

}
