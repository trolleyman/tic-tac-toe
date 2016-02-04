package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import shared.Packet;
import shared.PacketGetUsers;
import shared.PacketPutUsers;
import shared.exception.ProtocolException;

public class Lobby implements Runnable {
	private Socket serverSock = null;
	private HashMap<String, InetSocketAddress> users;
	private boolean running = true;
	private InetSocketAddress serverAddr;
	private ArrayList<LobbyListener> listeners;
	
	public Lobby(InetSocketAddress serverAddr) throws IOException, ProtocolException {
		this.listeners = new ArrayList<LobbyListener>();
		this.serverAddr = serverAddr;
		connect(serverAddr);
		updateUsers();
		
		Thread t = new Thread(this, "Lobby Thread");
		t.setDaemon(true);
		t.start();
	}
	
	public void addListener(LobbyListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	public void removeListener(LobbyListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	private void connect(InetSocketAddress addr) throws IOException {
		serverSock = new Socket(addr.getAddress(), addr.getPort());
		serverSock.setKeepAlive(true);
		serverSock.setTcpNoDelay(true);
		serverSock.setReuseAddress(false);
	}
	
	private void updateUsers() throws IOException, ProtocolException {
		(new PacketGetUsers()).write(serverSock.getOutputStream());
		Packet p = null;
		while (p == null || !(p instanceof PacketPutUsers)) {
			if (!running) {
				return;
			}
			p = Packet.readPacket(serverSock.getInputStream());
		}
		HashMap<String, InetSocketAddress> us = ((PacketPutUsers) p).getUsers();
		synchronized (this) {
			users = us;
		}
		//System.out.println("users: " + users);
	}
	
	@Override
	public void run() {
		try {
			if (serverSock == null || serverSock.isClosed())
				connect(serverAddr);
			
			while (running) {
				updateUsers();
				synchronized (listeners) {
					for (LobbyListener l : listeners)
						l.usersChanged(users);
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		} catch (IOException e) {
			
		} catch (ProtocolException e) {
			
		} finally {
			if (serverSock != null)
				try {
					serverSock.close();
				} catch (IOException e) {
					// Whatever.
				} finally {
					serverSock = null;
				}
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public HashMap<String, InetSocketAddress> getUsers() {
		synchronized (this) {
			return users;
		}
	}
}
