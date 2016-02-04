package client;

import shared.UserInfo;

public class Game {
	private UserInfo info;
	
	private boolean finished = false;
	private boolean won = false;
	private String nick;
	private String opponent;
	
	// Starts a new game with opponent on the server
	public Game(UserInfo info) {
		this.info = info;
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
