use std::net::TcpStream;
use std::io::{self, ErrorKind};
use std::thread;
use std::time::Duration;
use std::sync::{Arc, Mutex};

use packet::{Packet, PacketType, PutUsersPacket};
use user::Username;

use ::Server;

pub struct ServerThread {
	server: Arc<Mutex<Server>>,
	stream: TcpStream,
	client: Option<Username>,
}

impl ServerThread {
	pub fn new(server: Arc<Mutex<Server>>, stream: TcpStream) {
		thread::spawn(move || {
			ServerThread {
				server: server,
				stream: stream,
				client: None,
			}.run();
		});
	}
	
	pub fn run(mut self) {
		match self.stream.peer_addr() {
			Ok(addr) => println!("Connected to {}", &addr),
			Err(_) => {},
		}
		
		match self.handle_packets() {
			Ok(()) => {},
			Err(e) => println!("{:?} : {}", e.kind(), e),/*match e.kind() {
				ErrorKind::ConectionReset => {}
			}*/
		}
		
		if let Some(client) = self.client {
			{
				let mut server = self.server.lock().unwrap();
				server.remove_client(&client);
			}
			
			if client.is_user() {
				match self.stream.peer_addr() {
					Ok(addr) => println!("{} left. ({})", &client, &addr),
					Err(_) => println!("{} left.", &client),
				}
			} else {
				match self.stream.peer_addr() {
					Ok(addr) => println!("{} left.", &addr),
					Err(_) => {}
				}
			}
		} else {
			match self.stream.peer_addr() {
				Ok(addr) => println!("{} left.", &addr),
				Err(_) => {}
			}
		}
		
		
	}
	
	fn handle_packets(&mut self) -> io::Result<()> {
		loop {
			match try!(Packet::recieve_timeout(&mut self.stream, Duration::from_millis(1))) {
				Some(p) => {
					// Handle packet
					try!(self.handle_packet(p));
				},
				None => {}
			}
			if let Some(ref client) = self.client {
				try!(Packet::new(client.clone(), Username::server(), PacketType::Heartbeat).send(&mut self.stream));
			}
			thread::sleep(Duration::from_millis(100));
		}
	}

	fn handle_packet(&mut self, p: Packet) -> io::Result<()> {
		use packet::PacketType::*;
		use packet::QuitPacket;
		
		if self.client.is_none() {
			self.client = Some(p.from().clone());
			let mut server = self.server.lock().unwrap();
			match server.add_client(p.from().clone()) {
				Ok(())  => {
					println!("{} joined.", p.from());
				},
				Err(()) => {
					self.client = None;
					let err = format!("{} is already connected.", p.from());
					let _ = Packet::new(p.from().clone(), Username::server(), Quit(QuitPacket::new(err.clone()))).send(&mut self.stream);
					return Err(io::Error::new(ErrorKind::Other, err));
				},
			}
		}
		
		match p.payload() {
			&Quit(ref qp) => {
				return Err(io::Error::new(ErrorKind::Other, qp.msg().clone()));
			},
			&GetUsers => {
				let users = self.server.lock().unwrap().get_users();
				try!(Packet::new(p.from().clone(), Username::server(), PutUsers(
						PutUsersPacket::from_users(users)
					)).send(&mut self.stream));
			},
			&Heartbeat | &PutUsers(_) => {},
		}
		
		Ok(())
	}
}
