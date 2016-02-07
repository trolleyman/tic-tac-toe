package server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import shared.Instruction;
import shared.Username;
import shared.Util;
import shared.exception.ProtocolException;
import shared.exception.UserNotConnectedException;
import shared.packet.Packet;
import shared.packet.PacketEcho;
import shared.packet.PacketErr;
import shared.packet.PacketPutUsers;

public class ServerThread extends Thread {
	private Socket sock;
	private boolean close;
	private Username client;
	private LinkedList<Packet> packetQueue;
	
	public ServerThread(Socket _sock) {
		super();
		sock = _sock;
		close = false;
		client = null;
		packetQueue = new LinkedList<Packet>();
	}
	
	@Override
	public void run() {
		try {
			close = false;
			
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			//sock.setSoTimeout(5000);
			
			while (!close) {
				if (sock.getInputStream().available() > 0) {
					handlePacket(sock.getInputStream(), sock.getOutputStream());
				} else if (!packetQueue.isEmpty()) {
					for (Packet packet : packetQueue) {
						packet.send(sock.getOutputStream());
					}
				} else {
					(new Packet(Instruction.WAIT, Username.SERVER, client)).send(sock.getOutputStream());;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						
					}
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
			if (client != null) {
				System.out.println(client + " left.");
				Server.unregisterUsername(client);
			}
			
			try {
				sock.close();
			} catch (IOException e) {
				// Don't care
			}
		}
	}
	
	private void handlePacket(InputStream in, OutputStream out) throws IOException, ProtocolException {
		Packet p = Packet.readPacket(in);
		
		if (Util.isDebug()) {
			String payloadMsg = "";
			if (p.getPayload().length > 0)
				payloadMsg = " - 0x" + Util.bytesToHex(p.getPayload());
			System.out.println("Packet recieved from " + p.getFrom() + " to " + p.getTo()
				+ ": " + p.getInstruction() + payloadMsg);
		}
		
		Instruction i = p.getInstruction();
		
		if (i == Instruction.QUIT) {
			close = true;
			return;
		}
		
		if (p.getFrom().isServer()) {
			(new PacketErr(Username.SERVER, p.getFrom(), "A server cannot connect to another server")).send(out);
			(new Packet(Instruction.QUIT, Username.SERVER, p.getFrom())).send(out);
			return;
		}
		
		if (client == null && !p.getFrom().isNull()) {
			HashMap<Username, ServerThread> users = Server.getUsers();
			synchronized (users) {
				if (users.containsKey(p.getFrom())) {
					(new PacketErr(Username.SERVER, p.getFrom(), "Username already taken")).send(out);
					return;
				} else {
					users.put(p.getFrom(), this);
				}
			}
			client = p.getFrom();
			Thread.currentThread().setName("ServerThread: " + client.toString());
			System.out.println(client.getString() + " connected.");
			Server.registerUsername(client, this);
		}
		
		if (!(client == null)) {
			if (!p.getFrom().isNull() && !p.getFrom().equals(client)) {
				(new PacketErr(Username.SERVER, p.getFrom(),
						"Only " + client.getString() + " can send packets on this socket."
								+ " Got " + p.getFrom().getString()
						)).send(out);
				return;
			}
		}
		
		if (!p.getTo().isServer()) {
			try {
				Server.forwardPacket(p);
			} catch (UserNotConnectedException e) {
				(new PacketErr(Username.SERVER, p.getFrom(), e.getMessage())).send(out);
			}
			return;
		}
		
		// These instructions are allowed at any time:
		switch (i) {
		case QUIT:
			close = true;
			break;
		case GET_USERS:
			(new PacketPutUsers(Username.SERVER, p.getFrom(), Server.getUsersArray())).send(out);
			break;
		case ECHO:
			(new PacketEcho(Username.SERVER, p.getFrom(), p.getPayload())).send(out);;
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
			(new PacketErr(Username.SERVER, p.getFrom(), "Illegal instruction: " + i)).send(out);
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
