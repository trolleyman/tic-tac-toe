package server;

public enum ClientState {
	INIT_STATE,    // Initial state. Nickname is not known.
	WAITING_STATE, // Waiting state. Client is waiting to choose a user to challenge.
}
