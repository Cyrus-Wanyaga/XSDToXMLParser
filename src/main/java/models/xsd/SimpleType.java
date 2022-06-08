package models.xsd;

public class SimpleType {
    private String name = "";
    private Element element;
    private boolean parsed = false;

    public SimpleType() {
    }

    public SimpleType(SimpleType simpleType) {
        this.name = simpleType.getName();
        this.element = simpleType.getElement();
        this.parsed = simpleType.isParsed();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public boolean isParsed() {
        return parsed;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    @Override
    public String toString() {
        return "SimpleType{" +
                "name='" + name + '\'' +
                '}';
    }
}
