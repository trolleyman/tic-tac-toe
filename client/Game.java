package client;

import java.awt.Point;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import shared.GameStart;
import shared.GameState;
import shared.Username;
import shared.Util;
import shared.exception.IllegalInstructionException;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketErr;
import shared.packet.PacketMove;
import shared.packet.PacketStart;

public class Game {
	private Random rng;
	
	private volatile GameState state;
	private Socket sock;
	private boolean requestedJoin;
	
	private boolean first;
	private boolean cross;
	/**
	 * The piece that is used by this player in the game.
	 */
	private int piece;
	/**
	 * The piece that is used by the other player in the game.
	 */
	private int oppPiece;
	/**
	 * If it is my turn to move
	 */
	private volatile boolean turn;
	
	private volatile Object wait;
	private volatile Point lastMove;
	
	private Username me;
	private Username opp;

	private ArrayList<GameListener> listeners;
	
	public Game(Socket sock, boolean requestedJoin, Username me, Username opp)
			throws IOException, ProtocolException {
		wait = new Object();
		rng = new Random();
		this.me = me;
		this.opp = opp;
		this.requestedJoin = requestedJoin;
		this.sock = sock;
		listeners = new ArrayList<>();
		lastMove = new Point();
		
		Util.println("Starting game: " + me + " vs " + opp);
		
		connect();
		
		// When state changes, update the listeners.
		updateListeners();
	}
	
	private void connect() throws IOException, ProtocolException {
		GameStart start = null;
		if (!requestedJoin) {
			// Send game start info.
			first = rng.nextBoolean();
			cross = rng.nextBoolean();
			state = new GameState();
			start = new GameStart(state, !first, !cross);
			Util.debug("Sending game start info...");
			(new PacketStart(me, opp, start)).send(sock.getOutputStream());
		} else {
			// Recieve game start info.
			Util.debug("Recieving game start info...");
			Packet p = Packet.readPacket(sock.getInputStream());
			if (!(p instanceof PacketStart)) {
				Util.debugTrace("Expected PacketStart, got " + p);
				throw new IllegalInstructionException(p.getInstruction());
			}
			start = ((PacketStart) p).getStart();
			state = start.initState;
			first = start.first;
			cross = start.cross;
		}
		turn = first;
		if (cross) {
			piece = GameState.CROSS;
			oppPiece = GameState.NOUGHT;
		} else {
			piece = GameState.NOUGHT;
			oppPiece = GameState.CROSS;
		}
	}
	
	public void run() throws IOException, ProtocolException {
		while (!state.isFinished()) {
			if (turn) {
				// Wait for update
				try {
					synchronized (wait) {
						Util.debugTrace("Waiting for event.");
						wait.wait();
					}
				} catch (InterruptedException e) {
					
				}
				// Send update
				Util.debugTrace("Sending new state...");
				(new PacketMove(me, opp, lastMove.x, lastMove.y)).send(sock.getOutputStream());
				turn = !turn;
			} else {
				// Recieve update
				Util.debugTrace("Recieving move...");
				Packet p = Packet.readPacket(sock.getInputStream());
				if (!(p instanceof PacketMove)) {
					throw new IllegalInstructionException(p.getInstruction());
				}
				int x = ((PacketMove) p).getX();
				int y = ((PacketMove) p).getY();
				Util.debugTrace("Got move: " + x + ", " + y);
				if (state.getState(x, y) != GameState.EMPTY) {
					(new PacketErr(me, opp, "Pos at " + x + ", " + y + " is not empty")).send(sock.getOutputStream());
					Util.debugTrace("Error: pos is not empty.");
				}
				state.setState(x, y, oppPiece);
				turn = !turn;
			}
			
			for (GameListener l : listeners) {
				l.gameStateChanged(state);
			}
		}
		for (GameListener l : listeners) {
			l.gameEnded(state.hasWon(piece));
		}
	}
	
	/**
	 * Process a move at x, y
	 * @param x
	 * @param y
	 * @return true if moved, false if illegal move.
	 */
	public boolean move(int x, int y) {
		synchronized (this) {
			if (!turn) {
				// Ignore
				return false;
			}
			if (state.getState(x, y) != GameState.EMPTY) {
				// Ignore
				return false;
			}
			lastMove.x = x;
			lastMove.y = y;
			state.setState(x, y, piece);
			Util.debugTrace("Notifying main thread...");
			synchronized (wait) {
				wait.notifyAll();
			}
			Util.debugTrace("Notified.");
			return true;
		}
	}
	
	public boolean isTurn() {
		return turn;
	}
	
	public GameState getGameState() {
		return state;
	}
	
	private void updateListeners() {
		for (GameListener l : listeners) {
			l.gameStateChanged(state);
		}
	}
	
	public void addGameListener(GameListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public Username getMe() {
		return me;
	}
	public Username getOpponent() {
		return opp;
	}
}
