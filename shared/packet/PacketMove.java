package shared.packet;

import java.io.EOFException;

import shared.Instruction;
import shared.Username;

public class PacketMove extends Packet {
	private int x;
	private int y;
	
	public PacketMove(Packet p) throws EOFException {
		super(p);
		
		// Parse arguments
		if (payload.length < 3) {
			throw new EOFException();
		}
		x = payload[0];
		y = payload[1];
	}
	
	public PacketMove(Username from, Username to, int x, int y) {
		super(Instruction.MOVE, from, to);
		this.x = x;
		this.y = y;
		payload = new byte[3];
		payload[0] = (byte)x;
		payload[1] = (byte)y;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
