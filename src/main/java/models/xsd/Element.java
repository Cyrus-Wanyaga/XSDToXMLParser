/**
 * @author Cyrus Wanyaga
 */

package models.xsd;

/**
 * <h1>&lt xs:element&gt</h1>
 * <p>Object representing an element</p>
 */
public class Element {
    private String name = "";
    private String type = "";
    private String value = null;
    private Integer minOccurs;
    private Integer maxOccurs;
    private ComplexType parentComplexType;
    private ComplexType complexType = null;
    private SimpleType simpleType = null;

    public Element() {
    }

    public Element(Element element){
        this.name = element.getName();
        this.type = element.getType();
        this.value = element.getValue();
        this.minOccurs = element.getMinOccurs();
        this.maxOccurs = element.getMaxOccurs();
        this.complexType = element.getComplexType();
        this.simpleType = element.getSimpleType();
        this.parentComplexType = element.getParentComplexType();
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public ComplexType getParentComplexType() {
        return parentComplexType;
    }

    public void setParentComplexType(ComplexType parentComplexType) {
        this.parentComplexType = parentComplexType;
    }

    public ComplexType getComplexType() {
        return complexType;
    }

    public void setComplexType(ComplexType complexType) {
        this.complexType = complexType;
    }

    public SimpleType getSimpleType() {
        return simpleType;
    }

    public void setSimpleType(SimpleType simpleType) {
        this.simpleType = simpleType;
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", minOccurs=" + minOccurs +
                ", maxOccurs=" + maxOccurs +
                ", complexType=" + complexType +
                ", simpleType=" + simpleType +
                ", parentComplexType=" + complexType +
                '}';
    }
}
