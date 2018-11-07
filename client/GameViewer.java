package client;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import shared.GameState;

public class GameViewer implements GameListener {
	
	private volatile boolean systemExit = true;
	private volatile Game   g;
	private volatile JFrame frame;
	private volatile JLabel gameLabel;
	private volatile String base;

	public GameViewer(Game g, Point center) {
		this.g = g;
		frame = new JFrame("Tic-Tac-Toe Game");
		
		g.addGameListener(this);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				if (systemExit)
					System.exit(0);
			}
			
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}
		});
		
		frame.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(true);
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		panel.add(new GameComponent(g));
		
		base = "Tic-Tac-Toe: " + g.getMe() + " vs " + g.getOpponent();
		gameLabel = new JLabel(base);
		gameLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
		frame.add(gameLabel, BorderLayout.NORTH);
		frame.add(panel, BorderLayout.CENTER);
		
		frame.pack();
	    frame.setLocation(center.x - frame.getWidth() / 2, center.y - frame.getHeight() / 2);
	    update();
		frame.setVisible(true);
	}
	
	public void close() {
		systemExit  = false;
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
	
	public void update() {
		if (g.isTurn()) {
			gameLabel.setText(base + ": " + "Please select a position.");
		} else {
			gameLabel.setText(base + ": " + g.getOpponent() + " is choosing a position...");
		}
	}

	@Override
	public void gameStateChanged(GameState s) {
		update();
	}

	@Override
	public void gameEnded(boolean won) {
		if (won) {
			JOptionPane.showMessageDialog(
				null, "You won tic-tac-toe against " + g.getOpponent() + "!",
				"Congratulations!", JOptionPane.PLAIN_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(
				null, "You lost tic-tac-toe against " + g.getOpponent() + "...",
				"Comiserations...", JOptionPane.PLAIN_MESSAGE);
		}
	}
}
