package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import shared.Username;
import shared.exception.ProtocolException;
import shared.packet.Packet;
import shared.packet.PacketErr;
import shared.packet.PacketGetUsers;
import shared.packet.PacketPutUsers;

public class Lobby implements Runnable {
	private Socket sock;
	private volatile ArrayList<Username> users;
	private boolean running = true;
	private volatile ArrayList<LobbyListener> listeners;
	private InetSocketAddress addr;
	private Username me;
	private volatile Thread t;
	
	public Lobby(Username me, InetSocketAddress addr) throws IOException, ProtocolException {
		this.me = me;
		this.addr = addr;
		connect();
		listeners = new ArrayList<LobbyListener>();
		updateUsers();
		
		t = new Thread(this, "Lobby Thread");
		t.setDaemon(true);
		t.start();
	}
	
	public void interrupt() {
		t.interrupt();
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
		(new PacketGetUsers(Username.LOBBY, Username.SERVER)).send(sock.getOutputStream());
		Packet p = null;
		while (p == null || !(p instanceof PacketPutUsers)) {
			if (!running) {
				return;
			}
			p = Packet.readPacket(sock.getInputStream());
			if (p instanceof PacketErr) {
				throw new ProtocolException("Error: " + ((PacketErr) p).getError());
			}
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
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		} catch (ProtocolException e) {
			
		} catch (IOException e) {
			users = null;
			synchronized (listeners) {
				for (LobbyListener l : listeners)
					l.usersChanged(users);
			}
			return;
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
