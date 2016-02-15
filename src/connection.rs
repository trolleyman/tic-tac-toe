
use std::ops::Deref;
use std::net::{TcpStream, ToSocketAddrs};
use std::io;

pub struct Connection {
	stream: TcpStream,
}
impl Connection {
	pub fn new<A: ToSocketAddrs>(addr: A) -> io::Result<Connection> {
		match TcpStream::connect(addr) {
			Ok(stream) => {
				Ok(Connection{
					stream: stream,
				})
			},
			Err(e) => {
				Err(e)
			}
		}
	}
}
impl Deref for Connection {
	type Target = TcpStream;
	fn deref(&self) -> &TcpStream {
		&self.stream
	}
}
