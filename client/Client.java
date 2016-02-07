package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
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
	public static void printUsage() {
		System.err.println("Usage: java Client <user nickname> <port number> <machine name>");
	}
	public static void printUsageAndExit() {
		printUsage();
		System.exit(1);
	}
	
	public static int parsePort(String s) throws IllegalArgumentException {
		try {
			int port = Integer.parseUnsignedInt(s);
			if (port >= Util.MAX_PORT) {
				printUsageAndExit();
			}
			return port;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("'" + s + "' is not a valid number.");
		}
	}
	
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
			Client c = new Client(args);
			c.run();
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println(msg);
			printUsageAndExit();
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
	private int         serverPort;
	private String      machineName;
	private Lobby       lobby;
	private InetSocketAddress addr;
	private LobbyViewer lobbyViewer;
	private boolean     close;
	private Username    challenged;
	
	public Client(String[] args) throws IOException, ProtocolException {
		this.close = false;
		this.rng = new Random();
		if (args.length != 3) {
			throw new IllegalArgumentException();
		}
		
		me = new Username(args[0]);
		if (!me.isUser()) {
			System.out.println("'" + args[0] + "' is an invalid username.");
			System.exit(1);
		}
		serverPort = parsePort(args[1]);
		machineName = args[2];
		
		addr = new InetSocketAddress(machineName, serverPort);
		if (addr.isUnresolved()) {
			System.err.println("The hostname " + machineName + " could not be resolved.");
		}
		lobby = new Lobby(me, addr);
		lobbyViewer = new LobbyViewer(this, lobby);
	}
	
	private Socket connect() throws IOException {
		Socket sock = new Socket(addr.getAddress(), addr.getPort());
		sock.setKeepAlive(true);
		sock.setTcpNoDelay(true);
		return sock;
	}
	
	private void echo(Socket sock) throws IOException, ProtocolException {
		byte[] bytes = new byte[32];
		rng.nextBytes(bytes);
		(new PacketEcho(me, Username.SERVER, bytes)).send(sock.getOutputStream());
		Packet p = Packet.readPacket(sock.getInputStream());
		if (p instanceof PacketErr) {
			throw new ProtocolException(((PacketErr) p).getError());
		} else if (!(p instanceof PacketEcho)) {
			throw new IllegalInstructionException(p.getInstruction());
		} else if (!((PacketEcho) p).payloadEquals(bytes)) {
			throw new EchoException(bytes, p.getPayload());
		}
	}
	
	public void handlePacket(Socket sock) throws IOException, ProtocolException {
		InputStream in = sock.getInputStream();
		OutputStream out = sock.getOutputStream();
		
		Packet p = Packet.readPacket(in);
		Instruction i = p.getInstruction();
		
		switch (i) {
		case QUIT:
			close = true;
			return;
		case ECHO:
			(new PacketEcho(me, p.getFrom(), ((PacketEcho) p).getPayload())).send(out);
			break;
		case REQUEST_JOIN:
			Username opponent = ((PacketRequestJoin) p).getFrom();
			int option = JOptionPane.showConfirmDialog(null,
					opponent + " has sent you a game invitation. Do you accept?",
					"Game invitation alert",
					JOptionPane.YES_NO_OPTION);
			System.out.println("o: " + option);
			break;
		case ACCEPT_JOIN_REQUST:
		
			break;
		case REJECT_JOIN_REQUEST:
			
			break;
		case START:
			
			break;
		case ERR:
			if (Util.isDebug())
				System.err.println("Error: " + ((PacketErr) p).getError());
			break;
		case OK:
		case PUT_USERS:
		case WAIT:
			break; // Ignore
		case GET_USERS:
			(new PacketErr(me, p.getFrom(), "Illegal instruction: " + i)).send(out);
			break; // Send ERR
		}
	}
	
	public void challenge(Username user) {
		challenged = user;
		// Send join request.
		//(new PacketRequestJoin(me, user)).send();;
	}
	
	@Override
	public void run() {
		// Connect to server using nick
		Socket sock = null;
		try {
			sock = connect();
			echo(sock);
			while (!close) {
				try {
					handlePacket(sock);
				} catch (ProtocolException e) {
					// Ignore really, what could possibly go wrong.
				}
			}
			
		} catch (IOException | ProtocolException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println("Error: " + msg);
			System.exit(1);
		} finally {
			try {
				if (sock != null)
					sock.close();
			} catch (IOException e) {
				
			}
		}
	}
}
