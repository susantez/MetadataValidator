package tr.net.susantez.util;

/**
 * Created by serkan.susantez on 4/18/2017.
 */
public class Constants {

    private Constants () throws IllegalAccessException {
        throw new IllegalAccessException("Constants class");
    }

    public static final String constNullArgs = "Parameters missing! \n[0] -> metadata file,\n" +
                                                "[1] -> function, 1: unusedObjects - 2: codeQuality\n" +
                                                "[2] -> type, 1: ALL, 2:Document, 3:Finder, 4:Script, 5:Process(CodeQuality)\n" +
                                                "[3] -> namespace (CodeQuality - Optional)\n" +
                                                "[4] -> object (CodeQuality - Optional)";
    public static final String constCheckMetadataPath = "missing metadata, check path";
    public static final String constInvalidFunction = "invalid function, 1: unusedObjects - 2: codeQuality";
    public static final String constInvalidType = "invalid type, 1: ALL, 2:Document, 3:Finder, 4:Script, 5:Process(CodeQuality)";

    public static final int functionUnusedObjects = 1;
    public static final int functionCodeQuality = 2;

    public static final int typeAll = 1;
    public static final int typeDocument = 2;
    public static final int typeFinder = 3;
    public static final int typeScript = 4;
    public static final int typeProcess = 5;

    public static final String objDataType = "dataType";
    public static final String objElementType = "elementType";
    public static final String objDocument = "document";
    public static final String objFinder = "finder";
    public static final String objSearchData = "searchData";
    public static final String objDataStructure = "dataStructure";
    public static final String objMenu = "menu";
    public static final String objScript = "script";
    public static final String objInput = "input";
    public static final String objFunction = "function";

    public static final String errMissngDeffinition = "'{a}' was used before it was defined.";
    public static final String errUnexpectedElseAfterReturn = "Unexpected 'else' after 'return'.";
    public static final String errVarOnTop = "Move 'var' declarations to the top of the function.";
    public static final String errValueAssignment = "Expected an assignment or function call and instead saw an expression.";
    public static final String errDontDeclareVarInLoops = "Don't declare variables in a loop.";
    public static final String errArrayNotation = "Use the array literal notation [].";

    public static final int scriptObjectTypeDocument = 1;
    public static final int scriptObjectTypeFiner = 2;
    public static final int scriptObjectTypeNamespace = 3;
    public static final int scriptObjectTypeScript = 4;

    public static final String finderSQLGet = "cwOnFinderSQLGet";
    public static final String finderSQLSel = "cwOnFinderSQLSel";
}
