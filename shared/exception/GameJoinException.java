package shared.exception;

@SuppressWarnings("serial")
public class GameJoinException extends ProtocolException {
	public GameJoinException() {
		super();
	}
	
	public GameJoinException(String s) {
		super(s);
	}
	
	public GameJoinException(String s, Throwable t) {
		super(s, t);
	}
}
