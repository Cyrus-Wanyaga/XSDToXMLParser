package models.xsd;

import java.util.ArrayList;

/**
 * <h1>&lt xs:complexType&gt</h1>
 * <p>Object representing a complex type</p>
 */
public class ComplexType {
    private String name = "";
    private String tagName = "";
    //Arraylist that stores <xs:element> objects
    private ArrayList<Element> childrenElements = new ArrayList<>(0);

    public ComplexType() {
    }

    public ComplexType(ComplexType complexType) {
        this.name = complexType.getName();
        this.tagName = complexType.getTagName();
        ArrayList<Element> newElementArrayList = new ArrayList<>();
//        System.out.println("Creating complex type " + complexType.getName());
        for (Element element : complexType.getChildrenElements()) {
            Element element1 = new Element(element);
//            System.out.println("At child " + element.getName() + " for " + complexType.getName());
            recursivelyCreateNewElements(element1);
            newElementArrayList.add(element1);
        }
        this.childrenElements = newElementArrayList;
    }

    private static void recursivelyCreateNewElements(Element element) {
        if (element.getComplexType() != null) {
            ComplexType complexType = new ComplexType(element.getComplexType());
//            System.out.println(element.getName() + " has a complex type of " + complexType.getName());
            element.setComplexType(complexType);
            if (complexType.getChildrenElements().size() > 0) {
                for (Element element1 : complexType.getChildrenElements()) {
//                    System.out.println(complexType.getName() + " has element " + element1.getName());
                    recursivelyCreateNewElements(element1);
                }
            }
        }
//        else if (element.getSimpleType() != null){
//            System.out.println(element.getName() + " is a simple type");
//        } else {
//            System.out.println(element.getName() + " is null everywhere");
//        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public ArrayList<Element> getChildrenElements() {
        return childrenElements;
    }

    public void setChildrenElements(ArrayList<Element> childrenElements) {
        this.childrenElements = childrenElements;
    }

    public void addChildElementToArrayList(Element element) {
        childrenElements.add(element);
    }

    public Element getChildElementByName(String name) {
        for (Element element : childrenElements) {
            if (element.getName().equals(name)) {
                return element;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "ComplexType{" +
                "name='" + name + '\'' +
                ", tagName='" + tagName + '\'' +
                ", childrenElements=" + childrenElements +
                '}';
    }
}
