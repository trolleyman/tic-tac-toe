package shared;
import java.nio.charset.StandardCharsets;

import shared.exception.InvalidUsernameException;

public class PacketSetNick extends Packet {
	
	private String nick;
	
	public PacketSetNick(Packet p) throws InvalidUsernameException {
		super(p);
		
		nick = Util.assertValidUsername(payload);
	}
	public PacketSetNick(String nick) throws InvalidUsernameException {
		super(Instruction.SET_NICK, nick.getBytes(StandardCharsets.UTF_8));
		this.nick = nick;
		Util.assertValidUsername(nick);
	}
	
	public String getNick() {
		return nick;
	}
}
