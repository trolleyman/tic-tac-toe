use std::io::{self, Read, Write, ErrorKind};
use std::net::TcpStream;
use std::time::Duration;

use bo::{BigEndian, ReadBytesExt, WriteBytesExt};

use user::Username;

pub struct Packet {
	to: Username,
	from: Username,
	payload: PacketType,
}
impl Packet {
	pub fn new(to: Username, from: Username, payload: PacketType) -> Packet {
		Packet {
			to: to,
			from: from,
			payload: payload,
		}
	}
	pub fn to_server(from: Username, payload: PacketType) -> Packet {
		Packet::new(Username::server(), from, payload)
	}
	pub fn from_server(to: Username, payload: PacketType) -> Packet {
		Packet::new(to, Username::server(), payload)
	}
	
	pub fn get_to(&self) -> &Username {
		&self.to
	}
	pub fn get_from(&self) -> &Username {
		&self.from
	}
	pub fn get_payload(&self) -> &PacketType {
		&self.payload
	}
	
	pub fn send(&self, s: &mut TcpStream) -> io::Result<()> {
		try!(s.write_u8(self.payload.get_id()));
		try!(s.write_u8(self.to.bytes_len()));
		try!(s.write_all(self.to.as_bytes()));
		
		try!(s.write_u8(self.from.bytes_len()));
		try!(s.write_all(self.from.as_bytes()));
		
		let payload = self.payload.as_bytes();
		try!(s.write_u16::<BigEndian>(payload.len() as u16));
		try!(s.write_all(payload));
		Ok(())
	}
	pub fn recieve(s: &mut TcpStream) -> io::Result<Packet> {
		try!(s.set_read_timeout(None));
		Packet::recv(s)
	}
	/// Returns `Ok(None)` if the connection timed out.
	pub fn recieve_timeout(s: &mut TcpStream, timeout: Duration) -> io::Result<Option<Packet>> {
		try!(s.set_read_timeout(Some(timeout)));
		match Packet::recv(s) {
			Ok(p) => Ok(Some(p)),
			Err(ref e) if e.kind() == ErrorKind::TimedOut || e.kind() == ErrorKind::WouldBlock => {
				Ok(None)
			},
			Err(e) => Err(e),
		}
	}
	fn recv(s: &mut TcpStream) -> io::Result<Packet> {
		let id = try!(s.read_u8());
		
		match Packet::recv_rest(s, id) {
			Ok(p) => Ok(p),
			Err(ref e) if e.kind() == ErrorKind::TimedOut || e.kind() == ErrorKind::WouldBlock
				=> Err(io::Error::new(ErrorKind::UnexpectedEof, "unexpected EOF")),
			Err(e) => Err(e),
		}
	}
	
	fn recv_rest(s: &mut TcpStream, id: u8) -> io::Result<Packet> {
		let to_len = try!(s.read_u8());
		let mut to_bytes = vec![0; to_len as usize];
		try!(s.read_exact(&mut to_bytes));
		let to = try!(Username::from_bytes(to_bytes));
		
		let from_len = try!(s.read_u8());
		let mut from_bytes = vec![0; from_len as usize];
		try!(s.read_exact(&mut from_bytes));
		let from = try!(Username::from_bytes(from_bytes));
		
		let payload_len = try!(s.read_u16::<BigEndian>());
		let mut payload = vec![0; payload_len as usize];
		try!(s.read_exact(&mut payload));
		
		let ptype = match PacketType::from_bytes(id, payload) {
			Some(p) => p,
			None => return Err(io::Error::new(
				io::ErrorKind::InvalidData, format!("Packet ID {} is invalid", id))),
		};
		
		Ok(Packet{
			to: to,
			from: from,
			payload: ptype,
		})
	}
}

pub enum PacketType {
	Heartbeat,
	Hello(HelloPacket),
}
impl PacketType {
	pub fn get_id(&self) -> u8 {
		match self {
			&PacketType::Heartbeat => 0,
			&PacketType::Hello(_) => 1,
		}
	}
	pub fn from_bytes(id: u8, payload: Vec<u8>) -> Option<PacketType> {
		match id {
			0 => Some(PacketType::Heartbeat),
			1 => Some(PacketType::Hello(match HelloPacket::from_bytes(payload) {
				Some(hp) => hp,
				None => return None,
			})),
			_ => None
		}
	}
	pub fn as_bytes(&self) -> &[u8] {
		match self {
			&PacketType::Heartbeat => &[],
			&PacketType::Hello(ref hello) => hello.as_bytes(),
		}
	}
}

pub struct HelloPacket {
	payload: Vec<u8>,
}
impl HelloPacket {
	fn from_bytes(payload: Vec<u8>) -> Option<HelloPacket> {
		Some(HelloPacket{
			payload: payload,
		})
	}
	fn as_bytes(&self) -> &[u8] {
		&self.payload
	}
}
