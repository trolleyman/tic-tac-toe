package shared;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class Util {
	public static final int MAX_PORT = 49152;
	
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
	public static byte[] utf8Encode(String s) throws CharacterCodingException {
		CharBuffer cbuf = CharBuffer.wrap(s.toCharArray());
		
		return StandardCharsets.UTF_8.newEncoder()
			.onMalformedInput(CodingErrorAction.REPORT)
			.onUnmappableCharacter(CodingErrorAction.REPORT)
			.encode(cbuf)
			.array();
	}
}
