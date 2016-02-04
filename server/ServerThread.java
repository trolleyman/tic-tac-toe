package server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import shared.Instruction;
import shared.Util;
import shared.exception.InvalidUsername;
import shared.exception.InvalidUsernameException;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketErr;
import shared.packet.PacketOk;
import shared.packet.PacketPutUsers;
import shared.packet.PacketSetNick;

public class ServerThread extends Thread {
	private Socket sock;
	private boolean close;
	private String nick;
	
	public ServerThread(Socket _sock) {
		super();
		sock = _sock;
		close = false;
		nick = null;
	}
	
	@Override
	public void run() {
		try {
			close = false;
			
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			sock.setReuseAddress(false);
			//sock.setSoTimeout(5000);
			
			InputStream  in  = sock.getInputStream();
			OutputStream out = sock.getOutputStream();
			
			while (!close) {
				handlePacket(in, out);
			}
		} catch (ProtocolException e) {
			// Problem with the internal connection
			if (nick != null) {
				System.out.println(nick + " left.");
			}
			String msg;
			if ((msg = e.getMessage()) != null) {
				System.err.println("Protocol error: " + msg);
			} else {
				System.err.println("A protocol error occured in a ServerThread.");
			}
			//e.printStackTrace(System.err);
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg == null) {
				msg = "";
			}
			//System.err.println("Connection had to close: " + msg);
			//e.printStackTrace(System.err);
		} finally {
			// Remove the nick from the list of connected users
			if (nick != null) {
				System.out.println(nick + " left.");
				HashMap<String, ?> users = Server.getUsersInfo();
				synchronized (users) {
					users.remove(nick);
				}
			}
			
			try {
				sock.close();
			} catch (IOException e) {
				// Don't care
			}
		}
	}
	
	private void handlePacket(InputStream in, OutputStream out) throws IOException, ProtocolException {
		Packet p = new Packet(in);
		//System.out.println("Packet recieved from " + Util.sockAddressToString(sock) + ": " + p.getInstruction());
		Instruction i = p.getInstruction();
		
		// These instructions are allowed at any time:
		switch (i) {
		case QUIT:
			close = true;
			break;
		case GET_USERS:
			(new PacketPutUsers(Server.getUsersInfo())).write(out);
			break;
		case PUT_USERS:
			// Ignore.
			break;
		case SET_NICK:
			try {
				String nick = ((PacketSetNick) Packet.downcastPacket(p)).getNick();
				setNick(nick);
				System.out.println(nick + " connected from " + Util.sockAddressToString(sock) + ".");
				(new PacketOk()).write(out);
			} catch (InvalidUsernameException e) {
				(new PacketErr(e.getMessage())).write(out);
			}
			break;
		case ACCEPT_JOIN_REQUST:
		case ERR:
		case OK:
		case REJECT_JOIN_REQUEST:
		case REQUEST_JOIN:
		case START:
		case WAIT:
		default:
			(new PacketErr()).write(out);
		}
	}

	private void setNick(String newNick) throws InvalidUsernameException {
		HashMap<String, InetSocketAddress> users = Server.getUsersInfo();
		synchronized (users) {
			if (users.containsKey(newNick)) {
				throw new InvalidUsernameException(newNick, InvalidUsername.USER_ALREADY_LOGGED_IN);
			}
			
			if (users.containsKey(nick)) {
				users.put(newNick, users.get(nick));
			} else {
				users.put(newNick, new InetSocketAddress(sock.getInetAddress(), sock.getPort()));
			}
			nick = newNick;
		}
	}

}
