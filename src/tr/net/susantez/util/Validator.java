package tr.net.susantez.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Created by serkan.susantez on 4/18/2017.
 */
public class Validator {
    private static final Logger logger = LogManager.getLogger(Validator.class);
    public static boolean validateInputs(String[] args) {
        if (args == null || args.length != 1) {
            logger.warn(Constants.constNullArgs);
            return false;
        }
        if(!checkFile(args[0])) {
            logger.warn(Constants.constCheckMetadataPath);
            return false;
        }
        return true;
    }

    private static boolean checkFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }
}
