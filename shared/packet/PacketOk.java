package shared.packet;

import shared.Instruction;
import shared.Username;

public class PacketOk extends PacketResult {
	public PacketOk(Username from, Username to) {
		super(Instruction.OK, from, to);
	}
	public PacketOk(Packet p) {
		super(p);
	}

	@Override
	public boolean isOk() {
		return true;
	}

	@Override
	public boolean isErr() {
		return false;
	}
}
