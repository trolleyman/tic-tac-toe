use std::io::{self, Cursor, Read, Write, ErrorKind};
use std::net::TcpStream;
use std::time::Duration;

use bo::{BigEndian, ReadBytesExt, WriteBytesExt};

use user::Username;

macro_rules! try_option {
	( $x:expr ) => {
		match $x {
			Some(y) => y,
			None    => return None,
		}
	}
}

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
	
	pub fn to(&self) -> &Username {
		&self.to
	}
	pub fn from(&self) -> &Username {
		&self.from
	}
	pub fn payload(&self) -> &PacketType {
		&self.payload
	}
	
	pub fn send(&self, s: &mut TcpStream) -> io::Result<()> {
		if self.payload != PacketType::Heartbeat {
			//println!("Sent packet {:?} to {} from {}", &self.payload, &self.to, &self.from);
		}
		
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
	/// Returns `Ok(None)` if the read timed out.
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
		
		if ptype != PacketType::Heartbeat {
			//println!("Recv packet {:?} to {} from {}", &ptype, &to, &from);
		}
		
		Ok(Packet{
			to: to,
			from: from,
			payload: ptype,
		})
	}
}

#[derive(Debug, PartialEq, Eq)]
pub enum PacketType {
	Quit(QuitPacket),
	Heartbeat,
	GetUsers,
	PutUsers(PutUsersPacket),
}
impl PacketType {
	pub fn get_id(&self) -> u8 {
		match self {
			&PacketType::Quit(_)     => 0,
			&PacketType::Heartbeat   => 1,
			&PacketType::GetUsers    => 2,
			&PacketType::PutUsers(_) => 3,
		}
	}
	pub fn from_bytes(id: u8, payload: Vec<u8>) -> Option<PacketType> {
		match id {
			0 => Some(PacketType::Quit(QuitPacket::from_bytes(payload))),
			1 => Some(PacketType::Heartbeat),
			2 => Some(PacketType::GetUsers),
			3 => Some(PacketType::PutUsers(match PutUsersPacket::from_bytes(payload) {
				Some(p) => p,
				None    => return None,
			})),
			_ => None
		}
	}
	pub fn as_bytes(&self) -> &[u8] {
		match self {
			&PacketType::Quit(ref qp) => qp.as_bytes(),
			&PacketType::Heartbeat => &[],
			&PacketType::GetUsers => &[],
			&PacketType::PutUsers(ref pu) => pu.as_bytes(),
		}
	}
}

#[derive(Debug, PartialEq, Eq)]
pub struct PutUsersPacket {
	payload: Vec<u8>,
	users: Vec<Username>,
}
impl PutUsersPacket {
	pub fn from_users(users: Vec<Username>) -> PutUsersPacket {
		let mut payload = Vec::with_capacity(users.len() * 16);
		let _ = payload.write_u16::<BigEndian>(users.len() as u16);
		
		for u in &users {
			let _ = payload.write_u8(u.bytes_len());
			let _ = payload.write_all(u.as_bytes());
		}
		
		PutUsersPacket {
			payload: payload,
			users: users,
		}
	}
	
	pub fn from_bytes(payload: Vec<u8>) -> Option<PutUsersPacket> {
		let mut rdr = Cursor::new(payload);
		let users_len = try_option!(rdr.read_u16::<BigEndian>().ok());
		let mut users = Vec::with_capacity(users_len as usize);
		
		for _ in 0..users_len {
			let user_len = try_option!(rdr.read_u8().ok());
			let mut user_bytes = vec![0; user_len as usize];
			try_option!(rdr.read_exact(&mut user_bytes).ok());
			let user = match Username::from_bytes(user_bytes) {
				Ok(u) => u,
				_ => return None,
			};
			users.push(user);
		}
		
		Some(PutUsersPacket {
			payload: rdr.into_inner(),
			users: users,
		})
	}
	
	pub fn as_bytes(&self) -> &[u8] {
		&self.payload
	}
	pub fn users(&self) -> &[Username] {
		&self.users
	}
}

#[derive(Debug, PartialEq, Eq)]
pub struct QuitPacket {
	msg: String,
}
impl QuitPacket {
	pub fn new(msg: String) -> QuitPacket {
		QuitPacket {
			msg: msg,
		}
	}
	pub fn from_bytes(payload: Vec<u8>) -> QuitPacket {
		QuitPacket {
			msg: String::from_utf8_lossy(&payload).into_owned(),
		}
	}
	pub fn as_bytes(&self) -> &[u8] {
		self.msg.as_bytes()
	}
	
	pub fn msg(&self) -> &str {
		&self.msg
	}
}
