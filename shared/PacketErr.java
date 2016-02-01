package shared;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketErr extends PacketResult {
	private final Integer errorCode;
	
	public PacketErr() {
		super(Instruction.ERR);
		errorCode = null;
	}
	public PacketErr(Packet p) {
		super(p);
		
		int i = 0;
		try {
			ByteBuffer buf = ByteBuffer.wrap(payload);
			buf.order(ByteOrder.BIG_ENDIAN);
			i = buf.getInt();
		} catch (BufferUnderflowException e) {
			errorCode = null;
			return;
		}
		errorCode = i;
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
	 * Returns the error code associated with the packet
	 * @return Can be null, or even 0!
	 */
	public final Integer getError() {
		return errorCode;
	}
}
