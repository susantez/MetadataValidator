package tr.net.susantez.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.net.susantez.dataMapping.Metadata;
import tr.net.susantez.dataMapping.ScriptObject;
import tr.net.susantez.service.MetadataReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by serkan.susantez on 5/3/2017.
 * Utility Functions
 */
public class Utility {
    private static final Logger logger = LogManager.getLogger(Utility.class);

    private Utility () throws IllegalAccessException {
        throw new IllegalAccessException("Utility class");
    }

    public static Metadata parseMetadata(File metadataFile ) {
        XMLStreamReader reader = null;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        MetadataReader mr = null;
        try {
            reader = inputFactory.createXMLStreamReader(new FileInputStream(metadataFile));
            mr = new MetadataReader(reader);
            mr.parseMetadata();
        } catch (XMLStreamException | FileNotFoundException e) {
            logger.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException t) {
                    logger.error(t);
                }
            }
            if (mr == null) {
                mr = new MetadataReader(null);
            }
        }
        return mr.getMetadata();
    }

    public static ScriptObject getObject(String reason) {
        ScriptObject parent = new ScriptObject();
        parent.setName(StringUtils.substringBetween(reason, "'"));
        if (Constants.objDocument.equals(parent.getName())) {
            parent.setType(Constants.scriptObjectTypeDocument);
        } else if (Constants.objFinder.equals(parent.getName()) || Constants.objSearchData.equals(parent.getName())) {
            parent.setType(Constants.scriptObjectTypeFiner);
        } else {
            parent.setType(Constants.scriptObjectTypeNamespace);
        }
        return parent;
    }

    public static ScriptObject getChildObject(String script, ScriptObject parent, int startChar) {
        ScriptObject child = null;
        StringBuilder method = new StringBuilder();
        int startIndex = script.indexOf(parent.getName(),startChar-1) + parent.getName().length() + 1;
        if (script.charAt(startIndex-1) != '.') { //not a leave
            return null;
        }
        for (int i = startIndex; i < script.length(); i++) {
            char c = script.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                method.append(c);
            } else {
                break;
            }
        }
        if (!StringUtils.isAnyBlank(method)) {
            child = new ScriptObject();
            child.setNamespace(parent.getName());
            child.setName(method.toString());
            child.setType(parent.getType() == Constants.scriptObjectTypeNamespace ? Constants.scriptObjectTypeScript:parent.getType());
        }
        return child;
    }
}
