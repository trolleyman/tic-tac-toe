package shared;

import java.net.InetSocketAddress;

public class UserInfo {
	public UserInfo(InetSocketAddress serverAddr, InetSocketAddress listenAddr) {
		this.serverAddr = serverAddr;
		this.listenAddr = listenAddr;
	}
	/**
	 * Address that the client communicates with the server from
	 */
	public InetSocketAddress serverAddr;
	/**
	 * Address that the client listens for game requests on
	 */
	public InetSocketAddress listenAddr;
}
