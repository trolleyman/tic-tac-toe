package shared.exception;

public enum InvalidUsername {
	LENGTH,
	WHITESPACE,
	UTF8;
	
	public String toString(String nick) {
		switch (this) {
		case LENGTH:
			return "Inavlid username: Username too long: " + nick;
		case UTF8:
			return "Inavlid username: UTF8 encoding error: " + nick;
		case WHITESPACE:
			return "Inavlid username: Whitespace not allowed: " + nick;
		default:
			return "Invalid username: " + nick;
		}
	}
}
