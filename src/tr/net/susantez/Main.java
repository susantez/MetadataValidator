package tr.net.susantez;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.net.susantez.service.ValidateMetadataObjects;
import tr.net.susantez.util.Validator;


public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.error("Hello, World!");
        args = new String[1];
        args[0] = "C:\\metadata.xml";
        if (Validator.validateInputs(args)) {
            ValidateMetadataObjects mdCheck = new ValidateMetadataObjects(args[0]);
            mdCheck.validateMetadata();
        }
    }
}