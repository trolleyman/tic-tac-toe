package shared.packet;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import shared.Instruction;
import shared.Username;
import shared.Util;
import shared.exception.ProtocolException;
import shared.exception.UnknownInstructionException;

public class Packet {
	private static final Instruction[] values = Instruction.values();
	
	protected Instruction ins;
	protected Username from;
	protected Username to;
	protected byte[] payload;
	
	protected Packet(Packet p) {
		this(p.ins, p.from, p.to, p.payload);
	}
	public Packet(Instruction ins, Username from, Username to) {
		this(ins, from, to, new byte[0]);
	}
	public Packet(Instruction ins, Username from, Username to, byte[] payload) {
		this.ins = ins;
		this.from = from;
		this.to = to;
		this.payload = payload;
	}
	
	public Packet(InputStream in) throws IOException, ProtocolException {
		ins = readInstruction(Util.read(in));
		int fromLen = Util.readUShort(in);
		byte[] fromBytes = new byte[fromLen];
		Util.readBytes(in, fromBytes);
		from = new Username(fromBytes);
		
		int toLen = Util.readUShort(in);
		byte[] toBytes = new byte[toLen];
		Util.readBytes(in, toBytes);
		to = new Username(toBytes);
		
		int payloadLen = Util.readUShort(in);
		payload = new byte[payloadLen];
		Util.readBytes(in, payload);
	}
	public Packet(byte[] in) throws IOException, ProtocolException {
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
		case PUT_USERS:
			return new PacketPutUsers(p);
		case ECHO:
			return new PacketEcho(p);
		case START:
			return new PacketStart(p);
		case MOVE:
			return new PacketMove(p);
		case GET_USERS:
		case REQUEST_JOIN:
		case ACCEPT_JOIN_REQUST:
		case REJECT_JOIN_REQUEST:
		case QUIT:
		case WAIT:
			return p;
		}
		Util.debugTrace("Error: Unknown Instruction: " + p.getInstruction());
		return p;
	}
	
	@Override
	public String toString() {
		return getInstruction().toString()
		+ (getPayload().length == 0 ? "" : " - 0x" + Util.bytesToHex(getPayload()));
	}
	
	public void send(OutputStream out) throws IOException {
		if (getInstruction() != Instruction.WAIT) {
			//Util.debug("Sending packet from " + from + " to " + to + ": " + this);
		}
		out.write(toByteArray());
	}
	
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(
				1 + 2
				+ from.getBytes().length + 2
				+ to.getBytes().length + 2
				+ payload.length);
		buf.order(ByteOrder.BIG_ENDIAN);
		buf.put((byte) ins.ordinal());
		buf.putShort((short) from.getBytes().length);
		buf.put(from.getBytes());
		buf.putShort((short) to.getBytes().length);
		buf.put(to.getBytes());
		buf.putShort((short) payload.length);
		buf.put(payload);
		buf.position(0);
		return buf.array();
	}
	
	private static Instruction readInstruction(byte b) throws UnknownInstructionException {
		if (b < values.length) {
			return values[b];
		} else {
			throw new UnknownInstructionException(b);
		}
	}
	
	public Instruction getInstruction() {
		return ins;
	}
	public Username getFrom() {
		return from;
	}
	public Username getTo() {
		return to;
	}
	public byte[] getPayload() {
		return payload;
	}
}
