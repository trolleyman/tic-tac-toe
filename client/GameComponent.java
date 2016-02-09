package client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import shared.GameState;

@SuppressWarnings("serial")
public class GameComponent extends JComponent implements GameListener {
	private GameState state;
	public GameComponent(Game g) {
		state = g.getGameState();
		g.addGameListener(this);
		//setSize(400, 400);
		setPreferredSize(new Dimension(400, 400));
		setMinimumSize(new Dimension(400, 400));
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int size = Math.min(getWidth(), getHeight());
		int offset = 10;
		int tileSize = size / GameState.BOARD_SIZE;
		int xOrigin = 0;//getX();
		int yOrigin = 0;//getY();
		
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
		state = newState;
		update();
	}

	@Override
	public void gameEnded(boolean won) {
		
	}
}
