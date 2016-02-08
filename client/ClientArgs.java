package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import shared.Username;
import shared.Util;
import shared.exception.InvalidUsernameException;

public class ClientArgs {
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
	
	public Username me;
	public int serverPort;
	public String machineName;
	public InetSocketAddress addr;
	public Socket sock;
	
	public ClientArgs(String[] args) {
		if (args.length != 3) {
			printUsageAndExit();
		}
		
		try {
			me = new Username(args[0]);
		} catch (InvalidUsernameException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		if (!me.isUser()) {
			System.err.println("Error: '" + args[0] + "' is an invalid username.");
			System.exit(1);
		}
		serverPort = parsePort(args[1]);
		machineName = args[2];
		
		addr = new InetSocketAddress(machineName, serverPort);
		if (addr.isUnresolved()) {
			System.err.println("Error: The hostname " + machineName + " could not be resolved.");
			System.exit(1);
		}
		
		try {
			sock = new Socket(addr.getAddress(), addr.getPort());
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
		} catch (IOException e) {
			System.err.println("Error: Could not connet to " + Util.sockAddressToString(addr) + ": " + e.getMessage());
			System.exit(1);
		}
	}
}
