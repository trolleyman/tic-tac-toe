package shared.packet;
import java.io.EOFException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import shared.Instruction;
import shared.Util;
import shared.exception.InvalidUsernameException;

public class PacketSetUser extends Packet {
	
	private String nick;
	private InetSocketAddress listenAddr;
	
	public PacketSetUser(Packet p) throws InvalidUsernameException, EOFException {
		super(p);
		
		try {
			ByteBuffer buf = ByteBuffer.wrap(payload);
			int len = buf.getInt();
			byte[] bnick = new byte[len];
			buf.get(bnick);
			nick = Util.assertValidUsername(bnick);
			
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}
	public PacketSetUser(String nick) throws InvalidUsernameException {
		super(Instruction.SET_USER, nick.getBytes(StandardCharsets.UTF_8));
		this.nick = nick;
		Util.assertValidUsername(nick);
	}
	
	public String getNick() {
		return nick;
	}
	public InetSocketAddress getListenAddr() {
		return listenAddr;
	}
}
