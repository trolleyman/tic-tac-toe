package shared.packet;

import shared.Instruction;
import shared.Username;
import shared.Util;

public class PacketErr extends PacketResult {
	private final String error;
	
	public PacketErr(Username from, Username to) {
		super(Instruction.ERR, from, to);
		error = null;
	}
	public PacketErr(Username from, Username to, String error) {
		super(Instruction.ERR, from, to);
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
