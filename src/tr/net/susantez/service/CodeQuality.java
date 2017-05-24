package tr.net.susantez.service;

import com.googlecode.jslint4java.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.net.susantez.dataMapping.CWIssue;
import tr.net.susantez.dataMapping.Metadata;
import tr.net.susantez.dataMapping.ScriptObject;
import tr.net.susantez.dataMapping.metadata.*;
import tr.net.susantez.dataMapping.metadata.Process;
import tr.net.susantez.util.Constants;
import tr.net.susantez.util.Utility;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static tr.net.susantez.util.Constants.scriptObjectTypeDocument;

/**
 * Created by serkan.susantez on 5/2/2017.
 * Lint all scripts with JSLint4Java library, which integrates jsLint lib with java
 * Custom Validations for:
 *   namespace: check if namespace exists
 *   methods: check if namespace has this method
 *   dataType: check if document has that leave
 *
 */
public class CodeQuality {
    private static final Logger logger = LogManager.getLogger(CodeQuality.class);

    private Metadata metadata;
    private JSLint lint;
    private HashMap<String, String> namespaces;
    private HashMap<String, String> mdObjects;
    private String appNamespace = null;
    private String appObject = null;
    private int appType;

    public CodeQuality(String metadataPath, int type, String namespace, String object) {
        metadata = Utility.parseMetadata(new File(metadataPath));
        lint = new JSLintBuilder().fromDefault();
        appType = type;
        setLintOptions();
        setNamespaces();
        loadPackageDetails();
        if (!StringUtils.isAnyBlank(namespace)) {
            appNamespace = getNamespaceGUID(namespace);
            if (!StringUtils.isAnyBlank(object)) {
                appObject = object;
            }
        }
    }

