package shared.exception;

import shared.Util;

@SuppressWarnings("serial")
public class EchoException extends ProtocolException {
	public EchoException(byte[] expected, byte[] got) {
		super("Echo did not return the same data."
				+ " Expected 0x" + Util.bytesToHex(expected) + ", got 0x" + Util.bytesToHex(got));
	}
}
