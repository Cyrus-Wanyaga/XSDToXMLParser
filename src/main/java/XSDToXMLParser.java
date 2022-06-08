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
    private static XSDObjectsHandler mappingObjectsHandler;
    private static XSDObjectsHandler finalObjectsHandler;

    //The different types of elements that are in use
    private enum types {element, complexType, simpleType}

    //A global string builder to used to store the contents of the XML output
    private static StringBuilder outputXMLStringBuilder;
    private static StringBuilder outputXMLStringBuilder2;

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
            mappingObjectsHandler = new XSDObjectsHandler();
            finalObjectsHandler = new XSDObjectsHandler();
            //Initialize the string builder
            outputXMLStringBuilder = new StringBuilder();
            outputXMLStringBuilder2 = new StringBuilder();

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

            createObjects(elementsNodeList, String.valueOf(types.element));
            createObjects(simpleTypeNodeList, String.valueOf(types.simpleType));
            createObjects(complexNodeList, String.valueOf(types.complexType));
            //Create element objects ... these elements will be mapped to the complex types to create the relationship
            //Create simple type objects ... these elements will be used to ensure that the simple types do not appear as tags in the XML
            //since simple types are also elements

            //destructure the complex types, mapping the inner elements to the parent complex types in the process
//            destructComplexTypeChildren(complexNodeList, null);
            mapElementsToComplexType(complexNodeList, null);
//            setComplexTypeToElements();

            elementTypeMapper(elementsNodeList);
            restructureObjs();
            Element rootElement = objectsHandler.getRootElement();
            assert rootElement != null;
            outputXMLStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
//            createRelationshipsBetweenElements(rootElement);
//            createOutputXML(rootElement);
//            writeToFile(schemaNode);
            dynamicMappingLogicSample();
            dynamicObjects(mappingObjectsHandler.getRootElement());
