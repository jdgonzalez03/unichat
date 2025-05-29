package services;

import connection.ConnectionDB;
import model.*;
import utils.Logger;
import utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    public List<Model_Join_Group> getAllPendingRequests(Model_User user){
        List<Model_Join_Group> requests = new ArrayList<>();

        try{
            PreparedStatement checkStmt = connection.prepareStatement(GET_ALL_PENDING_REQUESTS);
            checkStmt.setString(1, user.getEmail());

            ResultSet resultSet = checkStmt.executeQuery();
            while (resultSet.next()) {
                Model_Join_Group request = new Model_Join_Group();
                request.setName_group(resultSet.getString("group_name"));
                request.setDescription_group(resultSet.getString("description_group"));
                request.setCreator_group(resultSet.getString("creator_group"));

                requests.add(request);
            }
        } catch (Exception e) {
            logger.log("Algo salió mal obteniendo las solicitudes con estado pendiente");
            e.printStackTrace();
        }

        return requests;
    }

    public Model_Response responsePendingRequest(Model_Join_Group_Response data) {
        Model_Response response = new Model_Response();
        String invitedEmail = data.getInvitedEmail();
        String groupName = data.getGroupName();

        try {
            PreparedStatement stmt;
            if (data.getAccepted()) {
                logger.log("estamos aceptando");
                stmt = connection.prepareStatement(UPDATE_TO_ACCEPTED_PENDING_REQUEST);
            } else {
                logger.log("estamos rechazando");

                stmt = connection.prepareStatement(UPDATE_TO_DECLINED_PENDING_REQUEST);
            }
            stmt.setString(1, groupName);
            stmt.setString(2, invitedEmail);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                response.setSuccess(false);
                response.setMessage("No se encontró ninguna solicitud pendiente para actualizar.");
                return response;
            }

            // 2. Si fue aceptada, insertar en group_members
            if (data.getAccepted()) {
                PreparedStatement insertStmt = connection.prepareStatement(INSERT_GROUP_MEMBER);
                insertStmt.setString(1, groupName);
                insertStmt.setString(2, invitedEmail);
                insertStmt.executeUpdate();

                response.setSuccess(true);
                response.setMessage("Has aceptado la invitación al grupo: " + groupName);
            } else {
                response.setSuccess(true);
                response.setMessage("Has rechazado la invitación al grupo: " + groupName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.log("Algo salió mal intentando actualizar las solicitudes con estado pendiente");
            response.setSuccess(false);
            response.setMessage("Error al procesar la solicitud.");
        }

        return response;
    }


    private final String CHECK_GROUP_IN_DB = "SELECT 1 FROM groups WHERE name = ?";
    private final String CREATE_GROUP = "INSERT INTO groups (name, description, creator_email) VALUES (?, ?, ?)";
    private final String SEND_REQUEST_TO_MEMBERS = "INSERT INTO group_invitations (group_name, invited_email) VALUES (?, ?)";
    private final String GET_ALL_PENDING_REQUESTS =
            "SELECT gi.group_name, g.description AS description_group, g.creator_email AS creator_group " +
                    "FROM group_invitations gi " +
                    "JOIN groups g ON gi.group_name = g.name " +
                    "WHERE gi.invited_email = ? AND gi.status = 'pending'";

    private final String UPDATE_TO_ACCEPTED_PENDING_REQUEST =
            "UPDATE group_invitations SET status = 'accepted' WHERE group_name = ? AND invited_email = ? AND status = 'pending'";

    private final String UPDATE_TO_DECLINED_PENDING_REQUEST =
            "UPDATE group_invitations SET status = 'rejected' WHERE group_name = ? AND invited_email = ? AND status = 'pending'";

    private final String INSERT_GROUP_MEMBER =
            "INSERT INTO group_members (group_name, user_email) VALUES (?, ?)";

}
