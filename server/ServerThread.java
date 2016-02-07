package server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import shared.Instruction;
import shared.Username;
import shared.exception.ProtocolException;
import shared.exception.UserNotConnectedException;
import shared.packet.Packet;
import shared.packet.PacketErr;
import shared.packet.PacketPutUsers;

public class ServerThread extends Thread {
	private Socket sock;
	private boolean close;
	private Username nick;
	private LinkedList<Packet> packetQueue;
	
	public ServerThread(Socket _sock) {
		super();
		sock = _sock;
		close = false;
		nick = null;
		packetQueue = new LinkedList<Packet>();
	}
	
	@Override
	public void run() {
		try {
			close = false;
			
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			//sock.setSoTimeout(5000);
			
			InputStream  in  = sock.getInputStream();
			OutputStream out = sock.getOutputStream();
			
			while (!close) {
				if (in.available() > 0) {
					handlePacket(in, out);
				} else {
					synchronized (packetQueue) {
						while (packetQueue.size() > 0) {
							packetQueue.pop().send(out);
						}
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					
				}
			}
		} catch (ProtocolException e) {
			// Problem with the internal connection
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
				Server.unregisterUsername(nick);
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
		
		if (i == Instruction.QUIT) {
			close = true;
			return;
		}
		
		if (p.getFrom().isServer()) {
			(new PacketErr(Username.SERVER, p.getFrom(), "A server cannot connect to another server")).send(out);
			(new Packet(Instruction.QUIT, Username.SERVER, p.getFrom())).send(out);
		}
		
		if (nick == null) {
			HashMap<Username, ServerThread> users = Server.getUsers();
			synchronized (users) {
				if (users.containsKey(p.getFrom())) {
					(new PacketErr(Username.SERVER, p.getFrom(), "Username already taken")).send(out);
				} else {
					users.put(p.getFrom(), this);
				}
			}
			nick = p.getFrom();
			Server.registerUsername(nick, this);
		}
		
		if (!p.getFrom().equals(nick)) {
			(new PacketErr(Username.SERVER, p.getFrom(),
					"Only " + nick.getString() + " can send packets on this socket. Got " + p.getFrom().getString()
					)).send(out);
			return;
		}
		
		if (!p.getTo().isServer()) {
			try {
				Server.forwardPacket(p);
			} catch (UserNotConnectedException e) {
				(new PacketErr(Username.SERVER, nick, e.getMessage())).send(out);
			}
			return;
		}
		
		// These instructions are allowed at any time:
		switch (i) {
		case QUIT:
			close = true;
			break;
		case GET_USERS:
			(new PacketPutUsers(Username.SERVER, nick, Server.getUsersArray())).send(out);
			break;
		case OK:
		case ERR:
		case WAIT:
			break;
			
		case PUT_USERS:
		case ACCEPT_JOIN_REQUST:
		case REJECT_JOIN_REQUEST:
		case REQUEST_JOIN:
		case START:
		default:
			(new PacketErr(Username.SERVER, nick, "Illegal instruction: " + i)).send(out);
		}
	}
	
	/**
	 * Queues a packet to be sent from the server to the user associated with the client.
	 * @param p
	 */
	public void queuePacket(Packet p) {
		synchronized (packetQueue) {
			packetQueue.push(p);
		}
		this.interrupt();
	}

}
