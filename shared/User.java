package shared;

public class User {
	public String nick;
	
	public User(String nick) {
		this.nick = nick;
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof User && ((User)o).nick == nick;
	}
}
