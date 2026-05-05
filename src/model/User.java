package model;

public abstract class User {
    protected String username;
    protected String role;

    public User(String username) {
        this.username = username;
    }

    // Getter và Setter ở đây (Tính Encapsulation)
    public String getUsername() { return username; }
}