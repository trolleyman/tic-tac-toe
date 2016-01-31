import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import sun.awt.CharsetString;

public class ServerThread extends Thread {
	private Socket sock;
	private boolean close;
	private ClientState state;
	private String nick;
	
	public ServerThread(Socket _sock) {
		super();
		sock = _sock;
	}
	
	@Override
	public void run() {
		try {
			close = false;
			state = ClientState.INIT_STATE;
			
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			sock.setReuseAddress(false);
			
			InputStream  in  = sock.getInputStream();
			OutputStream out = sock.getOutputStream();
			
			while (!close) {
				handlePacket(in, out);
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
	
	private void handlePacket(InputStream in, OutputStream out) throws IOException {
		Packet p = readPacket(in);
		Instruction i = p.getInstruction();
		
		// These instructions are allowed at any time:
		switch (i) {
		case QUIT:
			close = true;
			break;
		default:
			switch (state) {
			case INIT_STATE:
				switch (i) {
				case SET_NICK:
					setNick(((PacketSetNick) p).getNick());
					(new PacketOk()).write(out);
					break;
				default:
					throw new TTTProtocolException("Illegal instruction: " + i);
				}
				break;
			case WAITING_STATE:
				switch (i) {
				case GET_USERS:
					(new PacketPutUsers(Server.getUsers())).write(out);
					break;
				case PUT_USERS:
					// Ignore.
					break;
				case SET_NICK:
					setNick(((PacketSetNick) p).getNick());
					break;
				default:
					throw new TTTProtocolException("Illegal instruction: " + i);
				}
				break;
			default:
				throw new TTTProtocolException("Illegal state: " + state);
			}
			break;
		}
	}
	
	
	private Packet readPacket(InputStream in) throws IOException {
		Packet p = new Packet(in);
		Instruction i = p.getInstruction();
		switch (i) {
		case OK:
			return new PacketOk(p);
		case ERR:
			return new PacketErr(p);
		case GET_USERS:
			return p;
		case PUT_USERS:
			return new PacketPutUsers(p);
		case QUIT:
			return p;
		case SET_NICK:
			return new PacketSetNick(p);
		default:
			throw new TTTProtocolException("Illegal instruction: " + i);
		}
	}

	private void setNick(String newNick) {
		switch (state) {
		case INIT_STATE:
			state = ClientState.WAITING_STATE;
			nick = newNick;
			Server.addUser(new User(nick));
			break;
		case WAITING_STATE:
			nick = newNick;
			Server.changeUser(new User(nick), new User(newNick));
			break;
		default:
			// Don't do anything.
		}
	}
}
