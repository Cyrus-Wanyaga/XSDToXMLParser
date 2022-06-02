package models.handler;

import models.xsd.ComplexType;
import models.xsd.Element;
import models.xsd.SimpleType;

import java.util.ArrayList;

public class XSDObjectsHandler {
    public ArrayList<Element> elementArrayList = new ArrayList<>();
    public ArrayList<ComplexType> complexTypeArrayList = new ArrayList<>();
    public ArrayList<SimpleType> simpleTypeArrayList = new ArrayList<>();

    public XSDObjectsHandler() {
    }

    public ArrayList<Element> getElementArrayList() {
        return elementArrayList;
    }

    public void setElementArrayList(ArrayList<Element> elementArrayList) {
        this.elementArrayList = elementArrayList;
    }

    public ArrayList<ComplexType> getComplexTypeArrayList() {
        return complexTypeArrayList;
    }

    public void setComplexTypeArrayList(ArrayList<ComplexType> complexTypeArrayList) {
        this.complexTypeArrayList = complexTypeArrayList;
    }

    public ArrayList<SimpleType> getSimpleTypeArrayList() {
        return simpleTypeArrayList;
    }

    public void setSimpleTypeArrayList(ArrayList<SimpleType> simpleTypeArrayList) {
        this.simpleTypeArrayList = simpleTypeArrayList;
    }

    public void addElementToArrayList(Element element) {
        elementArrayList.add(element);
    }

    public void addComplexTypeToArrayList(ComplexType complexType) {
        complexTypeArrayList.add(complexType);
    }

    public void addSimpleTypeToArrayList(SimpleType simpleType) {
        simpleTypeArrayList.add(simpleType);
    }

    public Element getElementByType(String typeName) {
        for (Element element : elementArrayList) {
            if (element.getType().equals(typeName)) {
                return element;
            }
        }

        return null;
    }

    public Element getElementByName(String name) {
        for (Element element : elementArrayList) {
            if (element.getName().equals(name)) {
                return element;
            }
        }

        return null;
    }

    public Element getElementByTypeAndName(String type, String name) {
        for (Element element : elementArrayList) {
            if (element.getName().equals(name) && element.getType().equals(type)) {
                return element;
            }
        }

        return null;
    }

    public ComplexType getComplexTypeByName(String complexName) {
        for (ComplexType complexType : complexTypeArrayList) {
            if (complexType.getName().equals(complexName)) {
                return complexType;
            }
        }

        return null;
    }

    public Element getRootElement() {
        for (Element element : elementArrayList) {
            if (element.getParentComplexType() == null) {
                return element;
            }
        }

        return null;
    }

    public boolean elementTypeExistsAsSimpleType(String type) {
        for (SimpleType simpleType : simpleTypeArrayList) {
            if (simpleType.getName().equals(type)) {
                return true;
            }
        }

        return false;
    }
}
