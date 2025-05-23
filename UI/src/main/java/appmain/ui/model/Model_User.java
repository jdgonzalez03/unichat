package appmain.ui.model;


public class Model_User {
    private int id;
    private String username;
    private String image;
    private String email;
    private String password;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Model_User(int id, String username, String image, String email, String password) {
        this.id = id;
        this.username = username;
        this.image = image;
        this.email = email;
        this.password = password;
    }

    public Model_User(String username, String image, String email) {
        this.username = username;
        this.image = image;
        this.email = email;
    }

}
