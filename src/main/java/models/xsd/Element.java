package models.xsd;

public class Element {
    private String name = "";
    private String type = "";
    private ComplexType parentComplexType;

    public Element() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ComplexType getParentComplexType() {
        return parentComplexType;
    }

    public void setParentComplexType(ComplexType parentComplexType) {
        this.parentComplexType = parentComplexType;
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", parentComplexType=" + parentComplexType +
                '}';
    }
}
