package tr.net.susantez.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Created by serkan.susantez on 4/18/2017.
 */
public class Validator {
    private static final Logger logger = LogManager.getLogger(Validator.class);

    private Validator () throws IllegalAccessException {
        throw new IllegalAccessException("Validator Class");
    }

    /*
        [0] -> metadata file
        [1] -> function, 1: unusedObjects - 2: codeQuality
        [2] -> type, 1: ALL, 2:Document, 3:Finder, 4:Script, 5:Process(CodeQuality)
        [3] -> namespace (CodeQuality - Optional)
        [4] -> object (CodeQuality - Optional)
    */
    public static boolean validateInputs(String[] args) {
        if (args == null || args.length < 3) {
            logger.error(Constants.constNullArgs);
            return false;
        }
        if(!checkFile(args[0])) {
            logger.error(Constants.constCheckMetadataPath);
            return false;
        }
        if(!StringUtils.isNumeric(args[1]) || Integer.valueOf(args[1]) < 0 || Integer.valueOf(args[1]) > 2) {
            logger.error(Constants.constInvalidFunction);
            return false;
        }
        if(!StringUtils.isNumeric(args[2]) || Integer.valueOf(args[2]) < 0 || Integer.valueOf(args[2]) > 5) {
            logger.error(Constants.constInvalidType);
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
