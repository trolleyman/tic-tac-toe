package server;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import shared.Instruction;
import shared.Packet;
import shared.PacketErr;
import shared.PacketOk;
import shared.PacketPutUsers;
import shared.PacketSetNick;
import shared.TTTProtocolException;
import shared.User;
import sun.awt.CharsetString;

public class ServerThread extends Thread {
	private Socket sock;
	private boolean close;
	private ClientState state;
	private String nick;
	
	public ServerThread(Socket _sock) {
		super();
		sock = _sock;
		close = false;
		state = ClientState.INIT_STATE;
		nick = null;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Connected to client at "
					+ sock.getInetAddress().getHostName()
					+ " (" + sock.getInetAddress().getHostAddress() + ":" + sock.getPort() + ")");
			
			close = false;
			state = ClientState.INIT_STATE;
			
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			sock.setReuseAddress(false);
			//sock.setSoTimeout(5000);
			
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
			String msg = e.getMessage();
			if (msg == null) {
				msg = "";
			}
			System.err.println("Connection had to close: " + msg);
			e.printStackTrace(System.err);
		} finally {
			// Remove the nick from the list of connected users
			if (nick != null)
				Server.removeUser(new User(nick));
			
			try {
				sock.close();
			} catch (IOException e) {
				// Don't care
			}
		}
	}
	
	private void handlePacket(InputStream in, OutputStream out) throws IOException {
		Packet p = new Packet(in);
		System.out.println("Packet recieved from "
				+ sock.getInetAddress().getHostName()
				+ " (" + sock.getInetAddress().getHostAddress() + ":" + sock.getPort() + "): "
				+ p.getInstruction());
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
					try {
						setNick(((PacketSetNick) Packet.downcastPacket(p)).getNick());
						(new PacketOk()).write(out);
					} catch (TTTProtocolException e) {
						(new PacketErr()).write(out);
					}
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
					try {
						setNick(((PacketSetNick) Packet.downcastPacket(p)).getNick());
						(new PacketOk()).write(out);
					} catch (TTTProtocolException e) {
						(new PacketErr()).write(out);
					}
					break;
				default:
					//throw new TTTProtocolException("Illegal instruction: " + i);
				}
				break;
			default:
				throw new TTTProtocolException("Illegal state: " + state);
			}
			break;
		}
	}

	private void setNick(String newNick) throws TTTProtocolException {
		if (Server.getUsers().contains(new User(newNick))) {
			throw new TTTProtocolException("User already connected: " + newNick);
		}
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
			throw new TTTProtocolException("Illegal state: " + state);
		}
	}
}
