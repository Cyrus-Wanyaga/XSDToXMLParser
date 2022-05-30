package models.xsd;

public class SimpleType {
    private String name = "";


    public SimpleType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SimpleType{" +
                "name='" + name + '\'' +
                '}';
    }
}
