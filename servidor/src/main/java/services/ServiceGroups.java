package services;

import connection.ConnectionDB;
import model.Model_Create_Group;
import model.Model_Response;
import model.Model_User_With_Status;
import utils.Logger;
import utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServiceGroups {
    private final Connection connection;
    Logger logger = Logger.getInstance();
    PasswordUtils passwordUtils = PasswordUtils.getInstance();

    public ServiceGroups() {
        this.connection = ConnectionDB.getInstance().getConnection();
    }

    Model_Response createGroup(Model_Create_Group group){
        Model_Response response = new Model_Response();

        try{
            PreparedStatement checkStmt = connection.prepareStatement(CHECK_GROUP_IN_DB);
            checkStmt.setString(1, group.getName());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                response.setSuccess(false);
                response.setMessage("El grupo ya existe y no es posible crearlo, intenta con otro nombre.");
                logger.log("El usuario: " + group.getCreator_email() + " esta intentando crear un grupo, pero el nombre ya existe");
                resultSet.close();
                checkStmt.close();
            }

            resultSet.close();
            checkStmt.close();

            PreparedStatement createGroupStmt = connection.prepareStatement(CREATE_GROUP);
            createGroupStmt.setString(1, group.getName());
            createGroupStmt.setString(2, group.getCreator_email());
            createGroupStmt.setString(3, group.getCreator_email());

            int rowsInseted = createGroupStmt.executeUpdate();
            createGroupStmt.close();

            if (rowsInseted > 0) {
                response.setSuccess(true);
                response.setMessage("El grupo " + group.getName() + " se ha creado con exito. Espera que los chicos acepten la invitacion.");
                logger.log("El grupo " + group.getName() + " se ha creado con exito.");
            }

            //Crear invitaciones
            PreparedStatement requestMembersStmt = connection.prepareStatement(SEND_REQUEST_TO_MEMBERS);
            for(Model_User_With_Status member : group.getMembers()){
                if (!member.getUser().getEmail().equals(group.getCreator_email())){
                    requestMembersStmt.setString(1, group.getName());
                    requestMembersStmt.setString(2, member.getUser().getEmail());
                    requestMembersStmt.executeUpdate();
                    logger.log("Invitacion para unirse al grupo " + group.getName() + " ha sido enviada a: " + member.getUser().getEmail());
                }
            }

        }catch (Exception e){
            logger.log("Algo salió mal creando el grupo: " + group.getName() + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    private final String CHECK_GROUP_IN_DB = "SELECT 1 FROM groups WHERE name = ?";
    private final String CREATE_GROUP = "INSERT INTO groups (name, description, creator_email) VALUES (?, ?, ?)";
    private final String SEND_REQUEST_TO_MEMBERS = "INSERT INTO group_invitations (group_name, invited_email) VALUES (?, ?)";
}
