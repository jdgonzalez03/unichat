package model;

public class Model_User_Login {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String hashPassword) {
        this.password = hashPassword;
    }

    public Model_User_Login(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Model_User_Login() {}
}
