package shared.exception;

@SuppressWarnings("serial")
public class InvalidUsernameException extends ProtocolException {
	private String username;
	
	public InvalidUsernameException(String username, InvalidUsername reason) {
		super(reason.toString(username));
		this.username = username;
	}
	
	public InvalidUsernameException(String username, InvalidUsername reason, Throwable t) {
		super(reason.toString(username), t);
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}
}
