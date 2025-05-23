package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    private static PasswordUtils instance;

    private PasswordUtils() {
    }

    public static synchronized PasswordUtils getInstance() {
        if (instance == null) {
            instance = new PasswordUtils();
        }
        return instance;
    }

    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}