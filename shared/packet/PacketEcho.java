package shared.packet;

import shared.Instruction;
import shared.Username;

public class PacketEcho extends Packet {
	public PacketEcho(Packet p) {
		super(p);
	}
	public PacketEcho(Username from, Username to, byte[] payload) {
		super(Instruction.ECHO, from, to, payload);
	}
	public boolean payloadEquals(byte[] bytes) {
		if (payload.length != bytes.length)
			return false;
		
		for (int i = 0; i < payload.length; i++) {
			if (payload[i] != bytes[i]) {
				return false;
			}
		}
		
		return true;
	}
}
