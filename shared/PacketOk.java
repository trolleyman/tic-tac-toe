package shared;

public class PacketOk extends PacketResult {
	public PacketOk() {
		super(Instruction.OK);
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
