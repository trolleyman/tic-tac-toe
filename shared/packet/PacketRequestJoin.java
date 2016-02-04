package shared.packet;

import shared.Instruction;
import shared.Util;
import shared.exception.InvalidUsernameException;

public class PacketRequestJoin extends Packet {
	private String nick;
	
	public PacketRequestJoin(Packet p) throws InvalidUsernameException {
		super(p);
		
		nick = Util.assertValidUsername(payload);
	}
	
	public PacketRequestJoin(String nick) throws InvalidUsernameException {
		super(Instruction.REQUEST_JOIN);
		
		Util.assertValidUsername(nick);
		this.nick = nick;
		this.payload = Util.utf8Encode(nick);
	}

	public String getNick() {
		return nick;
	}
	
}
