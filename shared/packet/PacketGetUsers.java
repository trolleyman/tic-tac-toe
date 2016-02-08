package shared.packet;

import shared.Instruction;
import shared.Username;

public class PacketGetUsers extends Packet {
	public PacketGetUsers(Packet p) {
		super(p);
	}
	public PacketGetUsers(Username from, Username to) {
		super(Instruction.GET_USERS, from, to);
	}
}
