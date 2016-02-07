package shared;

import shared.exception.InvalidUsernameException;

public class Username {
	static {
		try {
			SERVER = new Username("");
		} catch (InvalidUsernameException e) {
			throw new Error(e.getMessage());
		}
	}
	static public final Username SERVER;
	
	private byte[] bytes;
	private String name;
	
	public Username(String name) throws InvalidUsernameException {
		this.name = name;
		Util.assertValidUsername(name);
		this.bytes = Util.utf8Encode(name);
	}
	public Username(byte[] bytes) throws InvalidUsernameException {
		this.bytes = bytes;
		this.name = Util.assertValidUsername(bytes);
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	public String getString() {
		return name;
	}
	
	public boolean isServer() {
		return name.length() == 0;
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null &&
		     ((o instanceof Username && ((Username) o).getString().equalsIgnoreCase(name))
		   || (o instanceof String && ((String) o).equalsIgnoreCase(name)));
	}
}
