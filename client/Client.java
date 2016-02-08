package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
import shared.packet.PacketRequestJoin;

public class Client implements Runnable {
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
			Client c = new Client(cargs.me, cargs.sock);
			c.run();
			Username opp = c.getOpponent();
			if (opp == null) {
				return;
			}
			Game g = new Game(cargs.sock, opp);
			
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println("IO Error: " + msg);
			else
				System.err.println("IO Error occured. The client has to close.");
			e.printStackTrace(System.err);
		} catch (ProtocolException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println("Protocol Error: " + msg);
		}
	}
	
	private Random      rng;
	private Username    me;
	private Lobby       lobby;
	private Socket      sock;
	private InetSocketAddress addr;
	private LobbyViewer lobbyViewer;
	private boolean     close;
	
	private Username    challenged;
	private Username    opponent;
	
	public Client(Username me, Socket sock) throws IOException, ProtocolException {
		this.rng = new Random();
		this.me = me;
		this.sock = sock;
		addr = new InetSocketAddress(sock.getInetAddress(), sock.getPort());
		lobby = new Lobby(me, addr);
		lobbyViewer = new LobbyViewer(this, lobby);
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
	
	public Username getOpponent() {
		return opponent;
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
				Username opponent = ((PacketRequestJoin) p).getFrom();
				int option = JOptionPane.showConfirmDialog(null,
						opponent + " has sent you a game invitation. Do you accept?",
						"Game invitation alert",
						JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					(new Packet(Instruction.ACCEPT_JOIN_REQUST, me, opponent)).send(out);
					this.opponent = opponent;
					return;
				} else {
					(new Packet(Instruction.REJECT_JOIN_REQUEST, me, opponent)).send(out);
				}
			}
			break;
		case ACCEPT_JOIN_REQUST:
			
			break;
		case REJECT_JOIN_REQUEST:
			
			break;
		case ERR:
			if (Util.isDebug())
				System.err.println("Error: " + ((PacketErr) p).getError());
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
				challenged = user;
				// Send join request.
				synchronized (sock) {
					(new PacketRequestJoin(me, user)).send(sock.getOutputStream());
				}
			} catch (IOException e) {
				challenged = null;
				throw e;
			}
		}
	}
	
	@Override
	public void run() {
		// Connect to server using nick
		try {
			echo(sock);
			while (!close) {
				try {
					long last = System.nanoTime();
					if (opponent != null) {
						// Found a worthy adversary, return.
						return;
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
			String msg = e.getMessage();
			if (msg != null)
				System.err.println("Error: " + msg);
			System.exit(1);
		}
	}
}
