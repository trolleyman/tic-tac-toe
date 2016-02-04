package client;

import java.net.InetSocketAddress;
import java.util.HashMap;

public interface LobbyListener {
	public void usersChanged(HashMap<String, InetSocketAddress> newUsers);
}
