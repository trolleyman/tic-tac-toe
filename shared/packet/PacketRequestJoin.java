package shared.packet;

import shared.Instruction;
import shared.Username;

public class PacketRequestJoin extends Packet {
	public PacketRequestJoin(Packet p) {
		super(p);
	}
	
	public PacketRequestJoin(Username from, Username to) {
		super(Instruction.REQUEST_JOIN, from, to);
	}
}
