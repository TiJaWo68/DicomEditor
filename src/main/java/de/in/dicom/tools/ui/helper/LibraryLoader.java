package de.in.dicom.tools.ui.helper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Loads library information from licenses.xml.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class LibraryLoader {

    private static final Logger LOGGER = LogManager.getLogger(LibraryLoader.class);

    public record LibraryInfo(String name, String version, String license, String projectUrl, String licenseUrl) {
    }

    public List<LibraryInfo> loadLibraries() {
        List<LibraryInfo> libs = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/licenses.xml")) {
            if (is == null) {
                return libs; // No licenses.xml found
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("dependency");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String name = getTagValue("name", eElement);
                    if (name.isEmpty())
                        name = getTagValue("artifactId", eElement); // Fallback

                    String version = getTagValue("version", eElement);
                    String projectUrl = getTagValue("url", eElement);

                    // License & License URL
                    String licenseName = "";
                    String licenseUrl = "";
                    NodeList licensesNodeList = eElement.getElementsByTagName("licenses");
                    if (licensesNodeList.getLength() > 0) {
                        Element licensesElement = (Element) licensesNodeList.item(0);
                        NodeList lList = licensesElement.getElementsByTagName("license");
                        if (lList.getLength() > 0) {
                            Element licenseElement = (Element) lList.item(0);
                            licenseName = getTagValue("name", licenseElement);
                            licenseUrl = getTagValue("url", licenseElement);
                        }
                    }

                    libs.add(new LibraryInfo(name, version, licenseName, projectUrl, licenseUrl));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load licenses.xml", e);
        }
        return libs;
    }

    private String getTagValue(String tag, Element element) {
        NodeList nlList = element.getChildNodes();
        for (int i = 0; i < nlList.getLength(); i++) {
            Node n = nlList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(tag)) {
                return n.getTextContent().trim();
            }
        }
        return "";
    }
}
