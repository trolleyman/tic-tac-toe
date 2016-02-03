package shared.exception;

import shared.Instruction;

@SuppressWarnings("serial")
public class IllegalInstructionException extends ProtocolException {
	private Instruction i;

	public IllegalInstructionException(Instruction i) {
		super("Illegal instruction: " + i);
		this.i = i;
	}

	public Instruction getInstruction() {
		return i;
	}
}
