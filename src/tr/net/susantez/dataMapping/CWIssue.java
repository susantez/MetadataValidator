package tr.net.susantez.dataMapping;

import com.googlecode.jslint4java.Issue;
import org.apache.commons.lang3.StringUtils;
import tr.net.susantez.dataMapping.metadata.BasicObject;
import tr.net.susantez.util.Constants;

/**
 * Created by serkan.susantez on 5/15/2017.
 * Class that extends JSLint Issue for custom validations
 */
public class CWIssue {
    private BasicObject metadataObject;
    private BasicObject lintObject;
    private Issue issue = null;
    private String customReason = "";

    public CWIssue (Issue issue, ScriptObject object, BasicObject mdObj, BasicObject lintObj) {
        if (mdObj != null) {
            metadataObject = mdObj;
        } else {
            metadataObject = lintObj;
        }
        lintObject = lintObj;
        if (Constants.scriptObjectTypeNamespace == object.getType()) {
            customReason = object.getName() + " - Missing Namespace";
        } else {
            customReason = object.getNamespace() + ":" + object.getName() + " - Missing Object";
        }
        this.issue = issue;
    }

    public CWIssue (Issue issue, BasicObject mdObj, BasicObject lintObj) {
        this.issue = issue;
        if (mdObj != null) {
            metadataObject = mdObj;
        } else {
            metadataObject = lintObj;
        }
        lintObject = lintObj;
        customReason = issue.getReason();
    }

    public String toString() {
        return metadataObject.getNameSpace() + ":" + metadataObject.getName() + "|" +
                lintObject.getName() + "|Line:" + issue.getLine() + "|" + customReason + "|" + StringUtils.remove(issue.getEvidence(),"\t");
    }
}
