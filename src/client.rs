extern crate gtk;
extern crate gdk;
extern crate byteorder as bo;

use std::process::exit;
use std::env;
use std::mem;
use std::io::{self, Write, ErrorKind};
use std::net::TcpStream;
use std::thread;
use std::time::Duration;

use user::Username;
use gui::Gui;

pub mod user;
pub mod packet;
pub mod connection;
pub mod gui;

pub fn usage_exit() -> ! {
	let _ = writeln!(io::stderr(), "Usage: client.exe <nick> <port> <machine-name>");
	exit(1);
}

pub struct ParsedArgs {
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
	let stream = match TcpStream::connect((args.machine_name.as_str(), args.port)) {
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
	
	while !get_gui().should_quit() {
		gtk::main_iteration_do(false);
		thread::sleep(Duration::from_millis(10));
	}
}
