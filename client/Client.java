package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import shared.Util;
import shared.exception.ProtocolException;

public class Client implements LobbyListener, Runnable {
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
	private int    port;
	private String machineName;
	private Lobby  lobby;
	
	public Client(String[] args) throws IOException, ProtocolException {
		if (args.length != 3) {
			throw new IllegalArgumentException();
		}
		
		nick = args[0];
		port = parsePort(args[1]);
		machineName = args[2];
		
		lobby = new Lobby(new InetSocketAddress(machineName, port));
		LobbyViewer lobbyViewer = new LobbyViewer(lobby);
	}
	
	@Override
	public void run() {
		
	}

	@Override
	public void usersChanged(HashMap<String, InetSocketAddress> newUsers) {
		
	}
}
