package server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import shared.User;
import shared.Util;

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
	
	private static ArrayList<User> users;
	private int port;
	
	public Server(int _port) {
		port = _port;
		users = new ArrayList<User>();
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
			serverSock.close();
		}
	}
	
	public static void addUser(User user) {
		synchronized (users) {
			System.out.println("addUser: " + user.nick);
			users.add(user);
		}
	}
	public static void removeUser(User user) {
		synchronized (users) {
			System.out.println("removeUser: " + user.nick);
			users.remove(user);
		}
	}
	public static void changeUser(User oldUser, User newUser) {
		synchronized (users) {
			System.out.println("changeUser: " + oldUser.nick + " to " + newUser.nick);
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
