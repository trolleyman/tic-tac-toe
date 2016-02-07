package shared.packet;

import shared.Instruction;
import shared.Username;

public abstract class PacketResult extends Packet {
	protected PacketResult(Instruction ins, Username from, Username to) {
		super(ins, from, to);
	}
	protected PacketResult(Packet p) {
		super(p);
	}
	
	public abstract boolean isOk();
	public abstract boolean isErr();
}
