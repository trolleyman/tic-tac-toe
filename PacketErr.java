import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketErr extends PacketResult {
	Integer errorCode;
	
	public PacketErr() {
		super(Instruction.ERR);
		
		try {
			ByteBuffer buf = ByteBuffer.wrap(payload);
			buf.order(ByteOrder.BIG_ENDIAN);
			int i = buf.getInt();
			errorCode = i;
		} catch (BufferUnderflowException e) {
			errorCode = null;
		}
	}
	protected PacketErr(Packet p) {
		super(p);
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
	public Integer getError() {
		return errorCode;
	}
}
