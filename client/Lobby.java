package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import shared.Username;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketPutUsers;

public class Lobby implements Runnable {
	private Socket sock;
	private Username[] users;
	private boolean running = true;
	private ArrayList<LobbyListener> listeners;
	
	public Lobby(Username from, Socket sock) throws IOException, ProtocolException {
		this.sock = sock;
		this.listeners = new ArrayList<LobbyListener>();
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
	
	private void updateUsers() throws IOException, ProtocolException {
		(new PacketGetUsers()).send(sock.getOutputStream());
		Packet p = null;
		while (p == null || !(p instanceof PacketPutUsers)) {
			if (!running) {
				return;
			}
			p = Packet.readPacket(serverSock.getInputStream());
		}
		HashMap<String, UserInfo> us = ((PacketPutUsers) p).getUsers();
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
	
	public HashMap<String, UserInfo> getUsers() {
		synchronized (this) {
			return users;
		}
	}
}
