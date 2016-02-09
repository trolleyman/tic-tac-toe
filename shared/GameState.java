package shared;

public class GameState {
	static final public int BOARD_SIZE = 3;
	static final public int EMPTY = 0;
	static final public int CROSS = 1;
	static final public int NOUGHT = 2;
	
	// 0 - ' ' or
	// 1 - 'X' or
	// 2 - 'O'
	private int[] board;
	
	public GameState() {
		board = new int[BOARD_SIZE * BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++)
			board[i] = EMPTY;
	}
	private GameState(int[] board) {
		this.board = board;
	}
	
	public int getState(int x, int y) {
		if (x >= BOARD_SIZE || y == BOARD_SIZE) {
			throw new IndexOutOfBoundsException();
		}
		return board[y * BOARD_SIZE + x];
	}
	
	public void setState(int x, int y, int c) {
		if (c == EMPTY || c == CROSS || c == NOUGHT) {
			board[y * BOARD_SIZE + x] = c;
		} else {
			throw new IllegalArgumentException(c + " not allowed.");
		}
	}
	
	@Override
	public GameState clone() {
		return new GameState(board.clone());
	}
	public boolean hasWon(int piece) {
		int p = won();
		if (p == piece) {
			return true;
		}
		return false;
	}
	
	private int won() {
		for (int i = 0; i < BOARD_SIZE; i++) {
			int p = wonRow(i);
			if (p != -1) {
				return p;
			}
			p = wonCol(i);
			if (p != -1) {
				return p;
			}
		}
		int p = wonDiag();
		if (p != -1) {
			return p;
		}
		return GameState.EMPTY;
	}
	private int wonRow(int row) {
		int p = getState(0, row);
		for (int x = 1; x < BOARD_SIZE; x++) {
			if (getState(x, row) != p) {
				return -1;
			}
		}
		return p;
	}
	private int wonCol(int col) {
		int p = getState(col, 0);
		for (int y = 1; y < BOARD_SIZE; y++) {
			if (getState(col, y) != p) {
				return -1;
			}
		}
		return p;
	}
	private int wonDiag() {
		int p1 = getState(0, 0);
		boolean won = true;
		for (int i = 1; i < BOARD_SIZE; i++) {
			if (getState(i, i) != p1) {
				won = false;
			}
		}
		if (won)
			return p1;
		won = true;
		int p2 = getState(0, BOARD_SIZE - 1);
		for (int i = 1; i < BOARD_SIZE; i++) {
			if (getState(i, BOARD_SIZE - i - 1) != p2) {
				won = false;
			}
		}
		if (won)
			return p2;
		return -1;
	}
	
	public boolean isFinished() {
		int p = won();
		return p == GameState.CROSS || p == GameState.NOUGHT;
	}
}
