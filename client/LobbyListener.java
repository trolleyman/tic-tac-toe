package client;

import shared.Username;

public interface LobbyListener {
	public void usersChanged(Username[] newUsers);
}
