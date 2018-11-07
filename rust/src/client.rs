#![feature(time2, associated_consts)]
extern crate gtk;
extern crate gdk;
extern crate byteorder as bo;

use std::process::exit;
use std::env;
use std::mem;
use std::io::{self, Write, ErrorKind};
use std::net::TcpStream;
use std::thread;
use std::time::{Duration, Instant};

use user::Username;
use gui::Gui;
use packet::{Packet, PacketType};

pub mod user;
pub mod packet;
pub mod connection;
pub mod gui;

fn usage_exit() -> ! {
	let _ = writeln!(io::stderr(), "Usage: client.exe <nick> <port> <machine-name>");
	exit(1);
}

struct ParsedArgs {
	nick: Username,
	port: u16,
	machine_name: String,
}
impl ParsedArgs {
	pub fn new() -> ParsedArgs {
		let mut args = env::args_os();
		if args.len() != 4 {
			let _ = writeln!(io::stderr(), "Error: Invalid number of arguments.");
			usage_exit();
		}
		let _ = args.next();
		
		let nick = match Username::from_os_str(args.next().unwrap()) {
			Ok(u) => u,
			Err(e) => {
				let _ = writeln!(io::stderr(), "Error: {}", e);
				usage_exit();
			}
		};
		
		let port_os = args.next().unwrap();
		let port = match port_os.clone().into_string().map(|s| u16::from_str_radix(&s, 10)) {
			Ok(Ok(u)) => u,
			_ => {
				let _ = writeln!(io::stderr(), "Error: port '{}' is not valid.", port_os.to_string_lossy());
				usage_exit();
			}
		};
		
		let machine_name_os = args.next().unwrap();
		let machine_name = match machine_name_os.into_string() {
			Ok(s) => s,
			Err(os) => {
				let _ = writeln!(io::stderr(), "Error: Machine name '{}' is not a valid utf8 string", os.to_string_lossy());
				usage_exit();
			}
		};
		
		ParsedArgs {
			nick: nick,
			port: port,
			machine_name: machine_name,
		}
	}
}

static mut gui_g: *mut Gui = 0 as *mut _;
pub fn get_gui() -> &'static mut Gui {
	unsafe {
		if gui_g.is_null() {
			panic!("GUI is null.");
		}
		return mem::transmute(gui_g);
	}
}

fn main() {
	let args = ParsedArgs::new();
	// Try to connect to args.machine_name:args.port
	let mut stream = match TcpStream::connect((args.machine_name.as_str(), args.port)) {
		Ok(s) => s,
		Err(e) => {
			let _ = match e.kind() {
				ErrorKind::InvalidInput => writeln!(io::stderr(), "Error: could not resolve '{}'", &args.machine_name),
				ErrorKind::ConnectionRefused => writeln!(io::stderr(), "Error: connection refused on port {}", args.port),
				_ => writeln!(io::stderr(), "Error: {}", e),
			};
			exit(2);
		}
	};
	
	match gtk::init() {
		Ok(_) => {
			println!("GTK initialized.");
		},
		Err(_) => {
			println!("Error: GTK could not be initialized.");
			exit(2);
		},
	}
	
	let mut gui = Gui::new();
	unsafe {
		gui_g = &mut gui as *mut _;
	}
	
	let heartbeat_delay = Duration::from_millis(100);
	let get_users_delay = Duration::from_millis(1000);
	
	let mut last_users_updated = Instant::now() - get_users_delay;
	let mut last_heartbeat_sent = Instant::now() - heartbeat_delay;
	
	while !get_gui().should_quit() {
		gtk::main_iteration_do(false);
		
		// Recieve packets
		match match Packet::recieve_timeout(&mut stream, Duration::from_millis(1)) {
			Ok(v) => v,
			Err(e) => {
				println!("Error: {}", e);
				::get_gui().quit();
				break;
			},
		} {
			Some(p) => {
				// Handle packet
				handle_packet(p, &mut stream);
			},
			None => {}
		}
		
		// Send Heartbeat Packet
		if last_heartbeat_sent.elapsed() > heartbeat_delay {
			last_heartbeat_sent = Instant::now();
			let _ = Packet::new(Username::server(), args.nick.clone(), PacketType::Heartbeat).send(&mut stream);
		}
		if last_users_updated.elapsed() > get_users_delay {
			last_users_updated = Instant::now();
			let _ = Packet::new(Username::server(), args.nick.clone(), PacketType::GetUsers).send(&mut stream);
		}
		thread::sleep(Duration::from_millis(10));
	}
}

fn handle_packet(p: Packet, _stream: &mut TcpStream) {
	use packet::PacketType::*;
	
	match p.payload() {
		&Heartbeat | &GetUsers => {},
		
		&Quit(ref p) => {
			println!("Error: {}", p.msg());
			::get_gui().quit()
		},
		&PutUsers(ref up) => ::get_gui().set_users(up.users().into()),
	}
}
