package shared.exception;

@SuppressWarnings("serial")
public class ProtocolException extends Exception {
	public ProtocolException() {
		super();
	}
	
	public ProtocolException(String s) {
		super(s);
	}

	public ProtocolException(String s, Throwable e) {
		super(s, e);
	}
}