//            cashAccountMan();

            try {
                FileWriter outputFile = new FileWriter("mapper.xml");
                outputFile.write(outputXMLStringBuilder2.toString());
                outputFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    private static void restructureObjs() {
        for (ComplexType complexType : objectsHandler.getComplexTypeArrayList()) {
//            System.out.println("Complex Type " + complexType.getName());
            ComplexType complexType1 = new ComplexType();
            complexType1.setName(complexType.getName());
            for (Element element : complexType.getChildrenElements()) {
                Element element1 = new Element(element);
//                System.out.println("Created new " + element.getName() + ". Old=" + element.hashCode() + ", New=" + element1.hashCode());
                if (!objectsHandler.elementTypeExistsAsSimpleType(element1.getType())) {
                    ComplexType complexType2 = new ComplexType(objectsHandler.getComplexTypeByName(element1.getType()));
                    element1.setComplexType(complexType2);
//                    System.out.println("Set " + complexType2.getName() + " to " + element1.getName() + " for " + complexType.getName());
                }
                complexType1.addChildElementToArrayList(element1);
            }
            finalObjectsHandler.addComplexTypeToArrayList(complexType1);
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

    private static void mapElementsToComplexType(NodeList nodeList, ComplexType complexType) {
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName().equals("xs:element")) {
                    Element element;
                    if (objectsHandler.elementTypeExistsAsSimpleType(nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue())) {
                        element = objectsHandler.getSimpleTypeNotSetElement(nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue(), nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    } else {
                        element = objectsHandler.getParentComplexTypeNotSetElement(nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue(), nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                        element.setParentComplexType(complexType);
                    }
                    complexType.addChildElementToArrayList(element);
                } else if (nodeList.item(i).getNodeName().equals("xs:complexType")) {
                    ComplexType complexType1 = objectsHandler.getComplexTypeByName(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    NodeList newList = nodeList.item(i).getChildNodes();
                    mapElementsToComplexType(newList, complexType1);
                } else {
                    NodeList newList = nodeList.item(i).getChildNodes();
                    mapElementsToComplexType(newList, complexType);
                }
            }
        }
    }

    private static void setComplexTypeToElements() {
        for (Element element : objectsHandler.elementArrayList) {
            if (objectsHandler.elementTypeExistsAsSimpleType(element.getType())) {
                SimpleType simpleType = new SimpleType(objectsHandler.getSimpleTypeByName(element.getType()));
                element.setSimpleType(simpleType);
//                mappingObjectsHandler.addSimpleTypeToArrayList(simpleType);
            } else {
                ComplexType complexType = new ComplexType(objectsHandler.getComplexTypeByName(element.getType()));
                element.setComplexType(complexType);
//                mappingObjectsHandler.addComplexTypeToArrayList(complexType);
            }
//            mappingObjectsHandler.addElementToArrayList(element);
        }
    }

    private static void elementTypeMapper(NodeList nodeList) {
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                String type = nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue();
                String name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                Element element;
                if (objectsHandler.elementTypeExistsAsSimpleType(type)) {
                    element = objectsHandler.getSimpleTypeNotSetElement(type, name);
                    SimpleType simpleType = new SimpleType(objectsHandler.getSimpleTypeByName(type));
                    element.setSimpleType(simpleType);
                    mappingObjectsHandler.addSimpleTypeToArrayList(simpleType);
                } else {
                    element = objectsHandler.getElementWithoutComplex(type, name);
                    ComplexType complexType = new ComplexType(objectsHandler.getComplexTypeByName(type));
                    element.setComplexType(complexType);
                    mappingObjectsHandler.addComplexTypeToArrayList(complexType);
                }
                mappingObjectsHandler.addElementToArrayList(element);
            }
        }
    }

    public static void createOutputXML(Element element) {
        if (element != null) {
            ComplexType complexType = element.getComplexType();
            assert complexType != null;
            outputXMLStringBuilder.append("<").append(element.getName()).append(">").append("\n");
            System.out.println(complexType);
            ArrayList<Element> elementArrayList = complexType.getChildrenElements();

            for (Element element1 : elementArrayList) {
                if (element1.getSimpleType() != null) {
                    outputXMLStringBuilder.append("<").append(element1.getName()).append(">").append(element1.getValue() == null ? "" : element1.getValue()).append("</").append(element1.getName()).append(">").append("\n");
                } else if (element1.getParentComplexType() != null) {
                    createOutputXML(element1);
                }
            }
            outputXMLStringBuilder.append("</").append(element.getName()).append(">");
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
                //Append the output string with the element tags
                outputXMLStringBuilder.append("<").append(element1.getName()).append(">").append(element1.getValue() == null ? "" : element1.getValue()).append("</").append(element1.getName()).append(">").append("\n");
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
        String[] mtFields = {"20:FX1708062250", "57D:J.P Morgan Chase Co", "58D:Standard Chartered Bank Kenya", "21:123456789", "22:987654321", "53A:Cyrus Wanyaga", "56A:Shem Muchemi"};
        String[] mappings = {"20:FIToFICstmrCdtTrf.GrpHdr.MsgId", "57D:FIToFICstmrCdtTrf.CdtTrfTxInf.InstgAgt.FinInstnId.PstlAdr.AdrLine", "58D:FIToFICstmrCdtTrf.CdtTrfTxInf.InstdAgt.FinInstnId.PstlAdr.AdrLine", "21:FIToFICstmrCdtTrf.CdtTrfTxInf.InstgAgt.FinInstnId.BICFI", "22:FIToFICstmrCdtTrf.CdtTrfTxInf.PmtId.TxId", "56A:FIToFICstmrCdtTrf.CdtTrfTxInf.InstgAgt.FinInstnId.Nm", "53A:FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Nm"};

        for (String field : mtFields) {
            System.out.println(field);
            String[] splitField = field.split(":");
            String fieldNo = splitField[0];
            String value = splitField[1];
            for (String mapping : mappings) {
                String[] splitMapping = mapping.split(":");
                if (splitMapping[0].equals(fieldNo)) {
                    String[] elementTags = splitMapping[1].split("\\.");
                    Element element = objectsHandler.getRootElement();
                    ComplexType rootComplexType = finalObjectsHandler.getComplexTypeByName(element.getType());
                    System.out.println(Arrays.toString(elementTags));
                    writeToElement2(rootComplexType, elementTags, 0, value);
                    break;
                }
            }
        }
    }

    private static void writeToElement2(ComplexType complexType, String[] tags, int i, String writeValue) {
        //Get complex type from element
        //Get children of complex type
        //Loop over children checking if element matched current tag
        //If matches get the element of that complex type
        //Get complex type of element
        //To avoid return of same element remove it ...
        if (complexType.getChildrenElements().size() > 0 && complexType.getChildrenElements() != null) {
            for (Element element : complexType.getChildrenElements()) {
                if (element.getName().equals(tags[i]) && tags.length - 1 != i) {
                    if (element.getComplexType() != null) {
                        writeToElement2(element.getComplexType(), tags, i + 1, writeValue);
                    }
                } else if (element.getName().equals(tags[i]) && tags.length - 1 == i) {
                    element.setValue(writeValue);
                }
            }
        }
    }

    private static void writeToElement(Element element, String[] tags, int i, String writeValue) {
        //Get complex type from element
        //Get children of complex type
        //Loop over children checking if element matched current tag
        //If matches get the element of that complex type
        //Get complex type of element
        //To avoid return of same element remove it ...
        System.out.println("Setting " + tags[i] + " to " + element.getName() + ", " + i);
        ComplexType complexType = element.getComplexType();
//      System.out.println("Has complex type " + complexType.getName());
        for (Element element1 : complexType.getChildrenElements()) {
            if (element1.getName().equals(tags[i]) && tags.length - 1 != i) {
                System.out.println(element1.getName() + ", " + element.getType());
                Element element2 = mappingObjectsHandler.getElementByNameAndParentType(element1.getName(), element.getType());
                System.out.println("Found " + element2.getName() + ", " + element2.getType() + ", " + element2.hashCode());
                writeToElement(element2, tags, i + 1, writeValue);
            } else if (element1.getName().equals(tags[i]) && tags.length - 1 == i) {
                Element element2 = mappingObjectsHandler.getUnparsedElementByNameAndParent(element1.getName(), element.getType());
                System.out.println("Has simple type " + element2.getName() + ", " + element2.getSimpleType().getName() + ", " + element2.hashCode());
                element2.setValue(writeValue);
                element2.setParsed(true);
                System.out.println(element2);
            }
        }

//        if (element.getName().equals(mappingObjectsHandler.getRootElement().getName())) {
//            Element element1 = mappingObjectsHandler.getElementByNameAndParentType(element.getComplexType().getChildrenElements().get(0).getName(), element.getName());
//            writeToElement(element1, tags, i + 1, writeValue);
//        } else {
//            for (Element element1 : element.getComplexType().getChildrenElements()) {
//                if (element1.getName().equals(tags[i]) && tags.length - 1 != i) {
//                    Element element2 = mappingObjectsHandler.getElementByNameAndParentType(element1.getName(), mappingObjectsHandler.getElementByName(element1.getName()).getParentComplexType().getName());
//                    writeToElement(element2, tags, i + 1, writeValue);
//                } else if (element1.getName().equals(tags[i]) && tags.length - 1 == i) {
//                    Element element2 = mappingObjectsHandler.getElementByNameAndParentType(element1.getName(), element.getType());
//                    element2.setValue(writeValue);
//                    System.out.println(element2.getName() + " == " + element2.getValue() + " == " + element2.hashCode());
//                }
//            }
//        }
    }

    private static void cashAccountMan() {
        System.out.println("======================CASH ACCOUNT MANAGER===========================");
        ComplexType complexType = objectsHandler.getComplexTypeByName("PostalAddress24");
        System.out.println(complexType.hashCode());
        for (Element element : complexType.getChildrenElements()) {
            System.out.println(element.getName() + " == " + element.hashCode() + " == " + element.getValue());
        }
        System.out.println("=======================MAPPING OBJECTS============================");
        for (Element element : mappingObjectsHandler.elementArrayList) {
            if (element.getComplexType() != null && element.getComplexType().getName().equals("PostalAddress24")) {
                System.out.println("name=" + element.getName() + " type=" + element.getType() + " for " + element.getParentComplexType().getName());
                System.out.println(element.getComplexType().hashCode());
                for (Element element1 : element.getComplexType().getChildrenElements()) {
                    System.out.println(element1.getName() + " == " + element1.hashCode() + " == " + element1.getValue());
                }
                System.out.println("---------------------------------------------------------");
            }
        }
        System.out.println("======================OBJECTS HANDLER===========================");
        for (Element element : objectsHandler.elementArrayList) {
            if (element.getComplexType() != null && element.getComplexType().getName().equals("PostalAddress24")) {
                System.out.println("name=" + element.getName() + " type=" + element.getType() + " for " + element.getParentComplexType().getName());
                System.out.println(element.getComplexType().hashCode());
                for (Element element1 : element.getComplexType().getChildrenElements()) {
                    System.out.println(element1.getName() + " == " + element1.hashCode() + " == " + element1.getValue());
                }
                System.out.println("---------------------------------------------------------");
            }
        }
    }

    private static void dynamicObjects(Element element) {
        outputXMLStringBuilder2.append("<").append(element.getName()).append(">").append("\n");

        ComplexType complexType;
        if (element.getComplexType() != null) {
            complexType = element.getComplexType();
            for (Element element1 : complexType.getChildrenElements()) {
                Element element2 = mappingObjectsHandler.getElementByTypeAndName(element1.getType(), element1.getName());
                if (element2.getComplexType() == null && element2.getSimpleType() != null) {
                    outputXMLStringBuilder2.append("<").append(element2.getName()).append(">").append(element2.getValue() == null ? "" : element2.getValue()).append("</").append(element2.getName()).append(">").append("\n");
                } else {
                    dynamicObjects(element2);
                }
            }
        }

        outputXMLStringBuilder2.append("</").append(element.getName()).append(">").append("\n");
    }
}