    private void loadPackageDetails() {
        mdObjects = new HashMap<>();
        Path path = Paths.get("packageDetails");
        try {
            List<String> obj = Files.readAllLines(path);
            for (String method : obj) {
                mdObjects.put(method,"predefined");
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void setNamespaces() {
        namespaces = new HashMap<>();
        for (NameSpace nameSpace : metadata.getNameSpaceDictionary()) {
            namespaces.put(nameSpace.getGuid(), nameSpace.getName());
        }
    }

    private void setLintOptions() {
        lint.addOption(Option.ASS);
        lint.addOption(Option.BITWISE);
        lint.addOption(Option.CONTINUE);
        lint.addOption(Option.DEVEL);
        lint.addOption(Option.EQEQ);
        lint.addOption(Option.INDENT);
        lint.addOption(Option.SLOPPY);
        lint.addOption(Option.VARS);
        lint.addOption(Option.WHITE);
        lint.addOption(Option.PLUSPLUS);
        lint.addOption(Option.NOMEN);
        lint.addOption(Option.EVIL);
    }

    public void lintMetadata() {
        logger.info("Object|Item|Line|Reason|Script");
        //Lint Documents
        if (appType == Constants.typeAll || appType == Constants.typeDocument) {
            for (Document document : metadata.getDocumentDictionary()) {
                if (checkItem(document)) {
                    lintActions(document.getActionCollection(), document);
                }
            }
        }

        if (appType == Constants.typeAll || appType == Constants.typeFinder) {
            for (Finder finder : metadata.getFinderDictionary()) {
                if (checkItem(finder)) {
                    lintActions(finder.getActions(), finder);
                }
            }
        }

        if (appType == Constants.typeAll || appType == Constants.typeProcess) {
            for (Process process : metadata.getProcessDictionary()) {
                if (checkItem(process)) {
                    lintActions(process.getActions(), process);
                }
            }
        }

        if (appType == Constants.typeAll || appType == Constants.typeScript) {
            lintScript(metadata.getScriptsDictionary(), null);
        }
    }

    private boolean checkItem(BasicObject obj) {
        boolean result = false;
        if (appNamespace != null) {
            if (appObject != null && appObject.equals(obj.getName()) && appNamespace.equals(obj.getNameSpace())) {
                result = true;
            } else if (appNamespace.equals(obj.getNameSpace())) {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    private void lintActions(List<Action> actions, BasicObject mdObj) {
        for (Action action : actions) {
            lintScript(action.getScripts(), mdObj);
        }
    }

    private void lintScript(List<Script> scripts, BasicObject mdObj) {
        for (Script script : scripts) {
            if (mdObj == null && !checkItem(script)) {
                continue;
            }
            if (script.getName().equals(Constants.finderSQLGet) ||
                    script.getName().equals(Constants.finderSQLSel) ) {
                continue;
            }
            List<Issue> issueList = lint.lint("ConceptWave",script.toString()).getIssues();
            analyzeIssues(issueList, mdObj, script);
        }
    }

    private boolean skipIssue (Issue issue) {
        boolean skip = false;
        if (issue.getEvidence() == null ||
                Constants.errUnexpectedElseAfterReturn.equals(issue.getRaw()) ||
                Constants.errVarOnTop.equals(issue.getRaw()) ||
                Constants.errValueAssignment.equals(issue.getRaw()) ||
                Constants.errDontDeclareVarInLoops.equals(issue.getRaw()) ||
                Constants.errArrayNotation.equals(issue.getRaw())) {
            skip = true;
        }
        return skip;
    }

    private void analyzeIssues(List<Issue> issueList, BasicObject mdObj, BasicObject lintObj) {
        for (Issue issue : issueList) {
            if (skipIssue(issue)) {
                continue;
            }
            if (Constants.errMissngDeffinition.equals(issue.getRaw())) {
                boolean identified = false;
                ScriptObject parent = Utility.getObject(issue.getReason());
                if (mdObj instanceof Document) {
                    identified = checkDocumentIssues(issue, (Document) mdObj, lintObj, parent);
                } else if (mdObj instanceof  Finder) {
                    identified = checkFinderIssues(issue, (Finder) mdObj, (Script) lintObj, parent);
                }

                if (!identified){
                    ScriptObject obj = validateIssue(issue);
                    if (obj != null) {
                        logIssue(new CWIssue(issue, obj, mdObj, lintObj));
                    }
                }
            } else {
                logIssue(new CWIssue(issue, mdObj, lintObj));
            }
        }
    }

    private boolean checkDocumentIssues (Issue issue, Document document, BasicObject lintObj, ScriptObject parent) {
        boolean identified = false;
        if (scriptObjectTypeDocument == parent.getType()) {
            identified = true;
            /*
             * DEBUG MODE
             *
            if (document.getName().equals("doc_AccessPortCustomerOrder")) {
                System.out.print("debug");
            }
            */

            ScriptObject leave = Utility.getChildObject(issue.getEvidence(), parent, issue.getCharacter());
            if (null != leave && !validateLeave(document, leave.getName()) && validateIssue(issue) != null) {
                logIssue(new CWIssue(issue, leave, document, lintObj));
            }
        }
        return identified;
    }

    private boolean checkFinderIssues(Issue issue, Finder finder, Script lintObj, ScriptObject parent) {
        Document finderDoc = null;
        boolean identified = true;

        if (parent.getName().equals(Constants.objDocument)) {
            return true;
        } if (parent.getName().equals(Constants.objSearchData)) {
            finderDoc = getDocument(finder.getInput());
        } else if (parent.getName().equals(Constants.objInput)) {
            finderDoc = getDocument(finder.getOutput());
        } else { // nothing to do within this method
            identified = false;
        }
        /*
         * DEBUG MODE
         *
        if (finder.getName().equals("find_WorklistExtended")) {
            System.out.print("debug");
        }
        */


        ScriptObject leave = Utility.getChildObject(issue.getEvidence(), parent, issue.getCharacter());
        if (leave == null) { //object itself or method
            if (lintObj.getMdScript().contains(Constants.objFunction + " " + parent.getName())) { //check if function exists
                identified = true;
            }
        } else if (finderDoc != null && !validateLeave(finderDoc, leave.getName())) {
            //If finder is related to template, finderDoc will be null.
            //Should be checked manuelly
            logIssue(new CWIssue(issue, leave, finder, lintObj));
        }
        return identified;
    }

    private ScriptObject validateIssue(Issue issue){
        ScriptObject namespace = Utility.getObject(issue.getReason());
        String namespaceGUID = getNamespaceGUID(namespace.getName());
        ScriptObject method = Utility.getChildObject(issue.getEvidence(), namespace, issue.getCharacter());
        if (!validateObject(method, namespaceGUID)) {
            return method;
        }
        return null;
    }

    private boolean validateLeave (Document doc, String leave) {
        while (doc != null) {
            for (Leaf leaf : doc.getLeafCollection()) {
                if (leaf.getName().equals(leave)) {
                    return true;
                }
            }
            doc = getDocument(doc.getBaseObject());
        }
        return false;
    }

    private boolean validateObject(ScriptObject obj, String namespaceGUID) {
        if (obj == null) {
            return true;
        }
        if (mdObjects.get(namespaceGUID + ":" + obj.getName()) != null ||
                mdObjects.get(obj.getNamespace() + ":" + obj.getName()) != null) {
            return true;
        }
        for (Script script: metadata.getScriptsDictionary()){
            if (namespaceGUID == null) {
                break;
            }
            if (obj.getName().equals(script.getName()) && namespaceGUID.equals(script.getNameSpace())) {
                mdObjects.put(namespaceGUID + ":" + obj.getName(), obj.getName());
                return true;
            }
        }
        return false;
    }

    private String getNamespaceGUID(String namespace) {
        for (HashMap.Entry entry : namespaces.entrySet()) {
            if (namespace.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    private Document getDocument(String guid) {
        for (Document document : metadata.getDocumentDictionary()) {
            if (document.getGuid().equals(guid)) {
                return document;
            }
        }
        return null;
    }

    private void logIssue(CWIssue issue) {
        logger.info(issue.toString());
    }
}
