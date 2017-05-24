package tr.net.susantez.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.net.susantez.dataMapping.Metadata;
import tr.net.susantez.dataMapping.metadata.*;
import tr.net.susantez.dataMapping.metadata.Process;
import tr.net.susantez.util.Constants;
import tr.net.susantez.util.Utility;

import java.io.File;
import java.util.List;

/**
 * Created by serkan.susantez on 4/18/2017.
 * Validator for ConceptWave v4.2 Metadata
 * Lists unused objects
 */
public class FindUnusedMetadataObjects {
    private static final Logger logger = LogManager.getLogger(FindUnusedMetadataObjects.class);


    private File metadataFile;
    private Metadata metadata;
    private int totalObjectsValidated = 0;
    private int totalUnusedObjects = 0;
    private int appType;

    public FindUnusedMetadataObjects(String metadataPath, int type) {
        metadataFile = new File(metadataPath);
        metadata = null;
        appType = type;
    }

    public void validateMetadata() {
        metadata = Utility.parseMetadata(metadataFile);
        logger.info("type|namespace|name|guid");
        if (metadata != null) {
            if (appType == Constants.typeAll || appType == Constants.typeDocument) {
                findUnusedDataTypes();
                findUnusedDocuments();
            }
            if (appType == Constants.typeAll || appType == Constants.typeFinder) {
                findUnusedFinders();
            }
            if (appType == Constants.typeAll || appType == Constants.typeScript) {
                findUnusedScripts();
            }
        }
        logger.info("Objects scanned :" + totalObjectsValidated);
        logger.info("Unused objects :" + totalUnusedObjects);
    }

    private void findUnusedDocuments() {
        List<Document> documents = metadata.getDocumentDictionary();
        for (Document document : documents) {
            totalObjectsValidated++;
            String scriptCall = nameSpaceToString(document.getNameSpace()) + ":" + document.getName();
            if (!document.isExtension() && !findObject(document.getGuid(), document.getName(), scriptCall, Constants.objDocument)) {
                logBasicObject(document, Constants.objDocument);
            }
        }
    }

    private void findUnusedFinders() {
        List<Finder> finders = metadata.getFinderDictionary();
        for (Finder finder : finders) {
            totalObjectsValidated++;
            String scriptCall = nameSpaceToString(finder.getNameSpace()) + ":" + finder.getName();
            if (!findObject(finder.getGuid(), finder.getName(), scriptCall, Constants.objFinder)) {
                logBasicObject(finder, Constants.objFinder);
            }
        }
    }

    private void findUnusedDataStructures() {
        List<DataStructure> dataStructures = metadata.getDataStructureDictionary();
        for (DataStructure dataStructure : dataStructures) {
            String scriptCall = nameSpaceToString(dataStructure.getNameSpace()) + ":" + dataStructure.getName();
            if (!findObject(dataStructure.getGuid(), dataStructure.getName(), scriptCall, Constants.objDataStructure)) {
                logBasicObject(dataStructure, Constants.objDataStructure);
            }
        }
    }

    private void findUnusedMenus() {
        List<Menu> menus = metadata.getMenuDictionary();
        for (Menu menu : menus) {
            String scriptCall = nameSpaceToString(menu.getNameSpace()) + ":" + menu.getName();
            if (!findObject(menu.getGuid(), menu.getName(), scriptCall, Constants.objMenu)) {
                logBasicObject(menu, Constants.objMenu);
            }
        }
    }

    private void findUnusedScripts() {
        List<Script> scripts = metadata.getScriptsDictionary();
        for (Script script : scripts){
            totalObjectsValidated++;
            String scriptCall = nameSpaceToString(script.getNameSpace()) + "." + script.getName();
            if (!findObject(script.getGuid(), script.getName(), scriptCall, Constants.objScript)) {
                logBasicObject(script, Constants.objScript);
            }
        }
    }

