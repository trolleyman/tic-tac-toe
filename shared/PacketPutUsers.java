package shared;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PacketPutUsers extends Packet {
	private final ArrayList<User> users;
	
	public PacketPutUsers(Packet p) throws TTTProtocolException {
		super(p);
		
		// Parse payload
		try {
			ByteBuffer buf = ByteBuffer.wrap(payload);
			int usersLen = buf.getInt();
			ArrayList<User> us = new ArrayList<User>(usersLen);
			for (int i = 0; i < usersLen; i++) {
				int nickLen = buf.getInt();
				byte[] nick = new byte[nickLen];
				buf.get(nick);
				try {
					us.add(new User(Util.utf8Decode(nick)));
				} catch (CharacterCodingException e) {
					throw new TTTProtocolException("Nick was not valid utf-8: " + (new String(nick, StandardCharsets.UTF_8)));
				}
			}
			users = us;
		} catch (BufferUnderflowException e) {
			throw new TTTProtocolException("Unexpected end of stream.");
		}
	}
	public PacketPutUsers(final ArrayList<User> users) {
		super(Instruction.PUT_USERS);
		this.users = users;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(users.size() * 16);
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeInt(users.size());
			for (User user : users) {
				try {
					byte[] nick = Util.utf8Encode(user.nick);
					os.writeInt(nick.length);
					os.write(nick);
				} catch (CharacterCodingException e) {
					throw new TTTProtocolException("Nick was not valid utf-8: " + user.nick);
				}
			}
			os.flush();
		} catch (IOException e) {
			// There is no way this can happen. Probably.
		}
		payload = baos.toByteArray();
	}
	
	public final ArrayList<User> getUsers() {
		return users;
	}
}
