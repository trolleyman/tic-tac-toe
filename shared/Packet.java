package shared;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {
	private static final Instruction[] values = Instruction.values();
	
	protected Instruction ins;
	protected byte[] payload;
	
	protected Packet(Packet p) {
		this(p.ins, p.payload);
	}
	public Packet(Instruction ins) {
		this(ins, new byte[0]);
	}
	public Packet(Instruction ins, byte[] payload) {
		this.ins = ins;
		this.payload = payload;
	}
	
	public Packet(InputStream in) throws IOException {
		byte[] bs = new byte[3];
		in.read(bs);
		ByteBuffer buf = ByteBuffer.wrap(bs);
		buf.order(ByteOrder.BIG_ENDIAN);
		ins = readInstruction(buf);
		int payloadLen = readUShort(buf);
		payload = new byte[payloadLen];
		if (in.read(payload) != payloadLen) {
			throw new TTTProtocolException("Unexpected end of stream");
		}
	}
	public Packet(byte[] in) throws IOException {
		this(new ByteArrayInputStream(in));
	}
	
	public static Packet readPacket(InputStream in) throws IOException {
		Packet p = new Packet(in);
		return downcastPacket(p);
	}
	
	public static Packet downcastPacket(Packet p) throws TTTProtocolException {
		Instruction i = p.getInstruction();
		switch (i) {
		case OK:
			return new PacketOk(p);
		case ERR:
			return new PacketErr(p);
		case GET_USERS:
			return new PacketGetUsers(p);
		case PUT_USERS:
			return new PacketPutUsers(p);
		case QUIT:
			return p;
		case SET_NICK:
			return new PacketSetNick(p);
		default:
			throw new TTTProtocolException("Illegal instruction: " + i);
		}
	}
	
	public void write(OutputStream out) throws IOException {
		out.write(toByteArray());
	}
	
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(3 + payload.length);
		buf.order(ByteOrder.BIG_ENDIAN);
		buf.put((byte) ins.ordinal());
		buf.putShort((short) payload.length);
		buf.put(payload);
		return buf.array();
	}
	
	private static Instruction readInstruction(ByteBuffer buf) throws TTTProtocolException {
		try {
			int i = buf.get();
			if (i < values.length) {
				return values[i];
			} else {
				throw new TTTProtocolException("Unknown instruction: " + i);
			}
		} catch (BufferUnderflowException e) {
			throw new TTTProtocolException("Unexpected end of stream");
		}
	}
	
	private static int readUShort(ByteBuffer buf) throws TTTProtocolException {
		try {
			return buf.getShort() & 0xFFFF;
		} catch (BufferUnderflowException e) {
			throw new TTTProtocolException("Unexpected end of stream");
		}
	}
	
	public Instruction getInstruction() {
		return ins;
	}
	public byte[] getPayload() {
		return payload;
	}
	
	
}
