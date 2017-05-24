package tr.net.susantez;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.net.susantez.service.CodeQuality;
import tr.net.susantez.service.FindUnusedMetadataObjects;
import tr.net.susantez.util.Constants;
import tr.net.susantez.util.Validator;


public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class.getName());


    /*
        [0] -> metadata file
        [1] -> function, 1: unusedObjects - 2: codeQuality
        [2] -> type, 1: ALL, 2:Document, 3:Finder, 4:Script, 5:Process(CodeQuality)
        [3] -> namespace (CodeQuality - Optional)
        [4] -> object (CodeQuality - Optional)
     */
    public static void main(String[] args) {
        try {
            /*
            DEBUG

            if (args == null || args.length < 1) {
                args = new String[5];
                //args[0] = "C:\\cw\\catalogExtend.xml";
                args[0] = "C:\\Users\\serkan.susantez\\Documents\\Installers\\CW Installation\\Metadata\\Metadata-TurkTelekom-IPVPN_SRM1.xml";
                args[1] = "1";
                args[2] = "3";
                args[3] = "tt_Common";
                args[4] = "";
            }
            */
            if (Validator.validateInputs(args)) {
                if (Integer.valueOf(args[1]) == Constants.functionUnusedObjects) {
                    FindUnusedMetadataObjects mdCheck = new FindUnusedMetadataObjects(args[0], Integer.valueOf(args[2]));
                    mdCheck.validateMetadata();
                } else if (Integer.valueOf(args[1]) == Constants.functionCodeQuality) {
                    String metadataPath = args[0];
                    int type = Integer.valueOf(args[2]);
                    String namespace = args.length > 3?args[3]:null;
                    String object  = args.length > 4?args[4]:null;
                    CodeQuality codeQuality = new CodeQuality(metadataPath, type, namespace, object);
                    codeQuality.lintMetadata();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
