package client;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import shared.Username;
import shared.Util;
import shared.exception.IllegalInstructionException;
import shared.exception.InvalidUsernameException;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketErr;
import shared.packet.PacketOk;

// java Client <user nickname> <port number> <machine name>

public class CommandLineClient implements Runnable {
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
			CommandLineClient c = new CommandLineClient(args);
			c.run();
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			if (msg != null)
				System.err.println(msg);
			printUsageAndExit();
		}
	}
	
	private String  nick;
	private int     port;
	private String  machineName;
	private boolean close;
	private Lobby   lobby;
	
	public CommandLineClient(String[] args) {
		if (args.length != 3) {
			throw new IllegalArgumentException();
		}
		
		nick = args[0];
		port = parsePort(args[1]);
		machineName = args[2];
		close = false;
	}
	
	private Socket connect() throws UnknownHostException, IOException {
		Socket sock = new Socket(machineName, port);
		
		sock.setKeepAlive(true);
		sock.setTcpNoDelay(true);
		sock.setSoTimeout(5000);
		
		return sock;
	}
	
	private Username getOpponent(Socket serverSock) throws IOException, ProtocolException {
		while (true) {
			
			ArrayList<Username> users = new ArrayList<Username>(Arrays.asList(lobby.getUsers()));
			users.remove(nick);
			if (users.size() == 0) {
				System.out.println("No users connected to server.");
			} else {
				System.out.println("Users connected to the server:");
				for (Username user : users) {
					System.out.println(String.format("%s", user));
				}
			}
			
			System.out.print("Enter a user to join: ");
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			String opponentName = stdin.readLine();
			if (opponentName.trim().equalsIgnoreCase("quit") || opponentName.trim().equalsIgnoreCase("exit")) {
				System.exit(0);
			}
			try {
				Util.assertValidUsername(opponentName);
			} catch (InvalidUsernameException e) {
				System.err.println("Error: '" + opponentName + "' is not a valid username.");
				continue;
			}
			for (Username user : users) {
				if (user.equals(opponentName)) {
					System.out.println("Connecting to " + opponentName + "'s game...");
					return user;
				}
			}
			System.err.println("Error: " + opponentName + " not connected to the server.");
		}
	}
	
	@SuppressWarnings("resource")
	@Override
	public void run() {
		Socket sock = null;
		try {
			while (true) {
				if (sock == null || sock.isClosed())
					sock = connect();
				
				lobby = new Lobby(new InetSocketAddress(sock.getInetAddress(), sock.getPort()));
				
				System.out.println("Connected to server at " + Util.sockAddressToString(sock));
				System.out.println("Type quit to exit");
				
				//System.out.println("Welcome, " + nick + ".");
				
				Username opponent = getOpponent(sock);
				
				System.out.println("Connected to " + opponent + " in a game of tic-tac-toe.");
				//while (!g.isFinished()) {
				//	g.step();
				//}
				
				System.out.println("Finished.");
				
				//System.out.println("Game " + (g.hasWon() ? "won!" : "lost."));
				int c;
				boolean anotherRound;
				while (true) {
					System.out.print("Another round? (Y/N): ");
					if ((c = System.in.read()) == -1) {
						throw new EOFException();
					} else if (c == 'Y' || c == 'y') {
						anotherRound = true;
						break;
					} else if (c == 'N' || c == 'n') {
						anotherRound = false;
						break;
					} else {
						System.out.println("Invalid input.");
					}
				}
				System.in.read();
				if (!anotherRound)
					break;
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
			String msg = e.getMessage() == null ? "" : e.getMessage();
			System.err.println("IO Error: " + msg);
			e.printStackTrace(System.err);
		} finally {
			if (sock != null) {
				try {
					sock.close();
				} catch (IOException e) {
					// Whatever man, just let me leave.
				}
			}
		}
	}
}
