package client;

import java.net.InetSocketAddress;

public class Game {
	private InetSocketAddress addr;
	
	private boolean finished = false;
	private boolean won = false;
	private String nick;
	private String opponent;
	
	// Starts a new game with opponent on the server
	public Game(InetSocketAddress addr) {
		this.addr = addr;
	}

	public boolean isFinished() {
		return finished;
	}
	
	public boolean hasWon() {
		return won;
	}
	
	public void step() {
		
	}
}
