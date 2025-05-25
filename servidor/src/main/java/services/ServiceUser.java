package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connection.ConnectionDB;
import model.Model_User_Login;
import model.Model_User_Register;
import utils.Logger;
import model.Model_Response;
import model.Model_User;
import utils.PasswordUtils;

public class ServiceUser {
    private final Connection connection;
    Logger logger = Logger.getInstance();
    PasswordUtils passwordUtils = PasswordUtils.getInstance();

    public ServiceUser() {
        this.connection = ConnectionDB.getInstance().getConnection();
    }

    public Model_Response register(Model_User_Register userData) {
        Model_Response response = new Model_Response();

        try {
            // Verificar si el email ya existe
            PreparedStatement checkStmt = connection.prepareStatement(CHECK_USER_IN_DB);
            checkStmt.setString(1, userData.getEmail());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                response.setSuccess(false);
                response.setMessage("El usuario con el correo electrónico " + userData.getEmail() + " ya se encuentra registrado.");
                logger.log("El usuario con el correo electrónico " + userData.getEmail() + " ya se encuentra registrado.");
                resultSet.close();
                checkStmt.close();
                return response;
            }
            resultSet.close();
            checkStmt.close();

            // Si no existe, crear el nuevo usuario
            String username = userData.getUsername();
            String password = passwordUtils.hashPassword(userData.getPassword());
            String email = userData.getEmail();

            PreparedStatement createStmt = connection.prepareStatement(CREATE_USER);
            createStmt.setString(1, username);
            createStmt.setNull(2, java.sql.Types.VARCHAR); // Imagen en NULL
            createStmt.setString(3, email);
            createStmt.setString(4, password);

            int rowsInserted = createStmt.executeUpdate();
            createStmt.close();

            if (rowsInserted > 0) {
                response.setSuccess(true);
                response.setMessage("Usuario registrado exitosamente.");
                response.setData(new Model_User(username, null , email));
                logger.log("Usuario: " + email + " registrado exitosamente.");
            } else {
                response.setSuccess(false);
                response.setMessage("No se pudo registrar el usuario.");
                response.setData(null);
            }

        } catch (Exception e) {
            logger.log("Algo salió mal registrando al usuario. Error: " + e.getMessage());
            response.setSuccess(false);
            response.setMessage("Error al registrar el usuario.");
        }

        return response;
    }

    public Model_Response login(Model_User_Login userData) {
        Model_Response response = new Model_Response();

        try {
            PreparedStatement checkStmt = connection.prepareStatement(CHECK_USER_IN_DB);
            checkStmt.setString(1, userData.getEmail());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String image = resultSet.getString("image");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");

                //Validate password
                boolean isCorrectPassword = passwordUtils.verifyPassword(userData.getPassword(), password);

                if (isCorrectPassword) {
                    Model_User user = new Model_User(username, image, email);
                    response.setSuccess(true);
                    response.setMessage("Login exitoso para el usuario: " + userData.getEmail());
                    response.setData(user);
                    logger.log("Login exitoso.");
                }else {
                    response.setSuccess(false);
                    response.setMessage("Contraseña incorrecta para el usuario: " + userData.getEmail());
                    response.setData(null);
                    logger.log("Contraseña incorrecta");
                }
            } else {
                response.setSuccess(false);
                response.setMessage("El usuario no existe.");
                response.setData(null);
                logger.log("El usuario no existe: ."+ userData.getEmail());
            }
        } catch (Exception e) {
            logger.log("Algo salió mal al iniciar sesión. Error: " + e.getMessage());
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Error al iniciar sesión con el usuario: " + userData.getEmail());
        }

        return response;
    }

    public List<Model_User> allUsers() {
        List<Model_User> users = new ArrayList<Model_User>();
        try {
            PreparedStatement checkStmt = connection.prepareStatement(ALL_USERS);
            ResultSet resultSet = checkStmt.executeQuery();
            while (resultSet.next()) {
                Model_User user = new Model_User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("image"),
                        resultSet.getString("email"),
                        resultSet.getString("password")
                );
                users.add(user);
            }
            logger.log("Todos los usuarios: " + users.size());

        }catch (Exception e) {
            e.printStackTrace();
            logger.log("Algo salió mal obteniendo todos los usuarios. Error: " + e.getMessage());
        }

        return users;
    }

    private final String CHECK_USER_IN_DB = "SELECT * FROM users WHERE email = ?";
    private final String CREATE_USER = "INSERT INTO users (username, image, email, password) VALUES (?, ?, ?, ?)";
    private final String ALL_USERS = "SELECT * FROM users";
}
