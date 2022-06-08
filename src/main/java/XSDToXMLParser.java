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

/**
 * <h1>XSD Schema to Model XML Parser</h1>
 * <p>This application creates an in-memory representation of the XML defined by an XSD schema</p>
 * <p>Strictly works with ISO20022 Schemas</p>
 */
public class XSDToXMLParser {
    //Class that orchestrates the management of the objects in use
    private static XSDObjectsHandler objectsHandler;
    private static XSDObjectsHandler finalObjectsHandler;

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
            finalObjectsHandler = new XSDObjectsHandler();
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
            createObjects(complexNodeList, String.valueOf(types.complexType));

            mapElementsToComplexType(complexNodeList, null);
            elementTypeMapper(elementsNodeList);

            createComplexTypesForChildElements();
            outputXMLStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
            dynamicMappingLogicSample();
            ComplexType complexType = finalObjectsHandler.getComplexTypeByName(objectsHandler.getRootElement().getType());
            outputXMLStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
            outputXMLStringBuilder.append("<").append(complexType.getName()).append(">");
            createOutputXML(complexType);
            outputXMLStringBuilder.append("</").append(complexType.getName()).append(">");

            writeToFile();
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
     * Adds the element children to the enclosing complex type
     * @param nodeList
     * @param complexType
     */
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

    /**
     * Go through each element in the node list, adding the complex type or simple type as necessary
     * @param nodeList
     */
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
                } else {
                    element = objectsHandler.getElementWithoutComplex(type, name);
                    ComplexType complexType = new ComplexType(objectsHandler.getComplexTypeByName(type));
                    element.setComplexType(complexType);
                }
            }
        }
    }

    /**
     * Create the corresponding complex types for the child elements in each complex type
     */
    private static void createComplexTypesForChildElements() {
        for (ComplexType complexType : objectsHandler.getComplexTypeArrayList()) {
            ComplexType complexType1 = new ComplexType();
            complexType1.setName(complexType.getName());
            for (Element element : complexType.getChildrenElements()) {
                Element element1 = new Element(element);
                if (!objectsHandler.elementTypeExistsAsSimpleType(element1.getType())) {
                    ComplexType complexType2 = new ComplexType(objectsHandler.getComplexTypeByName(element1.getType()));
                    element1.setComplexType(complexType2);
                }
                complexType1.addChildElementToArrayList(element1);
            }
            finalObjectsHandler.addComplexTypeToArrayList(complexType1);
        }
    }

    /**
     * Sample code for obtaining the field values from an MT,
     * grabbing the mapping path for the field
     * and setting the field value to the element at the end of the path
     */
    private static void dynamicMappingLogicSample() {
        String[] mtFields = {"20:FX1708062250", "57D:J.P Morgan Chase Co", "58D:Standard Chartered Bank Kenya", "21:123456789", "22:987654321", "53A:Cyrus Wanyaga", "56A:Shem Muchemi", "23:893512519", "32A:STANBICKKEEE", "53B:PARKLANDS 256789"};
        String[] mappings = {"20:FIToFICstmrCdtTrf.GrpHdr.MsgId", "57D:FIToFICstmrCdtTrf.CdtTrfTxInf.InstgAgt.FinInstnId.PstlAdr.AdrLine", "58D:FIToFICstmrCdtTrf.CdtTrfTxInf.InstdAgt.FinInstnId.PstlAdr.AdrLine", "21:FIToFICstmrCdtTrf.CdtTrfTxInf.InstgAgt.FinInstnId.BICFI", "23:FIToFICstmrCdtTrf.CdtTrfTxInf.InstdAgt.FinInstnId.BICFI", "22:FIToFICstmrCdtTrf.CdtTrfTxInf.PmtId.TxId", "56A:FIToFICstmrCdtTrf.CdtTrfTxInf.InstgAgt.FinInstnId.Nm", "53A:FIToFICstmrCdtTrf.CdtTrfTxInf.DbtrAcct.Nm", "32A:FIToFICstmrCdtTrf.GrpHdr.CreDtTm", "53B:FIToFICstmrCdtTrf.CdtTrfTxInf.ChrgsInf.Agt.FinInstnId.PstlAdr.AdrLine"};

        for (String field : mtFields) {
            String[] splitField = field.split(":");
            String fieldNo = splitField[0];
            String value = splitField[1];
            for (String mapping : mappings) {
                String[] splitMapping = mapping.split(":");
                if (splitMapping[0].equals(fieldNo)) {
                    String[] elementTags = splitMapping[1].split("\\.");
                    Element element = objectsHandler.getRootElement();
                    ComplexType rootComplexType = finalObjectsHandler.getComplexTypeByName(element.getType());
                    writeToElement(rootComplexType, elementTags, 0, value);
                    break;
                }
            }
        }
    }

    /**
     * Write the value to the last element specified in the mapping path
     *
     * @param complexType
     * @param tags
     * @param i
     * @param writeValue
     */
    private static void writeToElement(ComplexType complexType, String[] tags, int i, String writeValue) {
        if (complexType.getChildrenElements().size() > 0 && complexType.getChildrenElements() != null) {
            for (Element element : complexType.getChildrenElements()) {
                //The element name is the same as the tag being searched for, but we are not at the end of the mapping path
                if (element.getName().equals(tags[i]) && tags.length - 1 != i) {
                    //Some elements that are supposed to have a complex type, e.g. PstlAdr, do not have their appropriate complex types
                    //This is fixed here ...
                    if (!objectsHandler.elementTypeExistsAsSimpleType(element.getType()) && element.getComplexType() == null) {
                        ComplexType complexType1 = new ComplexType(objectsHandler.getComplexTypeByName(element.getType()));
                        element.setComplexType(complexType1);
                    }
                    if (element.getComplexType() != null) {
                        writeToElement(element.getComplexType(), tags, i + 1, writeValue);
                    }
                } else if (element.getName().equals(tags[i]) && tags.length - 1 == i) {
                    element.setValue(writeValue);
                }
            }
        }
    }

    /**
     * Create the output xml by mapping through the initial complex type e.g.
     *
     * Document has child element FIToFICstmrCdtTrf, of which this element has a complex type with child elements, and so on and so forth
     *
     * @param complexType
     */
    private static void createOutputXML(ComplexType complexType) {
        if (complexType.getChildrenElements().size() > 0 && complexType.getChildrenElements() != null) {
            for (Element element : complexType.getChildrenElements()) {
                outputXMLStringBuilder.append("<").append(element.getName()).append(">");
                if (element.getComplexType() != null) {
                    createOutputXML(element.getComplexType());
                } else {
                    outputXMLStringBuilder.append(element.getValue() != null ? element.getValue() : "");
                }
                outputXMLStringBuilder.append("</").append(element.getName()).append(">").append("\n");
            }
        }
    }

    /**
     * Write the string to a file
     */
    private static void writeToFile() {
        try {
            FileWriter outputFile = new FileWriter("output" + ".xml");
            outputFile.write(outputXMLStringBuilder.toString());
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
