package shared.exception;

public enum InvalidUsername {
	LENGTH,
	WHITESPACE,
	UTF8;
	
	public String toString(String nick) {
		switch (this) {
		case LENGTH:
			return "'" + nick + "' is an inavlid username: Username too long.";
		case UTF8:
			return "'" + nick + "' is an inavlid username: UTF8 encoding error: ";
		case WHITESPACE:
			return "'" + nick + "' is an inavlid username: Whitespace not allowed: ";
		default:
			return "'" + nick + "' is an inavlid username.";
		}
	}
}