    private boolean findObject(String guid, String name, String nameWithNamespace, String type) {

        if (searchInFinders(guid, name, nameWithNamespace)) return true;
        if (searchInConversionMaps(guid, nameWithNamespace)) return true;
        if (searchInDocuments(guid, nameWithNamespace)) return true;
        if (searchInOrders(guid, nameWithNamespace)) return true;
        if (searchInMenus(guid, nameWithNamespace)) return true;
        if (searchInProcesses(guid, nameWithNamespace)) return true;
        if (searchInScripts(guid, nameWithNamespace)) return true;
        if (searchInPermissions(guid, nameWithNamespace)) return true;
        if (searchInBindings(guid, nameWithNamespace)) return true;

        if (!Constants.objScript.equals(type)) {
            if (searchInDataStructures(guid)) return true;
            if (searchInInterfaces(guid)) return true;
            if (searchInElementTypes(guid)) return true;
        }

        return false;
    }


    //Check Finders input and output
    //Check Finder scripts
    private boolean searchInFinders (String guid, String name, String nameWithNamespace) {
        List<Finder> finders = metadata.getFinderDictionary();
        for (Finder finder : finders) {
            if (guid.equals(finder.getGuid())) continue;
            if (guid.equals(finder.getInput()) || guid.equals(finder.getOutput())) {
                return true;
            }
            if (hasValueInActionScripts(finder.getActions(), nameWithNamespace)) {
                return true;
            }
            if (hasValueInActionScripts(finder.getActions(), name)) {
                return true;
            }
        }
        return false;
    }
    //Check conversionMap source and target
    //Check conversionMap scripts
    private boolean searchInConversionMaps (String guid, String nameWithNamespace) {
        List<ConversionMap> maps = metadata.getConversionMapDictionary();
        for (ConversionMap conversionMap : maps) {
            if (guid.equals(conversionMap.getGuid())) continue;
            if (guid.equals(conversionMap.getSource()) || guid.equals(conversionMap.getTarget())) {
                return true;
            }
            if (hasValueInActionScripts(conversionMap.getActions(), nameWithNamespace)) {
                return true;
            }
        }
        return false;
    }

