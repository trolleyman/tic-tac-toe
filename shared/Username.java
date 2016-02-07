package shared;

import java.util.Locale;

import shared.exception.InvalidUsernameException;

public class Username {
	static {
		try {
			SERVER = new Username(" ");
			NULL = new Username("");
		} catch (InvalidUsernameException e) {
			throw new Error(e.getMessage());
		}
	}
	static public final Username SERVER;
	static public final Username NULL;
	
	private byte[] bytes;
	private String name;
	private String lowerCaseName;
	
	public Username(String name) throws InvalidUsernameException {
		this.name = name;
		this.lowerCaseName = name.toLowerCase(Locale.ROOT);
		Util.assertValidUsername(name);
		this.bytes = Util.utf8Encode(name);
	}
	public Username(byte[] bytes) throws InvalidUsernameException {
		this.bytes = bytes;
		this.name = Util.assertValidUsername(bytes);
		this.lowerCaseName = name.toLowerCase(Locale.ROOT);
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	public String getString() {
		return name;
	}
	
	/**
	 * If the username is just a simple username representing a client.
	 * @return
	 */
	public boolean isUser() {
		return !(isNull() || isServer());
	}
	public boolean isNull() {
		return this.equals(NULL);
	}
	public boolean isServer() {
		return this.equals(SERVER);
	}
	
	@Override
	public String toString() {
		return getString();
	}
	
	@Override
	public int hashCode() {
		return lowerCaseName.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null &&
		     ((o instanceof Username && ((Username) o).getString().equalsIgnoreCase(name))
		   || (o instanceof String && ((String) o).equalsIgnoreCase(name)));
	}
}