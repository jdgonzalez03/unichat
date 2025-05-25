package model;

public class Model_User_With_Status {
    private Model_User user;
    private boolean online;

    public Model_User_With_Status(Model_User user, boolean online) {
        this.user = user;
        this.online = online;
    }

    public Model_User_With_Status(Model_User user) {}

    public Model_User getUser() {
        return user;
    }

    public void setUser(Model_User user) {
        this.user = user;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
