package shared;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import shared.exception.InvalidUsername;
import shared.exception.InvalidUsernameException;

public class Util {
	public static final int MAX_PORT = 49152;
	
	public static String sockAddressToString(Socket sock) {
		InetAddress addr = sock.getInetAddress();
		if (addr.getHostName().equals(addr.getHostAddress())) {
			return addr.getHostAddress() + ":" + sock.getPort();
		} else {
			return addr.getHostName() + " (" + addr.getHostAddress() + ":" + sock.getPort() + ")";
		}
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
		if (nick.length() == 0) {
			throw new InvalidUsernameException(nick, InvalidUsername.LENGTH);
		} else {
			for (int i = 0; i < nick.length(); i++) {
				if (Character.isWhitespace(nick.charAt(i))) {
					throw new InvalidUsernameException(nick, InvalidUsername.WHITESPACE);
				}
			}
		}
	}
}
