package shared;
public abstract class PacketResult extends Packet {
	protected PacketResult(Instruction ins) {
		super(ins);
	}
	protected PacketResult(Packet p) {
		super(p);
	}
	
	public abstract boolean isOk();
	public abstract boolean isErr();
}
