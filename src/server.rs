#![feature(associated_consts)]
extern crate gtk;
extern crate gdk;
extern crate byteorder as bo;

use std::net::{TcpListener, TcpStream};
use std::io::{self, Write};
use std::thread;
use std::env;
use std::process::exit;
use std::time::Duration;

use packet::{Packet, PacketType};
use user::Username;

pub mod user;
pub mod packet;
pub mod connection;

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
	
	let listener = match TcpListener::bind(("localhost", port)) {
		Ok(l) => l,
		Err(e) => {
			println!("Could not open tcp listener on port {}: {}", port, e);
			exit(1);
		}
	};
	
	for stream in listener.incoming() {
		match stream {
			Ok(stream) => {
				thread::spawn(move|| {
					handle_client(stream);
				});
			}
			Err(e) => {
				let _ = writeln!(io::stderr(), "Error recieving incoming connection: {}", e);
			}
		}
	}
}

fn handle_client(mut stream: TcpStream) {
	match stream.peer_addr() {
		Ok(addr) => println!("Connected to {}", &addr),
		Err(_) => {}
	}
	
	let mut client = Username::unknown();
	
	loop {
		match Packet::new(client.clone(), Username::server(), PacketType::Heartbeat).send(&mut stream) {
			Ok(()) => {},
			Err(e) => {
				let _ = writeln!(io::stderr(), "Error occured sending packet to user: {}", e);
				break;
			},
		}
		thread::sleep(Duration::from_millis(100));
	}
}
