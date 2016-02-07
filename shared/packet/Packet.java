package shared.packet;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import shared.Instruction;
import shared.exception.ProtocolException;
import shared.exception.UnknownInstructionException;

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
	
	public Packet(InputStream in) throws IOException, UnknownInstructionException {
		byte[] bs = new byte[3];
		if (in.read(bs) == -1) {
			throw new EOFException("Input Stream has closed.");
		}
		ByteBuffer buf = ByteBuffer.wrap(bs);
		buf.order(ByteOrder.BIG_ENDIAN);
		ins = readInstruction(buf);
		int payloadLen = readUShort(buf);
		payload = new byte[payloadLen];
		int read = in.read(payload);
		if (read != payloadLen) {
			throw new EOFException("Packet (" + ins + ") did not have the correct length (got "
					+ payloadLen + ", expected " + read + ")");
		}
	}
	public Packet(byte[] in) throws IOException, UnknownInstructionException {
		this(new ByteArrayInputStream(in));
	}
	
	public static Packet readPacket(InputStream in) throws IOException, ProtocolException {
		Packet p = null;
		while (p == null || p.getInstruction() == Instruction.WAIT) {
			// Loop until a packet other than WAIT is read
			p = new Packet(in);
		}
		return downcastPacket(p);
	}
	
	public static Packet downcastPacket(Packet p) throws EOFException, ProtocolException {
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
		case SET_USER:
			return new PacketSetUser(p);
		case REQUEST_JOIN:
			return new PacketRequestJoin(p);
		case ACCEPT_JOIN_REQUST:
		case REJECT_JOIN_REQUEST:
		case QUIT:
			return p;
		
		default:
			return p;
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
	
	private static Instruction readInstruction(ByteBuffer buf) throws EOFException, UnknownInstructionException {
		try {
			int i = buf.get();
			if (i < values.length) {
				return values[i];
			} else {
				throw new UnknownInstructionException(i);
			}
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}
	
	private static int readUShort(ByteBuffer buf) throws EOFException {
		try {
			return buf.getShort() & 0xFFFF;
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}
	
	public Instruction getInstruction() {
		return ins;
	}
	public byte[] getPayload() {
		return payload;
	}
	
	
}
