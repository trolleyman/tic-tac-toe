import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ServerThread extends Thread {
	private Socket sock;
	private boolean close = false;
	
	public ServerThread(Socket _sock) {
		super();
		sock = _sock;
	}
	
	@Override
	public void run() {
		try {
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			sock.setReuseAddress(false);
			
			InputStream in = sock.getInputStream();
			
			while (!close) {
				byte[] packet = readPacket(in);
				handlePacket(packet);
			}
		} catch (TTTProtocolException e) {
			// Problem with the internal connection
			String msg;
			if ((msg = e.getMessage()) != null) {
				System.err.println("Protocol error: " + msg);
			} else {
				System.err.println("A protocol error occured in a ServerThread.");
			}
			e.printStackTrace(System.err);
		} catch (IOException e) {
			return;
		} finally {
			try {
				sock.close();
			} catch (IOException e) {
				// Don't care
			}
		}
	}
	
	private void readPacket(InputStream in) throws IOException {
		Packet p = Protocol.readInstruction(in);
		switch (p) {
		case HELLO:
			
			break;
		case GET_USERS:
			
			break;
		case PUT_USERS:
			
			break;
		case QUIT:
			close = true;
			break;
		default:
			throw new TTTProtocolException("Instruction not recognised: " + p);
		}
	}
	
	private void handlePacket(byte[] packet) throws IOException {
		if (packet.length == 0) {
			throw new TTTProtocolException("Packet length too short: " + packet.length);
		}
		byte insLen = packet[0];
		if (insLen == 0) {
			throw new TTTProtocolException("Instruction length too short: " + insLen);
		}
		if (packet.length - 1 < insLen) {
			throw new TTTProtocolException("Packet length too short: " + (packet.length - 1));
		}
		byte[] ins = Arrays.copyOfRange(packet, 0, insLen);
		if (ins.equals(Util.QUIT_INSTRUCTION)) {
			// Handle quit
			close = true;
		} else if (ins.equals(Util.GET_CONNECTED_USERS)) {
			sendUsers();
		} else {
			throw new TTTProtocolException("Illegal instruction: " + ins);
		}
	}
	
	// Sends the current set of users to the client.
	private void sendUsers() throws IOException {
		OutputStream out = sock.getOutputStream();
		
		out.write(Util.PUT_CONNECTED_USERS.length);
		out.write(Util.PUT_CONNECTED_USERS);
		
	}
}
