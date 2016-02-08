package client;

import java.net.Socket;

import shared.GameState;
import shared.Username;

public class Game {
	private GameState state;
	private Socket sock;
	
	public Game(Socket sock, Username user) {
		System.out.println("Starting game vs " + user);
	}
	
	public GameState getGameState() {
		return state;
	}
}
