extern crate gtk;

use std::process::exit;

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
