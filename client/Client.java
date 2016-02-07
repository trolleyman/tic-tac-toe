package client;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import shared.Username;
import shared.Util;
import shared.exception.EchoException;
import shared.exception.InvalidUsernameException;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketEcho;

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
	
	public Client(String[] args) throws IOException, ProtocolException {
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
		lobby = new Lobby(addr);
		lobbyViewer = new LobbyViewer(lobby);
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
		(new PacketEcho(Username.SERVER, me, bytes)).send(sock.getOutputStream());
		Packet p = Packet.readPacket(sock.getInputStream());
		if (p instanceof PacketEcho && p.getPayload().equals(bytes)) {
			return;
		} else {
			throw new EchoException(bytes, p.getPayload());
		}
	}
	
	@Override
	public void run() {
		// Connect to server using nick
		Socket sock = null;
		try {
			sock = connect();
			echo(sock);
			
			
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
