package shared.packet;

import shared.Instruction;

public class PacketGetUsers extends Packet {
	public PacketGetUsers() {
		super(Instruction.GET_USERS);
	}
	public PacketGetUsers(Packet p) {
		super(p);
	}
}
