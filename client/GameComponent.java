package client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import shared.GameState;

@SuppressWarnings("serial")
public class GameComponent extends JComponent implements GameListener {
	private GameState state;
	private Game game;
	
	public GameComponent(Game game) {
		this.game = game;
		state = game.getGameState();
		game.addGameListener(this);
		//setSize(400, 400);
		setPreferredSize(new Dimension(400, 400));
		setMinimumSize(new Dimension(400, 400));
		repaint();
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int size = Math.min(getWidth(), getHeight());
					int tileSize = size / GameState.BOARD_SIZE;
					
					int x = e.getX() / tileSize;
					int y = e.getY() / tileSize;
					System.out.println("x:" + x + ", y:" + y);
					if (game.getGameState().getState(x, y) != GameState.EMPTY) {
						JOptionPane.showInternalMessageDialog(
							null, "Error: Non-empty tiles cannot be selected.",
							"Tic-Tac-Toe Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					game.move(x, y);
				}
			}
		});
	}
	
	@Override
	public void paintComponent(Graphics gx) {
		Graphics2D g2 = (Graphics2D) gx;
		int size = Math.min(getWidth(), getHeight());
		int offset = 10;
		int tileSize = size / GameState.BOARD_SIZE;
		int xOrigin = 0;//getX();
		int yOrigin = 0;//getY();
		
		state = game.getGameState();
		for (int yTile = 0; yTile < GameState.BOARD_SIZE; yTile++) {
			for (int xTile = 0; xTile < GameState.BOARD_SIZE; xTile++) {
				int tile = state.getState(xTile, yTile);
				
				if (tile == GameState.CROSS) {
					// Draw cross
					g2.drawLine(xOrigin + offset + tileSize * xTile,
					            yOrigin + offset + tileSize * yTile,
					            xOrigin - offset + tileSize * (xTile + 1),
					            yOrigin - offset + tileSize * (yTile + 1));
					g2.drawLine(xOrigin + offset + tileSize * xTile,
						        yOrigin - offset + tileSize * (yTile + 1),
			                    xOrigin - offset + tileSize * (xTile + 1),
			                    yOrigin + offset + tileSize * yTile);
				} else if (tile == GameState.NOUGHT) {
					// Draw nought
					g2.drawOval(xOrigin + offset + tileSize * xTile,
					            yOrigin + offset + tileSize * yTile,
					            tileSize - offset * 2,
					            tileSize - offset * 2);
				}
			}
		}
		
		for (int n = 0; n <= GameState.BOARD_SIZE; n++) {
			g2.drawLine(xOrigin + tileSize * n, yOrigin,
			            xOrigin + tileSize * n, yOrigin + size);
			g2.drawLine(xOrigin       , yOrigin + tileSize * n,
	                    xOrigin + size, yOrigin + tileSize * n);
		}
	}
	
	public void update() {
		repaint();
	}

	@Override
	public void gameStateChanged(GameState newState) {
		update();
	}

	@Override
	public void gameEnded(boolean won) {
		update();
	}
}
