
use user::Username;

//use gtk::prelude::*;
//use gtk::{Window, WindowType};
use gtk::*;

pub struct Gui {
	lobby: Window,
	lobby_tree: TreeView,
	lobby_store: ListStore,
	
	game: Window,
	quit: bool,
	users: Vec<Username>
}

impl Gui {
	fn new_lobby() -> (TreeView, ListStore) {
		let store = ListStore::new(&[Type::String]);
		
		let tree = TreeView::new_with_model(&store);
		let user_col = TreeViewColumn::new();
		user_col.set_title("User");
		tree.append_column(&user_col);
		
		(tree, store)
	}
	
	pub fn new() -> Gui {
		let lobby = Window::new(WindowType::Toplevel);
		lobby.set_title("Tic-Tac-Toe Lobby");
		
		lobby.connect_destroy(|_| {
			::get_gui().quit();
		});
		let (lobby_tree, lobby_store) = Gui::new_lobby();
		lobby.add(&lobby_tree);
		lobby.show_all();
		
		let game = Window::new(WindowType::Toplevel);
		game.set_title("Tic-Tac-Toe Game");
		
		game.connect_destroy(|_| {
			::get_gui().quit();
		});
		
		Gui {
			lobby: lobby,
			lobby_tree: lobby_tree,
			lobby_store: lobby_store,
			
			game: game,
			quit: false,
			users: Vec::new(),
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
	
	fn update(&mut self) {
		self.lobby_store.clear();
		for user in &self.users {
			self.lobby_store.set_value(&self.lobby_store.append(), 0, &Value::from(&String::from(user.name())));
		}
	}
	
	pub fn set_users(&mut self, users: Vec<Username>) {
		println!("{:?}", users);
		self.users = users;
		self.update();
	}
}
