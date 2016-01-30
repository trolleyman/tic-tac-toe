import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Protocol {
	static {
		try {
			HELLO_INSTRUCTION     = "HELLO".getBytes("ISO-8859-1");
			GET_USERS_INSTRUCTION = "GETUSRS".getBytes("ISO-8859-1");
			PUT_USERS_INSTRUCTION = "PUTUSRS".getBytes("ISO-8859-1");
			QUIT_INSTRUCTION      = "QUIT".getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("ISO-8859-1 unknown.");
		}
	}
	private static final byte[] HELLO_INSTRUCTION;
	private static final byte[] GET_USERS_INSTRUCTION;
	private static final byte[] PUT_USERS_INSTRUCTION;
	private static final byte[] QUIT_INSTRUCTION;
	
	public static Packet readInstruction(InputStream in) throws IOException {
		int packetLen = readUShort(in);
		if (packetLen == 0) {
			throw new TTTProtocolException("Packet length too short: " + packetLen);
		}
		
		int insLen = readUShort(in);
		byte[] ins = readByteArray(in, insLen);
		if (ins.equals(HELLO_INSTRUCTION)) {
			return Packet.HELLO;
		} else if (ins.equals(GET_USERS_INSTRUCTION)) {
			return Packet.GET_USERS;
		} else if (ins.equals(PUT_USERS_INSTRUCTION)) {
			return Packet.PUT_USERS;
		} else if (ins.equals(QUIT_INSTRUCTION)) {
			return Packet.QUIT;
		} else {
			throw new TTTProtocolException("Unknown instruction: " + );
		}
	}

	public static void writeInstruction(OutputStream out, Packet p) {
		out.write();
	}
	
	private static byte[] readByteArray(InputStream in, int len) throws IOException {
		byte[] arr = new byte[len];
		if (in.read(arr) != len) {
			throw new TTTProtocolException("Unexpected end of stream.");
		}
		return arr;
	}
	
	public static int readUShort(InputStream in) throws IOException {
		int b0 = in.read();
		int b1 = in.read();
		
		if (b0 == -1 || b1 == -1) {
			throw new TTTProtocolException("Unexpected end of stream.");
		}
		
		ByteBuffer buf = ByteBuffer.wrap(new byte[] {(byte) b0, (byte) b1});
		buf.order(ByteOrder.BIG_ENDIAN);
		return buf.getShort() & 0x0000FFFF;
	}
	
	/*
	public static long readUInt(InputStream in) throws IOException {
		int b0 = in.read();
		int b1 = in.read();
		int b2 = in.read();
		int b3 = in.read();
		
		if (b0 == -1 || b1 == -1 || b2 == -1 || b3 == -1) {
			throw new TTTProtocolException("Unexpected end of stream.");
		}
		
		ByteBuffer buf = ByteBuffer.wrap(new byte[] {(byte) b0, (byte) b1, (byte) b2, (byte) b3});
		buf.order(ByteOrder.BIG_ENDIAN);
		return buf.getInt() & 0x00000000FFFFFFFFL;
	}*/
}
