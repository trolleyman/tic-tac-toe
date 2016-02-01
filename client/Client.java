package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import shared.Instruction;
import shared.Packet;
import shared.PacketSetNick;
import shared.TTTProtocolException;
import shared.Util;

// java Client <user nickname> <port number> <machine name>

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
			Client c = new Client(args);
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
	
	public Client(String[] args) {
		if (args.length != 3) {
			throw new IllegalArgumentException();
		}
		
		nick = args[0];
		port = parsePort(args[1]);
		machineName = args[2];
		close = false;
	}
	
	@Override
	public void run() {
		Socket sock = null;
		try {
			sock = new Socket(machineName, port);
			
			sock.setKeepAlive(true);
			sock.setTcpNoDelay(true);
			sock.setReuseAddress(false);
			sock.setSoTimeout(5000);
			
			OutputStream os = sock.getOutputStream();
			InputStream in = sock.getInputStream();
			
			System.out.println("Connected to server at " + machineName);
			(new PacketSetNick(nick)).write(os);
			expectOkPacket(in);
			System.out.println("Welcome, " + nick + ".");
			
			while (!close) {
				close = true;
			}
		} catch (TTTProtocolException e) {
			// Problem with the internal connection
			String msg;
			if ((msg = e.getMessage()) != null) {
				System.err.println("Protocol error: " + msg);
			} else {
				System.err.println("A protocol error occured in a ServerThread.");
			}
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("Connection had to close");
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
	
	private void expectOkPacket(InputStream in) throws IOException {
		Packet p = Packet.readPacket(in);
		if (p.getInstruction() != Instruction.OK) {
			throw new TTTProtocolException("Expected OK packet, got " + p.getInstruction());
		}
	}
}
