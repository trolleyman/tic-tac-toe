package client;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import shared.Instruction;
import shared.Username;
import shared.Util;
import shared.exception.EchoException;
import shared.exception.IllegalInstructionException;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketEcho;
import shared.packet.PacketErr;

public class Client {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e2) {
				// hmm.. Hope things work out.
			}
		}
		
		try {
			ClientArgs cargs = new ClientArgs(args);
			
			while (true) {
				Lobby lobby = new Lobby(cargs.me, cargs.addr);
				Client c = new Client(cargs.me, cargs.sock, lobby);
				LobbyViewer view = new LobbyViewer(c, lobby);
				Username opp = c.run();
				Point pos = view.getPosition();
				Dimension size = view.getSize();
				pos.x = pos.x + size.width / 2;
				pos.y = pos.y + size.height / 2;
				view.close();
				
				if (opp == null) {
					Util.debugTrace("Error: opp is null");
					return;
				}
				Game g = new Game(cargs.sock, c.requestedJoin(), cargs.me, opp);
				GameViewer gv = new GameViewer(g, pos);
				g.run();
				gv.close();
			}
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println("IO Error: " + msg);
			else
				System.err.println("IO Error occured. The client has to close.");
			if (Util.isDebug())
				e.printStackTrace(System.err);
		} catch (ProtocolException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println("Protocol Error: " + msg);
			if (Util.isDebug())
				e.printStackTrace(System.err);
		}
	}

	private Random   rng;
	private Username me;
	private Socket   sock;
	private volatile boolean  close;
	
	private Username challenged;
	private Username opponent;
	private volatile boolean requestedJoin;
	
	public Client(Username me, Socket sock, Lobby lobby) throws IOException, ProtocolException {
		rng = new Random();
		this.me = me;
		this.sock = sock;
	}
	
	private void echo(Socket sock) throws IOException, ProtocolException {
		byte[] bytes = new byte[32];
		rng.nextBytes(bytes);
		(new PacketEcho(me, Username.SERVER, bytes)).send(sock.getOutputStream());
		Packet p = Packet.readPacket(sock.getInputStream());
		if (p instanceof PacketErr) {
			String err = ((PacketErr) p).getError();
			throw new ProtocolException(err);
		} else if (!(p instanceof PacketEcho)) {
			throw new IllegalInstructionException(p.getInstruction());
		} else if (!((PacketEcho) p).payloadEquals(bytes)) {
			throw new EchoException(bytes, p.getPayload());
		}
	}
	
	public boolean requestedJoin() {
		return requestedJoin;
	}
	public Username getOpponent() {
		return opponent;
	}
	public void setOpponent(Username opponent) {
		this.opponent = opponent;
	}
	
	public void handlePacket(Socket sock) throws IOException, ProtocolException {
		InputStream in = sock.getInputStream();
		OutputStream out = sock.getOutputStream();
		
		sock.setSoTimeout(100);
		Packet p = null;
		try {
			p = Packet.readPacket(in);
		} catch (SocketTimeoutException e) {
			return;
		}
		sock.setSoTimeout(0);
		Util.debug("Packet recieved from " + p.getFrom() + " to " + p.getTo()
			+ ": " + p);
		Instruction i = p.getInstruction();
		
		switch (i) {
		case QUIT:
			close = true;
			return;
		case ECHO:
			(new PacketEcho(me, p.getFrom(), ((PacketEcho) p).getPayload())).send(out);
			break;
		case REQUEST_JOIN:
			synchronized (this) {
				Util.debug("Recieved challenge from " + p.getFrom());
				Username opponent = p.getFrom();
				int option = JOptionPane.showConfirmDialog(null,
						opponent + " has sent you a game invitation. Do you accept?",
						"Game invitation alert",
						JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					(new Packet(Instruction.ACCEPT_JOIN_REQUST, me, opponent)).send(out);
					this.opponent = opponent;
					requestedJoin = false;
					return;
				} else {
					(new Packet(Instruction.REJECT_JOIN_REQUEST, me, opponent)).send(out);
				}
			}
			break;
		case ACCEPT_JOIN_REQUST:
			synchronized (this) {
				if (challenged != null) {
					Util.println("Challenge vs " + challenged + " accepted!");
					opponent = challenged;
					requestedJoin = true;
					challenged = null;
					return;
				}
			}
			break;
		case REJECT_JOIN_REQUEST:
			synchronized (this) {
				if (challenged != null) {
					Util.println("Challenge vs " + challenged + " rejected...");
					challenged = null;
				}
			}
			break;
		case ERR:
			Util.debugTrace("Error: " + ((PacketErr) p).getError());
			break;
		case OK:
		case START:
		case PUT_USERS:
		case WAIT:
			break; // Ignore
		case GET_USERS:
			(new PacketErr(me, p.getFrom(), "Illegal instruction: " + i)).send(out);
			break; // Send ERR
		}
	}
	
	public boolean isChallenging() {
		return challenged != null;
	}
	
	public void challenge(Username user) throws IOException {
		synchronized (this) {
			try {
				Util.debugTrace("Challenging "+ user);
				challenged = user;
				// Send join request.
				synchronized (sock) {
					(new Packet(Instruction.REQUEST_JOIN, me, user)).send(sock.getOutputStream());
				}
			} catch (IOException e) {
				challenged = null;
				throw e;
			}
		}
	}
	
	public Username run() {
		// Connect to server using nick
		opponent = null;
		try {
			echo(sock);
			while (!close) {
				try {
					long last = System.nanoTime();
					if (opponent != null) {
						// Found a worthy adversary, return.
						return opponent;
					} else if (sock.getInputStream().available() > 0) {
						synchronized (sock) {
							handlePacket(sock);
						}
					} else {
						// 100 milliseconds in nanoseconds = 100_000_000
						synchronized (sock) {
							if (System.nanoTime() - last >= 100_000_000) {
								(new Packet(Instruction.WAIT, me, Username.SERVER))
									.send(sock.getOutputStream());
								last = System.nanoTime();
							}
						}
					}
				} catch (ProtocolException e) {
					// Ignore really, what could possibly go wrong.
				}
			}
		} catch (IOException | ProtocolException e) {
			Util.debugTrace(e);
			System.exit(1);
		}
		return null;
	}
}
