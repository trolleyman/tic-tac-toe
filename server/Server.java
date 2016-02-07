package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import shared.Username;
import shared.Util;
import shared.exception.UserNotConnectedException;
import shared.packet.Packet;

// java Server <port number>

public class Server implements Runnable {	
	public static void printUsage() {
		System.err.println("Usage: java Server <port number>");
	}
	public static void printUsageAndExit() {
		printUsage();
		System.exit(1);
	}
	
	public static int parsePort(String[] args) {
		if (args.length != 1) {
			printUsageAndExit();
		}
		
		try {
			int port = Integer.parseUnsignedInt(args[0]);
			if (port >= Util.MAX_PORT) {
				System.err.println("Number greater than " + Util.MAX_PORT + ": " + port);
				printUsageAndExit();
			}
			return port;
		} catch (NumberFormatException e) {
			System.err.println("Invalid number: " + args[0]);
			printUsageAndExit();
		}
		return 0; // So compiler doesn't complain
	}
	
	public static void main(String[] args) {
		Server s = new Server(parsePort(args));
		s.run();
	}
	
	private static HashMap<Username, ServerThread> users;
	private static int port;
	
	public static void registerUsername(Username name, ServerThread t) {
		synchronized (users) {
			users.put(name, t);
		}
	}
	public static void unregisterUsername(Username name) {
		synchronized (users) {
			users.remove(name);
		}
	}
	public static HashMap<Username, ServerThread> getUsers() {
		return users;
	}
	public static Username[] getUsersArray() {
		synchronized (users) {
			return users.keySet().toArray(new Username[users.size()]);
		}
	}
	
	public static void forwardPacket(Packet p) throws UserNotConnectedException {
		Username to = p.getTo();
		synchronized (users) {
			if (!users.containsKey(to)) {
				throw new UserNotConnectedException(to);
			}
			ServerThread t = users.get(to);
			t.queuePacket(p);
		}
	}
	
	public Server(int _port) {
		port = _port;
		users = new HashMap<Username, ServerThread>();
	}
	
	@Override
	public void run() {
		System.out.println("Server started on port " + port);
		while (true) {
			try {
				acceptIncoming();
			} catch (IOException e) {
				System.err.println("The server has encountered and error and must close.");
				String msg = e.getMessage();
				if (msg != null) System.out.println(msg);
				return;
			}
		}
	}
	
	private void acceptIncoming() throws IOException {
		ServerSocket serverSock = null;
		try {
			serverSock = new ServerSocket(port);
			while (true) {
				Socket sock = serverSock.accept();
				
				ServerThread t = new ServerThread(sock);
				t.start();
			}
		} finally {
			if (serverSock != null)
				serverSock.close();
		}
	}
}
