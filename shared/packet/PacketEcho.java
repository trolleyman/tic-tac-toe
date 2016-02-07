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
}
