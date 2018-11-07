#![feature(associated_consts)]
extern crate gtk;
extern crate gdk;
extern crate byteorder as bo;

use std::net::TcpListener;
use std::io::{self, Write};
use std::env;
use std::process::exit;
use std::collections::BTreeMap;
use std::collections::btree_map::Entry::{Vacant};
use std::sync::{Arc, Mutex};

use packet::{Packet};
use user::Username;
use server_thread::ServerThread;

pub mod user;
pub mod packet;
pub mod connection;

pub mod server_thread;

fn usage_exit() -> ! {
	let _ = writeln!(io::stderr(), "Usage: server.exe <port>");
	exit(1);
}

fn parse_port() -> u16 {
	if env::args_os().len() != 2 {
		usage_exit();
	}
	let mut args_os = env::args_os();
	args_os.next();
	
	let port_os = args_os.next().unwrap();
	let port = match port_os.clone().into_string().map(|s| u16::from_str_radix(&s, 10)) {
		Ok(Ok(u)) => u,
		_ => {
			let _ = writeln!(io::stderr(), "Error: port '{}' is not valid.", port_os.to_string_lossy());
			usage_exit();
		}
	};
	
	return port;
}

pub fn main() {
	let port = parse_port();
	println!("Opening socket on port {}.", port);
	
	let server = Server::new(port);
	server.run();
}

pub struct Server {
	port: u16,
	// Holds clients + their packet queues
	clients: BTreeMap<Username, Vec<Packet>>,
}
impl Server {
	pub fn new(port: u16) -> Server {
		Server {
			port: port,
			clients: BTreeMap::new(),
		}
	}
	
	pub fn run(self) {
		let listener = match TcpListener::bind(("localhost", self.port)) {
			Ok(l) => l,
			Err(e) => {
				println!("Could not open tcp listener on port {}: {}", self.port, e);
				exit(1);
			}
		};
		
		let mutex = Mutex::new(self);
		let arc = Arc::new(mutex);
		
		for stream in listener.incoming() {
			match stream {
				Ok(stream) => {
					ServerThread::new(arc.clone(), stream);
				}
				Err(e) => {
					let _ = writeln!(io::stderr(), "Error recieving incoming connection: {}", e);
				}
			}
		}
	}
	
	pub fn get_users(&mut self) -> Vec<Username> {
		self.clients.keys().cloned().collect()
	}
	
	/// Adds a new client to the server
	/// Returns Err if the client already exists
	pub fn add_client(&mut self, client: Username) -> Result<(), ()> {
		match self.clients.entry(client) {
			Vacant(e) => { e.insert(Vec::new()); },
			_ => return Err(()),
		}
		Ok(())
	}
	
	pub fn get_client_queue(&mut self, client: &Username) -> Option<&mut Vec<Packet>> {
		self.clients.get_mut(client)
	}
	
	pub fn remove_client(&mut self, client: &Username) {
		self.clients.remove(client);
	}
}
