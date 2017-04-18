package tr.net.susantez.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.net.susantez.dataMapping.Metadata;
import tr.net.susantez.dataMapping.metadata.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by serkan.susantez on 4/18/2017.
 */
public class ValidateMetadataObjects {
    private static final Logger logger = LogManager.getLogger(ValidateMetadataObjects.class);

    private XMLStreamReader reader;
    private File metadataFile;
    private Metadata metadata;

    public ValidateMetadataObjects(String metadataPath) {
        metadataFile = new File(metadataPath);
        metadata = null;
        reader = null;
    }

    public void validateMetadata() {
        parseMetadata();
        if (metadata != null) {
            checkDataTypes();
        }
    }

    private void checkDataTypes() {
        List<BasicObject> dataTypes = metadata.getDataDictionary().getDataTypeDictionary();

        for (BasicObject dataType : dataTypes) {
            if (!checkDocuments(dataType) && !checkDataStructures(dataType)) {
                logger.info("unused data type: " + dataType.getGuid());
            }
        }
    }

    private boolean checkDocuments(BasicObject dataType) {
        List<Document> documents =  metadata.getDocumentDictionary();
        for (Document document : documents) {
            List<Leaf> leaves = document.getLeafCollection();
            for (Leaf leave : leaves) {
                if (dataType.getGuid().equals(leave.getDataItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDataStructures(BasicObject dataType) {
        List<DataStructure> dataStructures = metadata.getDataStructureDictionary();
        for (DataStructure dataStructure : dataStructures) {
            List<DataStructureElement> elements = dataStructure.getElements();
            for (DataStructureElement element : elements) {
                if (dataType.getGuid().equals(element.getElement())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void parseMetadata() {
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
                    if (mr != null) {
                        metadata = mr.getMetadata();
                    }
                } catch (XMLStreamException t) {
                    logger.error(t);
                }
            }
        }
    }
}