    //Check document scripts
    //Check form scripts
    private boolean searchInDocuments (String guid, String nameWithNamespace) {
        List<Document> documents = metadata.getDocumentDictionary();
        for (Document document : documents) {
            if (guid.equals(document.getGuid())) continue;
            if (hasValueInActionScripts(document.getActionCollection(), nameWithNamespace)) {
                return true;
            }
            if (guid.equals(document.getMenu())) {
                return true;
            }
            List<Form> forms = document.getFormCollection();
            for (Form form : forms) {
                if (hasValueInActionScripts(form.getActions(), nameWithNamespace)) {
                    return true;
                }
                for(String finder : form.getFinders()) {
                    if (guid.equals(finder)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Check Orders orderItems
    //Check Order scripts
    private boolean searchInOrders (String guid, String nameWithNamespace) {
        List<Order> orders = metadata.getOrderDictionary();
        for (Order order : orders) {
            if (guid.equals(order.getGuid())) continue;
            for (String orderItem : order.getOrederItems()) {
                if (guid.equals(orderItem)) {
                    return true;
                }
            }
            if (hasValueInActionScripts(order.getActions(), nameWithNamespace)) {
                return true;
            }
        }
        return false;
    }

    //Check menu scripts
    private boolean searchInMenus (String guid, String nameWithNamespace) {
        List<Menu> menus = metadata.getMenuDictionary();
        for (Menu menu : menus) {
            if (guid.equals(menu.getGuid())) continue;
            if (hasValueInActionScripts(menu.getActions(), nameWithNamespace)) {
                return true;
            }
            for (String worklist : menu.getWorklist()) {
                if (guid.equals(worklist)) {
                    return true;
                }
            }
        }
        return false;
    }

    //Check process scripts
    private boolean searchInProcesses (String guid, String nameWithNamespace) {
        List<Process> processes = metadata.getProcessDictionary();
        for(Process process : processes) {
            if (guid.equals(process.getGuid())) continue;
            if (hasValueInActionScripts(process.getActions(), nameWithNamespace)) {
                return true;
            }
        }
        return false;
    }

    //Check interface operations input and output parameters
    private boolean searchInInterfaces (String guid) {
        List<Interface> interfaces = metadata.getInterfaceDictionary();
        for (Interface integration : interfaces) {
            if (guid.equals(integration.getGuid())) continue;
            List<Operation> operations = integration.getOperations();
            for (Operation operation : operations){
                if(guid.equals(operation.getInput()) || guid.equals(operation.getOutput())) {
                    return true;
                }
            }
        }
        return false;
    }

    //Check DataStructure elements
    private boolean searchInDataStructures (String guid) {
        List<DataStructure> dataStructures = metadata.getDataStructureDictionary();
        for (DataStructure dataStructure : dataStructures) {
            if (guid.equals(dataStructure.getGuid())) continue;
            List<DataStructureElement> dsElements = dataStructure.getElements();
            for (DataStructureElement dsElement: dsElements) {
                if (guid.equals(dsElement.getElement())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean searchInPermissions (String guid, String nameWithNamespace) {
        List<Permission> permissions = metadata.getPermissionDictionary();
        for (Permission permission : permissions) {
            if (guid.equals(permission.getGuid())) continue;
            if (hasValueInActionScripts(permission.getActions(), nameWithNamespace)) {
                return true;
            }
        }
        return false;
    }

    //Check scripts
    private boolean searchInScripts (String guid, String nameWithNamespace) {
        List<Script> scripts = metadata.getScriptsDictionary();
        for (Script script : scripts) {
            if (guid.equals(script.getGuid())) continue;
            if (script.getMdScript().contains(nameWithNamespace)) {
                return true;
            }
        }
        return false;
    }

    //Check element type finders.
    private boolean searchInElementTypes (String guid) {
        List<Element> elements = metadata.getDataDictionary().getElementDictionary();
        for (Element element : elements) {
            if (guid.equals(element.getFinder())){
                return true;
            }
        }
        return false;
    }

    //Check binding scripts
    private boolean searchInBindings (String guid, String nameWithNamespace) {
        List<Binding> bindings = metadata.getBindingDictionary();
        for (Binding binding : bindings) {
            if (guid.equals(binding.getGuid())) continue;
            if (binding.getScript() != null && binding.getScript().contains(nameWithNamespace)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValueInActionScripts(List<Action> actions, String name) {
        for (Action action : actions) {
            for (Script script : action.getScripts()) {
                if (script.getMdScript().contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void findUnusedDataTypes() {
        List<BasicObject> dataTypes = metadata.getDataDictionary().getDataTypeDictionary();
        List<Element> elementTypes = metadata.getDataDictionary().getElementDictionary();

        for (BasicObject dataType : dataTypes) {
            totalObjectsValidated++;
            if (!hasDataTypeInDocuments(dataType) && !hasDataTypeInStructures(dataType)) {
                logBasicObject(dataType, Constants.objDataType);
            }
        }

        for (Element elementType : elementTypes) {
            totalObjectsValidated++;
            if (!hasDataTypeInDocuments(elementType) && !hasDataTypeInStructures(elementType)) {
                logBasicObject(elementType, Constants.objElementType);
            }
        }
    }

    private boolean hasDataTypeInDocuments(BasicObject dataType) {
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

    private boolean hasDataTypeInStructures(BasicObject dataType) {
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



    private void logBasicObject(BasicObject object, String type) {
        totalUnusedObjects++;
        logger.info(type + "|" + nameSpaceToString(object.getNameSpace()) + "|" + object.getName() + "|" + object.getGuid());
    }

    private String nameSpaceToString(String guid) {
        for(NameSpace ns: metadata.getNameSpaceDictionary()) {
            if (ns.getGuid().equals(guid)) {
                return ns.getName();
            }
        }
        return "";
    }
}
