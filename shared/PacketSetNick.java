package shared;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class PacketSetNick extends Packet {
	
	private String nick;
	
	public PacketSetNick(Packet p) throws TTTProtocolException {
		super(p);
		
		try {
			nick = Util.utf8Decode(payload);
		} catch (CharacterCodingException e) {
			throw new TTTProtocolException("Invalid nick: " + new String(payload, StandardCharsets.UTF_8), e);
		}
	}
	public PacketSetNick(String nick) throws TTTProtocolException {
		super(Instruction.SET_NICK, nick.getBytes(StandardCharsets.UTF_8));
		this.nick = nick;
		if (nick.length() == 0) {
			throw new TTTProtocolException("Nick cannot be length 0.");
		}
	}
	
	public String getNick() {
		return nick;
	}
}
