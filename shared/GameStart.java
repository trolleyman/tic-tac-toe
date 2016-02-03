package shared;

public class GameStart {
	/**
	 * Initial state of the game
	 */
	public GameState initState;
	/**
	 * Is the reciever of this data the first player to play?
	 */
	public boolean first;
	/**
	 * Is the reciever of the data the crosses?
	 */
	public boolean cross;
	
	public GameStart(GameState initState, boolean first, boolean cross) {
		this.initState = initState;
		this.first = first;
		this.cross = cross;
	}
	
	public void invert() {
		this.first = !this.first;
		this.cross = !this.cross;
	}
	
	public GameStart clone() {
		return new GameStart(initState.clone(), first, cross);
	}
}
