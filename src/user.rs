use std::io::{self, ErrorKind};
use std::fmt::{self, Formatter, Display};
use std::error::Error;
use std::string::FromUtf8Error;
use std::convert::{From};
use std::ffi::OsString;

#[derive(Debug)]
pub enum UsernameErrorType {
	Empty,
	Whitespace,
	Length,
	Utf8(FromUtf8Error),
	OsStr,
}
#[derive(Debug)]
pub struct UsernameError {
	estr: String,
	e: UsernameErrorType,
}
impl UsernameError {
	pub fn new(nick: &str, e: UsernameErrorType) -> UsernameError {
		UsernameError {
			estr: String::from(match &e {
				&UsernameErrorType::Empty      => format!("Invalid username: Username is empty"),
				&UsernameErrorType::Whitespace => format!("Invalid username: Username '{}' contains whitespace", nick),
				&UsernameErrorType::Length     => format!("Invalid username: Username '{}' is too long", nick),
				&UsernameErrorType::Utf8(_)    => format!("Invalid username: Username is not valid utf8: '{}'", nick),
				&UsernameErrorType::OsStr      => format!("Invalid username: Username is not valid utf8: '{}'", nick),
			}),
			e: e,
		}
	}
}
impl Display for UsernameError {
	fn fmt(&self, f: &mut Formatter) -> Result<(), fmt::Error> {
		try!(f.write_str(&self.estr));
		Ok(())
	}
}
impl Error for UsernameError {
	fn description(&self) -> &str {
		&self.estr
	}
	fn cause(&self) -> Option<&Error> {
		match &self.e {
			&UsernameErrorType::Utf8(ref e) => Some(e),
			_ => None,
		}
	}
}
impl From<UsernameError> for io::Error {
	fn from(e: UsernameError) -> io::Error {
		io::Error::new(ErrorKind::InvalidData, e)
	}
}

pub struct Username {
	nick: String,
}
impl Username {
	pub fn new(nick: String) -> Result<Username, UsernameError> {
		if nick.is_empty() {
			return Err(UsernameError::new(&nick, UsernameErrorType::Empty));
		}
		if nick.len() >= ::std::u8::MAX as usize {
			return Err(UsernameError::new(&nick, UsernameErrorType::Length));
		}
		if nick.contains(|c: char| c.is_whitespace()) {
			return Err(UsernameError::new(&nick, UsernameErrorType::Whitespace));
		}
		Ok(Username {
			nick: nick,
		})
	}
	pub fn server() -> Username {
		Username {
			nick: " SERVER".into(),
		}
	}
	pub fn is_user(&self) -> bool {
		!self.nick.starts_with(' ')
	}
	
	pub fn from_os_str(s: OsString) -> Result<Username, UsernameError> {
		match s.into_string() {
			Ok(s) => Ok(try!(Username::new(s))),
			Err(e) => Err(UsernameError::new(&e.to_string_lossy(), UsernameErrorType::OsStr)),
		}
	}
	pub fn from_bytes(bytes: Vec<u8>) -> Result<Username, UsernameError> {
		match String::from_utf8(bytes.clone()) {
			Ok(s) => Ok(try!(Username::new(s))),
			Err(e) => Err(UsernameError::new(&String::from_utf8_lossy(&bytes), UsernameErrorType::Utf8(e))),
		}
	}
	pub fn as_bytes(&self) -> &[u8] {
		self.nick.as_bytes()
	}
	pub fn bytes_len(&self) -> u8 {
		self.nick.as_bytes().len() as u8
	}
}
