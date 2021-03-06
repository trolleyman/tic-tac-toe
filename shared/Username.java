package shared;

import java.util.Locale;

import shared.exception.InvalidUsernameException;

public class Username implements Comparable<Username> {
	static {
		try {
			LOBBY = new Username(" LOBBY");
			SERVER = new Username(" SERVER");
			NULL = new Username(" NULL");
		} catch (InvalidUsernameException e) {
			throw new Error(e.getMessage());
		}
	}
	static public final Username SERVER;
	static public final Username LOBBY;
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
	public String getLowerCase() {
		return lowerCaseName;
	}
	
	/**
	 * If the username is just a simple username representing a client.
	 * @return
	 */
	public boolean isUser() {
		return !(getString().startsWith(" "));
	}
	public boolean isNull() {
		return this.equals(NULL);
	}
	public boolean isServer() {
		return this.equals(SERVER);
	}
	public boolean isLobby() {
		return this.equals(LOBBY);
	}
	
	@Override
	public String toString() {
		if (!isUser()) return getString().trim();
		else           return getString();
	}
	
	@Override
	public int hashCode() {
		return lowerCaseName.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null &&
		     ((o instanceof Username && ((Username) o).getLowerCase().equalsIgnoreCase(lowerCaseName))
		   || (o instanceof String && ((String) o).equalsIgnoreCase(lowerCaseName)));
	}
	
	@Override
	public int compareTo(Username o) {
		return o.getLowerCase().compareToIgnoreCase(lowerCaseName);
	}
}
