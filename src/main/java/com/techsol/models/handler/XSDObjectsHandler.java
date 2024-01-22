/**
 * @author Cyrus Wanyaga
 */

package com.techsol.models.handler;

import com.techsol.models.xsd.ComplexType;
import com.techsol.models.xsd.Element;
import com.techsol.models.xsd.SimpleType;

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

    public ArrayList<Element> getElementsByName(String name) {
        ArrayList<Element> elementArrayList1 = new ArrayList<>();
        for (Element element : elementArrayList) {
            if (element.getName().equals(name)) {
                elementArrayList1.add(element);
            }
        }

        return elementArrayList1;
    }

    public boolean removeElementFromList(Element element) {
        for (Element element1 : elementArrayList) {
            if (element1.equals(element)) {
                elementArrayList.remove(element1);
                return true;
            }
        }

        return false;
    }

    public ArrayList<ComplexType> getComplexTypesByName (String name) {
        ArrayList<ComplexType> complexTypes = new ArrayList<>();
        for (ComplexType complexType : complexTypeArrayList){
            if (complexType.getName().equals(name)){
                complexTypes.add(complexType);
            }
        }

        return complexTypes;
    }

    public Element getParentComplexTypeNotSetElement(String type, String name) {
        for (Element element : elementArrayList) {
            if (element.getName().equals(name) && element.getType().equals(type) && element.getParentComplexType() == null) {
                return element;
            }
        }

        return null;
    }

    public Element getSimpleTypeNotSetElement(String type, String name) {
        for (Element element : elementArrayList) {
            if (element.getName().equals(name) && element.getType().equals(type) && element.getSimpleType() == null) {
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

    public SimpleType getSimpleTypeByName(String name) {
        for (SimpleType simpleType : simpleTypeArrayList) {
            if (simpleType.getName().equals(name)) {
                return simpleType;
            }
        }

        return null;
    }

    public Element getElementWithoutComplex(String type, String name) {
        for (Element element : elementArrayList) {
            if (element.getName().equals(name) && element.getType().equals(type) && element.getComplexType() == null) {
                return element;
            }
        }

        return null;
    }
}
