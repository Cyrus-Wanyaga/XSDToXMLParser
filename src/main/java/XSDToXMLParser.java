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
import java.util.Arrays;

/**
 * <h1>XSD Schema to Model XML Parser</h1>
 * <p>This application creates an in-memory representation of the XML defined by an XSD schema</p>
 * <p>Strictly works with ISO20022 Schemas</p>
 */
public class XSDToXMLParser {
    //Class that orchestrates the management of the objects in use
    private static XSDObjectsHandler objectsHandler;

    //The different types of elements that are in use
    private enum types {element, complexType, simpleType}

    //A global string builder to used to store the contents of the XML output
    private static StringBuilder outputXMLStringBuilder;

    public static void main(String[] args) {
        File directory = new File("schemas");
        //Get all the files in the directory specified and store the files in an array
        File[] files = directory.listFiles();

        assert files != null;
        //Loop through each file to process it
        for (File file : files) {
            createDocument(file);
        }
    }

    /**
     * Create a DOM representation of the XSD schema and then process it
     *
     * @param file @description
     */
    private static void createDocument(File file) {
        try {
            //Initialize the  class
            objectsHandler = new XSDObjectsHandler();
            //Initialize the string builder
            outputXMLStringBuilder = new StringBuilder();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //Parse the file
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
            //Create simple type objects ... these elements will be used to ensure that the simple types do not appear as tags in the XML
            //since simple types are also elements
            createObjects(simpleTypeNodeList, String.valueOf(types.simpleType));

            //destructure the complex types, mapping the inner elements to the parent complex types in the process
            destructComplexTypeChildren(complexNodeList, null);

            dynamicMappingLogicSample();
            Element rootElement = objectsHandler.getRootElement();
            assert rootElement != null;
            outputXMLStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
            createRelationshipsBetweenElements(rootElement);
            writeToFile(schemaNode);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the objects based on the type of object passed
     * The method creates the corresponding Java object, initializes it then adds it to the orchestration class
     *
     * @param nodeList
     * @param type
     */
    private static void createObjects(NodeList nodeList, String type) {
        if (nodeList.getLength() != 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (type.equals(String.valueOf(types.element))) {
                    Element element = new Element();
                    element.setName(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    element.setType(nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue());
                    if (nodeList.item(i).getAttributes().getNamedItem("minOccurs") != null) {
                        element.setMinOccurs(Integer.parseInt(nodeList.item(i).getAttributes().getNamedItem("minOccurs").getNodeValue()));
                    }
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

    /**
     * Destructures the complex type node list <br/>
     * <dt>A complex type can contain several nested elements e.g.</dt> <br/>
     * &lt xs:complexType="someName"&gt <br/>
     * &lt xs:sequence="someName"&gt <br/>
     * &lt xs:element name="element1" type="type1"/&gt <br/>
     * &lt xs:element name="element2" type="type2"/&gt <br/>
     * &lt /xs:sequence&gt <br/>
     * &lt /xs:complexType&gt <br/>
     * Further explanation provided on each condition of the method
     *
     * @param list
     * @param passedComplexType
     */
    private static void destructComplexTypeChildren(NodeList list, ComplexType passedComplexType) {
        //Process the node list only if there is data
        if (list.getLength() != 0) {
            for (int i = 0; i < list.getLength(); i++) {
                //If the node is that of complex type, create the complex type object
                if (list.item(i).getNodeName().equals("xs:complexType")) {
                    ComplexType complexType = new ComplexType();
                    complexType.setName(list.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    objectsHandler.addComplexTypeToArrayList(complexType);
                    //Get the child node list of this element
                    NodeList newList = list.item(i).getChildNodes();
                    if (newList.getLength() != 0) {
                        //Pass the child node list alongside the
                        //created complex type object to the method again to be destructured
                        destructComplexTypeChildren(newList, complexType);
                    }
                } else if
                    //If the node tag name is not equal to "xs:element" ...
                (!list.item(i).getNodeName().equals("xs:element")) {
                    //Get the child node list of this element
                    NodeList newList = list.item(i).getChildNodes();
                    if (newList.getLength() != 0) {
                        //Pass the child node list alongside the
                        //passed complex type to the method again to be destructured
                        destructComplexTypeChildren(newList, passedComplexType);
                    }
                }
                //Element is of type <xs:element>
                else {
                    //Get the element by the type and name
                    Element element = objectsHandler.getElementByTypeAndName(list.item(i).getAttributes().getNamedItem("type").getNodeValue(), list.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    //Proceed only if the passed complex type has been initialized
                    if (passedComplexType != null) {
                        assert element != null;
                        //Set the parent of the element as the passed complex type
                        element.setParentComplexType(passedComplexType);
                        //Set the child of the passed complex type as the element
                        passedComplexType.addChildElementToArrayList(element);
                    }
                }
            }
        }
    }

    /**
     * Creates the relationship between the complex types and the elements <br/>
     * Always expects the root element {@code ObjectsHandler.getRootElement()}, on a call outside the method itself
     *
     * @param element
     */
    public static void createRelationshipsBetweenElements(Element element) {
        ComplexType complexType;
        //Initialize the complex type to the complex type of the same name ... the rule is element type = complexType name
        complexType = objectsHandler.getComplexTypeByName(element.getType());

        assert complexType != null;
        //Get the child elements of the complex type
        ArrayList<Element> elementArrayList = complexType.getChildrenElements();
        //Append the output string with the complex type opening tag
        outputXMLStringBuilder.append("<").append(element.getName()).append(">").append("\n");
        //Loop through the children elements
        for (Element element1 : elementArrayList) {
            //Check if the element is a simple type
            if (objectsHandler.elementTypeExistsAsSimpleType(element1.getType())) {
//                if (element1.getMinOccurs() == null) {
                    //Append the output string with the element tags
//                    System.out.println("Rel " + element1.toString());
                    outputXMLStringBuilder.append("<").append(element1.getName()).append(">").append(element1.getValue() == null ? "" : element1.getValue()).append("</").append(element1.getName()).append(">").append("\n");
//                }
            }
            //Check if the element exists by the type
            else if (objectsHandler.getElementByType(element1.getType()) != null) {
                createRelationshipsBetweenElements(element1);
            }
        }
        outputXMLStringBuilder.append("</").append(element.getName()).append(">");
    }

    private static void writeToFile(Node schemaNode) {
        try {
            FileWriter outputFile = new FileWriter(schemaNode.getAttributes().getNamedItem("targetNamespace").getNodeValue() + ".xml");
            outputFile.write(outputXMLStringBuilder.toString());
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dynamicMappingLogicSample() {
        //Step 1 : Get the different fields of the MT
        //Step 2 : Get the mapping for each field
        //Step 3 : Get the element tag from memory and use it to create the final xml
        String[] mtFields = {"20:FX1708062250", "57D:J.P Morgan Chase Co", "58D:Standard Chartered Bank Kenya", "21:123456789", "56A:Yuri894ABD", "53A:Cyrus Wanyaga"};
        String[] mappings = {"20:PmtId.TxId", "57D:InstdAgt.AdrLine", "58D:InstgAgt.PstlAdr", "21:DbtrAcct.IBAN", "56A:InstdAgt.BICFI", "53A:CdtrAgtAcct.Nm"};

        for (String field : mtFields) {
            System.out.println(field);
            String[] splitField = field.split(":");
            String fieldNo = splitField[0];
            String value = splitField[1];
            for (String mapping : mappings) {
                String[] splitMapping = mapping.split(":");
                if (splitMapping[0].equals(fieldNo)) {
                    String[] splitSplitMapping = splitMapping[1].split("\\.");
                    System.out.println("Mapping for field " + fieldNo + " is " + splitMapping[1]);
                    Element element = objectsHandler.getElementByName(splitSplitMapping[0]);
                    try {
                        ComplexType complexType = objectsHandler.getComplexTypeByName(element.getType());
                        Element element1 = getElement(complexType, splitSplitMapping[1]);
                        assert element1 != null;
                        element1.setValue(value);
                        System.out.println(element1);
                    } catch (Exception e) {
                        ComplexType complexType = objectsHandler.getComplexTypeByName(splitMapping[1]);
                        System.out.println("Complex " + complexType);
                    }
                }
            }
        }
    }

    private static Element getElement(ComplexType complexType, String elementName){
        System.out.println("Looking for " + elementName + " in " + complexType.getName());
        ArrayList<Element> elements = complexType.getChildrenElements();
        for (Element element : elements){
            System.out.println("At element :: " + element.getName());
            if(element.getName().equals(elementName)){
                System.out.println("This much is true");
                return element;
            }
        }

        for (Element element : elements) {
            ComplexType complexType1 = objectsHandler.getComplexTypeByName(element.getType());
            return getElement(complexType1, elementName);
        }

        System.out.println("This element was not found");
        return null;
    }
}
