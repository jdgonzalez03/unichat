package model;

public class Model_Request_UserList {
    private String username;
    private String email;

    public Model_Request_UserList(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public Model_Request_UserList() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
