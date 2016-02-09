package shared.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import shared.GameStart;
import shared.GameState;
import shared.Instruction;
import shared.Username;
import shared.exception.ProtocolException;

public class PacketStart extends Packet {
	
	private GameStart start;
	
	public PacketStart(Packet p) throws ProtocolException, EOFException {
		super(p);
		try {
			ByteBuffer buf = ByteBuffer.wrap(payload);
			int n = buf.get() & 0xFF; // nxn sized tic-tac-toe. Should be GameState.GAME_SIZE
			if (n != GameState.BOARD_SIZE) {
				throw new ProtocolException("Wrong board size: " + n);
			}
			
			GameState state = new GameState();
			for (int y = 0; y < n; y++)
				for (int x = 0; x < n; x++)
					state.setState(x, y, buf.get() & 0xFF);
			
			boolean first = buf.get() == 0 ? false : true;
			boolean cross = buf.get() == 0 ? false : true;
			start = new GameStart(state, first, cross);
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}
	public PacketStart(Username from, Username to, GameStart start) {
		super(Instruction.START, from, to);
		this.start = start;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
			GameState.BOARD_SIZE * GameState.BOARD_SIZE * Short.BYTES + Short.BYTES + 2);
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeByte(GameState.BOARD_SIZE);
			for (int y = 0; y < GameState.BOARD_SIZE; y++)
				for (int x = 0; x < GameState.BOARD_SIZE; x++)
					os.writeByte(start.initState.getState(x, y));
			
			os.writeByte(start.first ? 1 : 0);
			os.writeByte(start.cross ? 1 : 0);
			os.flush();
		} catch (IOException e) {
			// There is no way this can happen. Probably.
		}
		payload = baos.toByteArray();
	}
	
	public GameStart getStart() {
		return start;
	}
}
