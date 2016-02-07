package shared;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import shared.exception.InvalidUsername;
import shared.exception.InvalidUsernameException;

public class Util {
	public static final int MAX_PORT = 49152;
	
	/**
	 * Reads or throw an IO exception
	 * @param is The input stream
	 * @return the byte read.
	 * @throws IOException 
	 */
	public static byte read(InputStream in) throws IOException {
		int i = in.read();
		if (i == -1) {
			throw new EOFException();
		}
		return (byte) i;
	}
	public static int readUShort(InputStream in) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.order(ByteOrder.BIG_ENDIAN);
		buf.put(Util.read(in));
		buf.put(Util.read(in));
		buf.position(0);
		return buf.getShort() & 0xFFFF;
	}
	public static void readBytes(InputStream in, byte[] bytes) throws IOException {
		int len = in.read(bytes);
		if (len != bytes.length) {
			throw new EOFException();
		}
	}
	
	public static String sockAddressToString(Socket sock) {
		return sockAddressToString(sock.getInetAddress(), sock.getPort());
	}
	public static String sockAddressToString(InetSocketAddress addr) {
		return sockAddressToString(addr.getAddress(), addr.getPort());
	}
	public static String sockAddressToString(InetAddress addr, int port) {
		if (addr.getHostName().equals(addr.getHostAddress())) {
			return addr.getHostAddress() + ":" + port;
		} else {
			return addr.getHostName() + " (" + addr.getHostAddress() + ":" + port + ")";
		}
	}
	
	final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static String bytesToString(byte[] b) {
		StringBuilder build = new StringBuilder(b.length);
		build.append(Character.toChars(b[0])[0]);
		return build.toString();
	}
	
	public static String utf8Decode(byte[] b) throws CharacterCodingException {
		ByteBuffer buf = ByteBuffer.wrap(b);
		return StandardCharsets.UTF_8.newDecoder()
			.onMalformedInput(CodingErrorAction.REPORT)
			.onUnmappableCharacter(CodingErrorAction.REPORT)
			.decode(buf)
			.toString();
	}
	public static byte[] utf8Encode(String s) {
		try {
			CharBuffer cbuf = CharBuffer.wrap(s.toCharArray());
			return StandardCharsets.UTF_8.newEncoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.encode(cbuf)
				.array();
		} catch (CharacterCodingException e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * Encodes a string into a UTF-8 representation. Replaces invalid/unmappable characters with U+FFFD.
	 * @param s
	 * @return
	 */
	public static byte[] utf8EncodeReplace(String s) {
		CharBuffer cbuf = CharBuffer.wrap(s.toCharArray());
		
		try {
			return StandardCharsets.UTF_8.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.encode(cbuf)
				.array();
		} catch (CharacterCodingException e) {
			throw new AssertionError();
		}
	}
	
	/**
	 * Decodes a byte array into a string. Replaces invalid/unmappable characters with U+FFFD.
	 * @param b
	 * @return
	 */
	public static String utf8DecodeReplace(byte[] b) {
		ByteBuffer buf = ByteBuffer.wrap(b);
		try {
			return StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.decode(buf)
				.toString();
		} catch (CharacterCodingException e) {
			throw new AssertionError();
		}
	}
	
	public static String assertValidUsername(byte[] nick) throws InvalidUsernameException {
		String snick;
		try {
			snick = Util.utf8Decode(nick);
			assertValidUsername(snick);
			return snick;
		} catch (CharacterCodingException e) {
			throw new InvalidUsernameException(Util.utf8DecodeReplace(nick), InvalidUsername.UTF8);
		}
	}
	public static void assertValidUsername(String nick) throws InvalidUsernameException {
		if (nick.length() >= 32) {
			throw new InvalidUsernameException(nick, InvalidUsername.LENGTH);
		} else if (nick.equals(" ")) {
			// It's fine - it's the server
		} else {
			for (int i = 0; i < nick.length(); i++) {
				if (Character.isWhitespace(nick.charAt(i))) {
					throw new InvalidUsernameException(nick, InvalidUsername.WHITESPACE);
				}
			}
		}
	}
	public static boolean isDebug() {
		return false;
	}
}
