package shared.exception;

@SuppressWarnings("serial")
public class UnknownInstructionException extends ProtocolException {
	private int i;

	public UnknownInstructionException(int i) {
		super("Unknown instruction: " + i);
		this.i = i;
	}

	public int getInstruction() {
		return i;
	}
}
