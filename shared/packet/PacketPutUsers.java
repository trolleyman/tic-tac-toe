package shared.packet;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import shared.Instruction;
import shared.Username;
import shared.exception.ProtocolException;

public class PacketPutUsers extends Packet {
	private final Username[] users;
	
	public PacketPutUsers(Packet p) throws EOFException, ProtocolException {
		super(p);
		
		// Parse payload
		try {
			ByteBuffer buf = ByteBuffer.wrap(payload);
			int usersLen = buf.getShort() & 0xFF;
			users = new Username[usersLen];
			for (int i = 0; i < usersLen; i++) {
				// Get String
				int nickLen = buf.getShort() & 0xFF;
				byte[] bnick = new byte[nickLen];
				buf.get(bnick);
				users[i] = new Username(bnick);
			}
		} catch (BufferUnderflowException e) {
			throw new EOFException();
		}
	}
	public PacketPutUsers(Username from, Username to, final Username[] users) {
		super(Instruction.PUT_USERS, from, to);
		this.users = users;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(users.length * 32);
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeShort(users.length);
			for (Username user : users) {
				os.writeShort(user.getBytes().length);
				os.write(user.getBytes());
			}
			os.flush();
		} catch (IOException e) {
			// There is no way this can happen. Probably.
		}
		payload = baos.toByteArray();
	}
	
	public final Username[] getUsers() {
		return users;
	}
}
