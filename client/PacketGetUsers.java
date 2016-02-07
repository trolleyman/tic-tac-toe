package client;

import shared.Instruction;
import shared.Username;
import shared.packet.Packet;

public class PacketGetUsers extends Packet {
	public PacketGetUsers(Packet p) {
		super(p);
	}
	public PacketGetUsers(Username from, Username to) {
		super(Instruction.GET_USERS, from, to);
	}
}
