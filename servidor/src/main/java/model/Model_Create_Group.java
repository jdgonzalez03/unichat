package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Model_Create_Group {
    private int id_group;
    private String name;
    private String description;
    private String creator_email;
    private Date creation_date;
    private List<Model_User_With_Status> members = new ArrayList<>();

    public Model_Create_Group(int id_group, String name, String description, String creator_email, Date creation_date) {
        this.id_group = id_group;
        this.name = name;
        this.description = description;
        this.creator_email = creator_email;
    }

    public Model_Create_Group() {}

    public int getId_group() {
        return id_group;
    }

    public void setId_group(int id_group) {
        this.id_group = id_group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator_email() {
        return creator_email;
    }

    public void setCreator_email(String creator_email) {
        this.creator_email = creator_email;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public List<Model_User_With_Status> getMembers() {
        return members;
    }

    public void setMembers(List<Model_User_With_Status> members) {
        this.members = members;
    }
}
