package client;

import shared.GameState;

public interface GameListener {
	public void gameStateChanged(GameState newState);
	public void gameEnded(boolean won);
}
