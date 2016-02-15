extern crate gtk;
extern crate byteorder as bo;

use std::process::exit;

pub mod user;
pub mod packet;
pub mod connection;

fn main() {
	match gtk::init() {
		Ok(_) => {
			println!("GTK initialized.");
		},
		Err(_) => {
			println!("Error: GTK could not be initialized.");
			exit(1);
		},
	}
	
	
}
