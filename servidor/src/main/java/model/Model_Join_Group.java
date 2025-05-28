package model;

public class Model_Join_Group {
    private String name_group;
    private String description_group;
    private String creator_group;

    public Model_Join_Group() {}

    public Model_Join_Group(String name_group, String description_group, String creator_group) {
        this.name_group = name_group;
        this.description_group = description_group;
        this.creator_group = creator_group;
    }

    public String getName_group() {
        return name_group;
    }

    public void setName_group(String name_group) {
        this.name_group = name_group;
    }

    public String getDescription_group() {
        return description_group;
    }

    public void setDescription_group(String description_group) {
        this.description_group = description_group;
    }

    public String getCreator_group() {
        return creator_group;
    }

    public void setCreator_group(String creator_group) {
        this.creator_group = creator_group;
    }
}
