package shared.exception;

import shared.Username;

@SuppressWarnings("serial")
public class UserNotConnectedException extends ProtocolException {
	public UserNotConnectedException(Username name) {
		super(name.getString() + " is not connected to the server.");
	}
}
