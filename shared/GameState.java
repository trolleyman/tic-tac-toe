package shared;

public class GameState {
	static final public int BOARD_SIZE = 3;
	static final public char[] VALID_CHARS = new char[] {' ', 'X', 'O'};
	
	// ' ' or
	// 'X' or
	// 'O'
	private char[] board;
	
	public GameState() {
		board = new char[BOARD_SIZE * BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)
			board[i] = ' ';
	}
	private GameState(char[] board) {
		this.board = board;
	}
	
	public char getState(int x, int y) {
		if (x >= BOARD_SIZE || y == BOARD_SIZE) {
			throw new IndexOutOfBoundsException();
		}
		return board[y * BOARD_SIZE + x];
	}
	
	public void setState(int x, int y, char c) {
		if (c == ' ' || c == 'X' || c == 'x' || c == 'O' || c == 'o') {
			c = Character.toUpperCase(c);
			board[y * BOARD_SIZE + x] = c;
		} else {
			throw new IllegalArgumentException(c + " not allowed.");
		}
	}
	
	public static boolean isValidChar(char c) {
		for (int i = 0; i < VALID_CHARS.length; i++) {
			if (VALID_CHARS[i] == c) {
				return true;
			}
		}
		return false;
	}
	
	public GameState clone() {
		return new GameState(board.clone());
	}
}
