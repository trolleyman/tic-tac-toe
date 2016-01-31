import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PacketPutUsers extends Packet {
	ArrayList<User> users;
	
	public PacketPutUsers(Packet p) {
		super(p);
		
		// Parse payload
		ByteBuffer buf = ByteBuffer.wrap(payload);
	}
	public PacketPutUsers(ArrayList<User> users) {
		super(Instruction.PUT_USERS);
		ByteArrayOutputStream buf = new ByteArrayOutputStream(users.size() * 16);
		
		payload = buf.toByteArray();
	}
	
	public ArrayList<User> getUsers() {
		return users;
	}
}
