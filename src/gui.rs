
use gtk::prelude::*;
use gtk::{Window, WindowType};
use gdk;

pub struct Gui {
	lobby: Window,
	game: Window,
	quit: bool,
}

impl Gui {
	pub fn new() -> Gui {
		let lobby = Window::new(WindowType::Toplevel);
		lobby.set_title("Tic-Tac-Toe Lobby");
		
		lobby.connect_destroy(|_| {
			::get_gui().quit();
		});
		lobby.show_all();
		
		let game = Window::new(WindowType::Toplevel);
		game.set_title("Tic-Tac-Toe Game");
		
		game.connect_destroy(|_| {
			::get_gui().quit();
		});
		
		lobby.connect_key_press_event(|_, e| {
			let mut e2 = e.clone();
			let key = e2.as_mut();
			
			println!("keypress: {:#8x} : {} : {}", key.keyval, gdk::keyval_to_unicode(key.keyval).unwrap_or(' '), gdk::keyval_name(key.keyval).unwrap_or("".into()));
			
			Inhibit(false)
		});
		game.connect_key_press_event(|_, e| {
			let mut e2 = e.clone();
			let key = e2.as_mut();
			
			println!("keypress: {:#8x} : {} : {}", key.keyval, gdk::keyval_to_unicode(key.keyval).unwrap_or(' '), gdk::keyval_name(key.keyval).unwrap_or("".into()));
			
			Inhibit(false)
		});
		
		Gui {
			lobby: lobby,
			game: game,
			quit: false,
		}
	}
	
	pub fn show_lobby(&self) {
		self.game.hide();
		self.lobby.show_all();
	}
	pub fn show_game(&self) {
		self.lobby.hide();
		self.game.show_all();
	}
	
	pub fn quit(&mut self) {
		println!("Quitting...");
		self.quit = true;
	}
	pub fn should_quit(&self) -> bool {
		self.quit
	}
}
