package shared.exception;

public enum InvalidUsername {
	LENGTH,
	WHITESPACE,
	UTF8,
	USER_ALREADY_LOGGED_IN;
	
	public String toString(String nick) {
		switch (this) {
		case LENGTH:
			return "Inavlid username: " + nick;
		case UTF8:
			return "Inavlid username: UTF8 encoding error: " + nick;
		case WHITESPACE:
			return "Inavlid username: Whitespace not allowed: " + nick;
		case USER_ALREADY_LOGGED_IN:
			return "Inavlid username: User already logged in: " + nick;
		default:
			return "Invalid username: " + nick;
		}
	}
}
