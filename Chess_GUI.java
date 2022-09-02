import javax.imageio.ImageIO;
import javax.swing.*;
import com.google.common.collect.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

@SuppressWarnings("deprecation")
public class Chess_GUI extends Observable implements chessInterface {
	
	private final JFrame gameFrame;
	public final BoardPanel boardPanel;
	
	private Square sourceSquare;
	private Square destSquare;
	private Piece pieceMoved;
	private BoardDirection boardDirection;
	
	private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600,600);
	private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400,350);
	private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10,10);
	private final static String defaultPieceImagePath = "/Users/kunal/eclipse-workspace/Chess/src/";
	private final Color lightTileColor = Color.decode("#FFFACD");
	private final Color darkTileColor = Color.decode("#593E1A");
	
	public Chess_GUI() {
		boardDirection = BoardDirection.NORMAL;
		gameFrame = new JFrame("Chess");
		gameFrame.setLayout(new BorderLayout());
		gameFrame.setSize(OUTER_FRAME_DIMENSION);
		boardPanel = new BoardPanel();
		gameFrame.add(boardPanel, BorderLayout.CENTER);
		//addObserver(new Watcher());
		gameFrame.setVisible(true);
	}
	
	private enum BoardDirection {
		NORMAL {
			@Override
			List<TilePanel> traverse(final List<TilePanel> boardTiles) {
				return boardTiles;
			}
			
			@Override
			BoardDirection opposite() {
				return FLIPPED;
			}
		},
		FLIPPED {
			@Override
			List<TilePanel> traverse(final List<TilePanel> boardTiles) {
				return Lists.reverse(boardTiles);
			}
			
			@Override
			BoardDirection opposite() {
				return NORMAL;
			}
		};
		
		abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
		abstract BoardDirection opposite();
	}
	
	private static class Watcher implements Observer {
		@Override
		public void update(Observable o, Object arg) {
			String player = board.masterWhiteTurn ? "white" : "black";
			if (board.isCheckmate) {
				JOptionPane.showMessageDialog(Chess_Engine.mainGUI.boardPanel, "Game over: Player " + player + " is in checkmate", "Game Over", JOptionPane.INFORMATION_MESSAGE);
			}
			if (board.isStalemate) {
				JOptionPane.showMessageDialog(Chess_Engine.mainGUI.boardPanel, "Game over: Player " + player + " is in stalemate", "Game Over", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	public class BoardPanel extends JPanel {
		final List<TilePanel> boardTiles;
		
		BoardPanel() {
			super(new GridLayout(8,8));
			boardTiles = new ArrayList<>();
			int row = numRows;
			for (int i = 0; i < numRows; i++) {
				char letter = firstLetter;
				for (int j = 0; j < numColumns; j++) {
					String name = letter + "" + row;
					final TilePanel tilePanel = new TilePanel(name);
					boardTiles.add(tilePanel);
					add(tilePanel);
					letter = (char) (letter + 1);
				}
				row--;
			}
			setPreferredSize(BOARD_PANEL_DIMENSION);
		}
		
		public TilePanel getTile(String name) {
			for (TilePanel t : boardTiles) {
				if (t.name.equals(name)) {
					return t;
				}
			}
			return null;
		}

		private void drawBoard() {
			removeAll();
			for (TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
				tilePanel.drawTile();
				add(tilePanel);
			}
			validate();
			repaint();
		}
	}
	
	public class TilePanel extends JPanel {
		private final String name;
		
		TilePanel(String name) {
			super(new GridBagLayout());
			this.name = name;
			setPreferredSize(TILE_PANEL_DIMENSION);
			assignTileColor();
			assignTilePieceIcon();
			
			addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(final MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						sourceSquare = null;
						destSquare = null;
						pieceMoved = null;
					}
					else if (SwingUtilities.isLeftMouseButton(e)) {
						if (sourceSquare == null) {
							sourceSquare = board.getSquare(name);
							pieceMoved = sourceSquare.occupiedBy;
							// Disregard click if no piece on square or if piece
							// is of opposing color.
							if (pieceMoved == null || pieceMoved.isWhite != board.internalWhiteTurn) {
								sourceSquare = null;
							}
						}
						else {
							destSquare = board.getSquare(name);
							// Safety reasons: realMove NEEDS to be true here;
							// subsequent move is by player
							board.realMove = REAL_MOVE;
							if (pieceMoved.makeMove(destSquare.name)) {
								setChanged();
								notifyObservers();
								boardDirection = boardDirection.opposite();
								// Add move to log
							}
							sourceSquare = null;
							destSquare = null;
							pieceMoved = null;
						}
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								boardPanel.drawBoard();
							}
						});
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
		}
		
		public void changed() {
			setChanged();
			notifyObservers();
			boardDirection = boardDirection.opposite();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					boardPanel.drawBoard();
				}
			});
		}
		
		public void drawTile() {
			assignTileColor();
			assignTilePieceIcon();
			highlightTileBorder();
			validate();
			repaint();
		}

		private void assignTilePieceIcon() {
			removeAll();
			if (board.getSquare(name).occupiedBy != null) {
				Piece p = board.getSquare(name).occupiedBy;
				try {
					final BufferedImage image = ImageIO.read(new File(defaultPieceImagePath + p.toString().replaceAll(" ", "") + ".gif"));
					add(new JLabel(new ImageIcon(image)));
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void assignTileColor() {
			int difference = name.charAt(0) - firstLetter;
			int tileId = difference * 8 + Integer.parseInt(name.charAt(1) + "") - 1;
			boolean isLight = ((tileId + tileId / 8) % 2 == 0);
            setBackground(isLight ? lightTileColor : darkTileColor);
		}
		
		private void highlightTileBorder() {
			if (pieceMoved != null && pieceMoved.isWhite == board.masterWhiteTurn 
				&& pieceMoved.position.name.equals(name)) {
				setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
			}
			else {
				setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			}
		}
	}
}