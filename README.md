# ISO 20022 XSD To XML Parser

## About

This application creates an in-memory representation of the XML defined by an ISO 20022 XSD schema.<br/>
To learn more about ISO 20022, visit [About ISO 20022](https://www.iso20022.org/about-iso-20022)

The in-memory objects are used to create a file with the canvas XML. 

## Requirements

The application requires Java 17 to run. If you will be doing development, you will also require Maven.

[Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)<br/>
[Maven](https://maven.apache.org/)

## Running

### Running provided JAR file
1. Create a `schemas` folder in the same location as the JAR file. Place the XSD schemas that you want to use in the `schemas` folder.
2. Open the terminal and navigate to the directory with the JAR application.
3. Run the application using the command `java -jar XSDToXMLParser-1.0.jar`.
#### Note
If you already have your schemas in another directory and do not want to copy them in the location of your jar file, then for step 3 run the command <br/>
`java -jar XSDToXMLParser-1.0.jar ${Absolute Path to Schemas Directory}` e.g. `java -jar XSDToXMLParser-1.0.jar /home/xsd/iso20022/schemas/`

## Note
<p>Schemas can be found from the following URL. 

> https://www.iso20022.org/iso-20022-message-definitions?business-domain=1