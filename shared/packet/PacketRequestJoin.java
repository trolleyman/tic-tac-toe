package shared.packet;

import shared.Instruction;
import shared.Username;
import shared.exception.InvalidUsernameException;

public class PacketRequestJoin extends Packet {
	public PacketRequestJoin(Packet p) throws InvalidUsernameException {
		super(p);
	}
	
	public PacketRequestJoin(Username from, Username to) throws InvalidUsernameException {
		super(Instruction.REQUEST_JOIN, from, to);
	}
}
