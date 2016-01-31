import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server implements Runnable {
	public static final int MAX_PORT = 49152;
	public static void printUsage() {
		System.err.println("Usage: java Server <port number>");
	}
	public static void printUsageAndExit() {
		printUsage();
		System.exit(1);
	}
	
	public static int parsePort(String[] args) {
		if (args.length != 2) {
			printUsageAndExit();
		}
		
		try {
			int port = Integer.parseUnsignedInt(args[1]);
			if (port >= MAX_PORT) {
				printUsageAndExit();
			}
			return port;
		} catch (NumberFormatException e) {
			printUsageAndExit();
		}
		return 0; // So compiler doesn't complain
	}
	
	public static void main(String[] args) {
		Server s = new Server(parsePort(args));
		s.run();
	}
	
	private static ArrayList<User> users;
	private int port;
	
	public Server(int _port) {
		port = _port;
	}
	
	@Override
	public void run() {
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
			serverSock.close();
		}
	}
	
	public static void addUser(User user) {
		synchronized (users) {
			users.add(user);
		}
	}
	public static void removeUser(User user) {
		synchronized (users) {
			users.remove(user);
		}
	}
	public static void changeUser(User oldUser, User newUser) {
		synchronized (users) {
			users.remove(oldUser);
			users.add(newUser);
		}
	}
	
	public static final ArrayList<User> getUsers() {
		synchronized (users) {
			return new ArrayList<User>(users);
		}
	}
}
