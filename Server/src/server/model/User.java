package server.model;

public class User {
    private int id;
    private String username;
    private String password; // Added
    private String fullName;
    private String email;    // Added
    private boolean isAdmin;

    public User() {
    }

    // Existing constructor
    public User(int id, String username, String fullName, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.isAdmin = isAdmin;
    }

    // New, more complete constructor for registration
    public User(int id, String username, String password, String fullName, String email, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.isAdmin = isAdmin;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
}
