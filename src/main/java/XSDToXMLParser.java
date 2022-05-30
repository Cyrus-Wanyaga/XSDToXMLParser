import models.handler.XSDObjectsHandler;
import models.xsd.ComplexType;
import models.xsd.Element;
import models.xsd.SimpleType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <h1>XSD Schema to Model XML Parser</h1>
 * <p>This application creates an in-memory representation of the XML defined by an XSD schema</p>
 * <p>Strictly works with ISO20022 Schemas</p>
 */
public class XSDToXMLParser {
    private static XSDObjectsHandler objectsHandler;
    private enum types {element, complexType, simpleType}
    private static StringBuilder outputXMLStringBuilder;

    public static void main(String[] args) {
        File directory = new File("schemas");
        File[] files = directory.listFiles();

        assert files != null;
        for (File file : files) {
            createDocument(file);
        }
    }

    private static void createDocument(File file) {
        try {
            objectsHandler = new XSDObjectsHandler();
            outputXMLStringBuilder = new StringBuilder();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            //Get schema node
            Node schemaNode = document.getElementsByTagName("xs:schema").item(0);
            //Get all the nodes with the tag "xs:complexType"
            NodeList complexNodeList = document.getElementsByTagName("xs:complexType");
            //Get all the nodes with the tag "xs:element"
            NodeList elementsNodeList = document.getElementsByTagName("xs:element");
            //Get all the nodes with the tag "xs:simpleType"
            NodeList simpleTypeNodeList = document.getElementsByTagName("xs:simpleType");

            //Create element objects ... these elements will be mapped to the complex types to create the relationship
            createObjects(elementsNodeList, String.valueOf(types.element));
            createObjects(simpleTypeNodeList, String.valueOf(types.simpleType));

            //destructure the complex types, mapping the inner elements to the parent complex types in the process
            destructComplexTypeChildren(complexNodeList, null);

            Element rootElement = objectsHandler.getRootElement();
            assert rootElement != null;
            outputXMLStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
            createRelationshipsBetweenElements(rootElement);
            writeToFile(schemaNode);
        } catch (ParserConfigurationException | IOException | SAXException e){
            e.printStackTrace();
        }
    }

    private static void createObjects(NodeList nodeList, String type){
        if (nodeList.getLength() != 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (type.equals(String.valueOf(types.element))) {
                    Element element = new Element();
                    element.setName(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    element.setType(nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue());
                    objectsHandler.addElementToArrayList(element);
                } else if (type.equals(String.valueOf(types.complexType))) {
                    ComplexType complexType = new ComplexType();
                    complexType.setName(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    objectsHandler.addComplexTypeToArrayList(complexType);
                } else if (type.equals(String.valueOf(types.simpleType))) {
                    SimpleType simpleType = new SimpleType();
                    simpleType.setName(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    objectsHandler.addSimpleTypeToArrayList(simpleType);
                }
            }
        }
    }

    private static void destructComplexTypeChildren(NodeList list, ComplexType passedComplexType) {
        if (list.getLength() != 0) {
            for (int i = 0; i < list.getLength(); i++) {
                //If the node is that of complex type, create the complex type object
                if (list.item(i).getNodeName().equals("xs:complexType")) {
                    ComplexType complexType = new ComplexType();
                    complexType.setName(list.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    objectsHandler.addComplexTypeToArrayList(complexType);
                    NodeList newList = list.item(i).getChildNodes();
                    if (newList.getLength() != 0) {
                        destructComplexTypeChildren(newList, complexType);
                    }
                } else if
                    //If the node tag name is not equal to "xs:element" do the following ...
                (!list.item(i).getNodeName().equals("xs:element")) {
                    NodeList newList = list.item(i).getChildNodes();
                    if (newList.getLength() != 0) {
                        destructComplexTypeChildren(newList, passedComplexType);
                    }
                } else {
                    Element element = objectsHandler.getElementByTypeAndName(list.item(i).getAttributes().getNamedItem("type").getNodeValue(), list.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    if (passedComplexType != null) {
                        assert element != null;
                        element.setParentComplexType(passedComplexType);
                        passedComplexType.addChildElementToArrayList(element);
                    }
                }
            }
        }
    }

    public static void createRelationshipsBetweenElements(Element element) {
        ComplexType complexType;
        complexType = objectsHandler.getComplexTypeByName(element.getType());
        assert complexType != null;
        ArrayList<Element> elementArrayList = complexType.getChildrenElements();
        outputXMLStringBuilder.append("<").append(element.getName()).append(">").append("\n");
        for (Element element1 : elementArrayList) {
            if (objectsHandler.elementTypeExistsAsSimpleType(element1.getType())) {
                outputXMLStringBuilder.append("<").append(element1.getName()).append("></").append(element1.getName()).append(">").append("\n");
            } else if (objectsHandler.getElementByType(element1.getType()) != null) {
                createRelationshipsBetweenElements(element1);
            }
        }
        outputXMLStringBuilder.append("</").append(element.getName()).append(">");
    }

    private static void writeToFile(Node schemaNode){
        try {
            FileWriter outputFile = new FileWriter(schemaNode.getAttributes().getNamedItem("targetNamespace").getNodeValue() + ".xml");
            outputFile.write(outputXMLStringBuilder.toString());
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
