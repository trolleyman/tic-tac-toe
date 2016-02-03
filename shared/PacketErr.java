package shared;

public class PacketErr extends PacketResult {
	private final String error;
	
	public PacketErr() {
		super(Instruction.ERR);
		error = null;
	}
	public PacketErr(String error) {
		super(Instruction.ERR);
		this.error = error;
		this.payload = Util.utf8EncodeReplace(error);
	}
	
	public PacketErr(Packet p) {
		super(p);
		
		error = Util.utf8DecodeReplace(payload);
	}

	@Override
	public boolean isOk() {
		return false;
	}

	@Override
	public boolean isErr() {
		return true;
	}
	
	/**
	 * Returns the error associated with the packet
	 */
	public final String getError() {
		return error;
	}
}
