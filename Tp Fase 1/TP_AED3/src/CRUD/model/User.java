package CRUD.model;

public class User {
	
	private int id;
	private String username;
	private String email;
	private String password;
	private boolean administrator;

	public User() {}

	public User(int id, String username, String email, String password, boolean administrator) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.administrator = administrator;
	}

	// construtor para criação (sem id)
	public User(String username, String email, String password, boolean administrator) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.administrator = administrator;
	}

	public int getID() { return id; }
	public String getEmail() { return email; }
	public String getUsername() { return username; }
	public String getPassword() { return password; }
	public boolean isAdministrator() { return administrator; }

	public void setID(int id) { this.id = id; }
	public void setEmail(String email) { this.email = email; }
	public void setUsername(String username) { this.username = username; }
	public void setPassword(String password) { this.password = password; }
	public void setAdministrator(boolean administrator) { this.administrator = administrator; }
}