package shared;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.HashMap;
import java.util.Map.Entry;

import shared.exception.ProtocolException;

public class PacketPutUsers extends Packet {
	private final HashMap<String, InetSocketAddress> users;
	
	public PacketPutUsers(Packet p) throws EOFException, ProtocolException {
		super(p);
		
		// Parse payload
		try {
			ByteBuffer buf = ByteBuffer.wrap(payload);
			int usersLen = buf.getInt();
			users = new HashMap<String, InetSocketAddress>();
			for (int i = 0; i < usersLen; i++) {
				// Get String
				int nickLen = buf.getInt();
				byte[] bnick = new byte[nickLen];
				buf.get(bnick);
				String nick = Util.assertValidUsername(bnick);
				
				// Get SocketAddress
				int addrLen = buf.getInt();
				byte[] baddr = new byte[addrLen];
				buf.get(baddr);
				String addrString = Util.utf8Decode(baddr);
				int port = buf.getShort() & 0xFFFF;
				InetSocketAddress addr = new InetSocketAddress(addrString, port);
				
				users.put(nick, addr);
			}
		} catch (CharacterCodingException e) {
			throw new ProtocolException("Invalid UTF-8");
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}
	public PacketPutUsers(final HashMap<String, InetSocketAddress> users) {
		super(Instruction.PUT_USERS);
		this.users = users;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(users.size() * 16);
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeInt(users.size());
			for (Entry<String, InetSocketAddress> user : users.entrySet()) {
				os.writeInt(user.getKey().length());
				os.write(Util.utf8Encode(user.getKey()));
				
				String addr = user.getValue().getHostString();
				os.writeInt(addr.length());
				os.write(Util.utf8Encode(addr));
				os.writeShort(user.getValue().getPort());
			}
			os.flush();
		} catch (IOException e) {
			// There is no way this can happen. Probably.
		}
		payload = baos.toByteArray();
	}
	
	public final HashMap<String, InetSocketAddress> getUsers() {
		return users;
	}
}
