package client;

import java.util.ArrayList;

import shared.Username;

public interface LobbyListener {
	public void usersChanged(ArrayList<Username> users);
}
