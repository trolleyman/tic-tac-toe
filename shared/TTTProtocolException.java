package shared;
import java.io.IOException;

@SuppressWarnings("serial")
public class TTTProtocolException extends IOException {
	public TTTProtocolException() {
		super();
	}
	
	public TTTProtocolException(String s) {
		super(s);
	}

	public TTTProtocolException(String s, Throwable e) {
		super(s, e);
	}
}
