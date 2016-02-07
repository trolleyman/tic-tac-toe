package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import shared.Username;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketPutUsers;

public class Lobby implements Runnable {
	private Socket sock;
	private ArrayList<Username> users;
	private boolean running = true;
	private ArrayList<LobbyListener> listeners;
	private InetSocketAddress addr;
	private Username me;
	
	public Lobby(Username me, InetSocketAddress addr) throws IOException, ProtocolException {
		this.me = me;
		this.addr = addr;
		connect();
		this.listeners = new ArrayList<LobbyListener>();
		updateUsers();
		
		Thread t = new Thread(this, "Lobby Thread");
		t.setDaemon(true);
		t.start();
	}
	
	private void connect() throws IOException {
		sock = new Socket(addr.getAddress(), addr.getPort());
		sock.setTcpNoDelay(true);
		sock.setKeepAlive(true);
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
		(new PacketGetUsers(Username.NULL, Username.SERVER)).send(sock.getOutputStream());
		Packet p = null;
		while (p == null || !(p instanceof PacketPutUsers)) {
			if (!running) {
				return;
			}
			p = Packet.readPacket(sock.getInputStream());
		}
		ArrayList<Username> us = new ArrayList<>(Arrays.asList(((PacketPutUsers) p).getUsers()));
		us.remove(me);
		synchronized (this) {
			users = us;
		}
		//System.out.println("users: " + users);
	}
	
	@Override
	public void run() {
		try {
			if (sock == null || sock.isClosed())
				connect();
			
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
			if (sock != null)
				try {
					sock.close();
				} catch (IOException e) {
					// Whatever.
				} finally {
					sock = null;
				}
		}
	}

	public boolean isRunning() {
		return running;
	}
	
	public ArrayList<Username> getUsers() {
		synchronized (this) {
			return users;
		}
	}
}
