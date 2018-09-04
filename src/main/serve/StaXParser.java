import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StaXParser {
    static final String SERVLET = "servlet";
    static final String SERVLET_NAME = "servlet-name";
    static final String SERVLET_CLASS = "servlet-class";
    static final String SERVLET_MAPPING = "servlet-mapping";
    static final String URL_PATTERN = "url-pattern";
    /*static final String INTERACTIVE = "interactive";*/

    @SuppressWarnings({ "unchecked", "null" })
    public List<ContextParameters> readConfig(String configFile) {
        List<ContextParameters> items = new ArrayList<ContextParameters>();
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = new FileInputStream(configFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            // read the XML document
            ContextParameters item = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    // If we have an item element, we create a new item
                    if (startElement.getName().getLocalPart().equals(SERVLET)) {
                        item = new ContextParameters();

                    }



                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(SERVLET_NAME)) {
                            event = eventReader.nextEvent();
                            item.setServlet_name(event.asCharacters().getData());
                            continue;
                        }
                    }


                    if (event.asStartElement().getName().getLocalPart()
                            .equals(SERVLET_CLASS)) {
                        event = eventReader.nextEvent();
                        item.setServlet_class(event.asCharacters().getData());
                        continue;
                    }

                    if (event.asStartElement().getName().getLocalPart()
                            .equals(URL_PATTERN)) {
                        event = eventReader.nextEvent();
                        item.setUrl_pattern(event.asCharacters().getData());
                        continue;
                    }


                }
                // If we reach the end of an item element, we add it to the list
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(SERVLET)) {
                        items.add(item);
                    }
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (XMLStreamException e1) {
            e1.printStackTrace();
        } return items;
    }

    }