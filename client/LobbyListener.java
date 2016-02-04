package client;

import java.util.HashMap;

import shared.UserInfo;

public interface LobbyListener {
	public void usersChanged(HashMap<String, UserInfo> newUsers);
}
