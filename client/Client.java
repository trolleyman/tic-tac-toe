package client;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import shared.User;
import shared.Util;
import shared.exception.InvalidUsernameException;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketSetUser;

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
	
	private String nick;
	private int    serverPort;
	private ServerSocket listenSocket;
	private String machineName;
	private Lobby  lobby;
	private InetSocketAddress addr;
	private LobbyViewer lobbyViewer;
	
	public Client(String[] args) throws IOException, ProtocolException {
		if (args.length != 3) {
			throw new IllegalArgumentException();
		}
		
		nick = args[0];
		serverPort = parsePort(args[1]);
		machineName = args[2];
		
		listenSocket = new ServerSocket(0);
		addr = new InetSocketAddress(machineName, serverPort);
		if (addr.isUnresolved()) {
			System.err.println("The hostname " + machineName + " could not be resolved.");
		}
		lobby = new Lobby(addr);
		lobbyViewer = new LobbyViewer(lobby);
	}
	
	public void sendJoinRequest(User opp) {
		
	}
	private static User getJoinRequest(ServerSocket serverSock) throws IOException {
		while (true) {
			try {
				Socket sock = serverSock.accept();
				sock.setSoTimeout(1000);
				sock.setTcpNoDelay(true);
				sock.setKeepAlive(true);
				
				Packet p = null;
				p = Packet.readPacket(sock.getInputStream());
				
			} catch (ProtocolException | EOFException e) {
				
			} catch (IOException e) {
				
			}
		}
	}
	
	private Socket connect() throws IOException {
		Socket sock = new Socket(addr.getAddress(), addr.getPort());
		sock.setKeepAlive(true);
		sock.setTcpNoDelay(true);
		return sock;
	}
	
	@Override
	public void run() {
		// Connect to server using nick
		Socket sock = null;
		try {
			sock = connect();
			(new PacketSetUser(nick)).write(sock.getOutputStream());
			User opp = getJoinRequest(listenSocket);
			
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println("IO Error: " + msg);
			System.exit(1);
		} catch (InvalidUsernameException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println(msg);
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
