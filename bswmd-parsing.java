import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class data_structures {
    static final int maxNodes = 100000;
    static List<ContainerItem> containerDef = new ArrayList<>();
    static int[] par = new int[maxNodes];
    static List<Integer>[] children = new ArrayList[maxNodes];
    static List<ParameterItem>[] containerParameters = new ArrayList[maxNodes];

    static {
        for (int i = 0; i < maxNodes; i++) {
            children[i] = new ArrayList<>();
            containerParameters[i] = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        String filePath = "CanNM_BSWMD.arxml";
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(new File(filePath));

            Element root = doc.getDocumentElement();
            Element containers = (Element) root.getElementsByTagName("CONTAINERS").item(0);

            dfs(containers, 0, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dfs(Element ecucContainer, int level, int parentIndex) {
        ContainerItem c = processContainer(ecucContainer);
        containerDef.add(c);
        int currentIndex = containerDef.size() - 1;
        par[currentIndex] = parentIndex;

        NodeList parametersList = ecucContainer.getElementsByTagName("PARAMETERS");
        for (int i = 0; i < parametersList.getLength(); i++) {
            Element params = (Element) parametersList.item(i);
            getParameters(params, currentIndex);
        }

        NodeList subContainersList = ecucContainer.getElementsByTagName("SUB-CONTAINERS");
        for (int i = 0; i < subContainersList.getLength(); i++) {
            Element subContainer = (Element) subContainersList.item(i);
            dfs(subContainer, level + 1, currentIndex);
        }
    }

    private static ContainerItem processContainer(Element ecucContainer) {
        String name = ecucContainer.getElementsByTagName("SHORT-NAME").item(0).getTextContent();
        String LM = ecucContainer.getElementsByTagName("LOWER-MULTIPLICITY").item(0).getTextContent();
        String UM = String.valueOf(ecucContainer.getElementsByTagName("UPPER-MULTIPLICITY-INFINITE").item(0));
        if (UM != null) {
            UM = String.valueOf(Float.POSITIVE_INFINITY);
        } else {
            UM = ecucContainer.getElementsByTagName("UPPER-MULTIPLICITY").item(0).getTextContent();
        }
        String UUID = ecucContainer.getAttribute("UUID");
        return new ContainerItem(name, UUID, LM, UM);
    }

    private static void getParameters(Element params, int idx) {
        NodeList integerParams = params.getElementsByTagName("ECUC-INTEGER-PARAM-DEF");
        processParameters(integerParams, idx, "INTEGER");

        NodeList floatParams = params.getElementsByTagName("ECUC-FLOAT-PARAM-DEF");
        processParameters(floatParams, idx, "FLOAT");

        NodeList booleanParams = params.getElementsByTagName("ECUC-BOOLEAN-PARAM-DEF");
        processParameters(booleanParams, idx, "BOOLEAN");

        NodeList enumerationParams = params.getElementsByTagName("ECUC-ENUMERATION-PARAM-DEF");
        processParameters(enumerationParams, idx, "ENUMERATION");
    }

    private static void processParameters(NodeList paramNodes, int idx, String type) {
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Element paramNode = (Element) paramNodes.item(i);
            ParameterItem p = processParameter(paramNode, type);
            containerParameters[idx].add(p);
        }
    }

    private static ParameterItem processParameter(Element ecucParameter, String typ) {
        String defName = ecucParameter.getElementsByTagName("SHORT-NAME").item(0).getTextContent();
        String dataType = typ;
        String declName = (typ.equals("ENUMERATION")) ? "TEXTUAL" : "NUMERICAL";

        Element defaultValueElement = (Element) ecucParameter.getElementsByTagName("DEFAULT-VALUE").item(0);
        int isDefault = (defaultValueElement != null) ? 1 : 0;
        String value = (isDefault == 1) ? defaultValueElement.getTextContent() : "-1";
        String startRange = "";
        String endRange = "";
        if (typ == "INTEGER") {
             startRange = ecucParameter.getElementsByTagName("MIN").item(0).getTextContent();
             endRange = ecucParameter.getElementsByTagName("MAX").item(0).getTextContent();
        }
        else if (typ == "FLOAT") {
            startRange = ecucParameter.getElementsByTagName("MIN").item(0).getTextContent();
            if (startRange != null) {
                ;
            }
            else {
                startRange = "-1";
            }
            endRange = ecucParameter.getElementsByTagName("MAX").item(0).getTextContent();
            if (endRange != null) {
                ;
            }
            else{
                startRange = "-1";
            }
        }
      else{
            startRange = "-1";
            endRange = "-1";
        }

        String LM = ecucParameter.getElementsByTagName("LOWER-MULTIPLICITY").item(0).getTextContent();
        String UMElementName = "UPPER-MULTIPLICITY-INFINITE";
        Element UMElement = (Element) ecucParameter.getElementsByTagName(UMElementName).item(0);
        String UM;
        if (UMElement != null) {
            UM = String.valueOf(Float.POSITIVE_INFINITY);
        } else {
            NodeList UMList = ecucParameter.getElementsByTagName("UPPER-MULTIPLICITY");
            if (UMList.getLength() > 0) {
                UM = UMList.item(0).getTextContent();
            } else {
                UM = "";  // or any default value you want to assign when both are null
            }
        }

        System.out.println(defName + ", " + value + ", " + dataType + ", " + declName + ", " + isDefault + ", " +
                startRange + ", " + endRange + ", " + LM + ", " + UM);

        return new ParameterItem(defName, value, dataType, declName, isDefault, startRange, endRange, LM, UM);
    }
}

class ContainerItem {
    String name;
    String UUID;
    String lowerMult;
    String upperMult;

    public ContainerItem(String name, String UUID, String lowerMult, String upperMult) {
        this.name = name;
        this.UUID = UUID;
        this.lowerMult = lowerMult;
        this.upperMult = upperMult;
    }
}

class ParameterItem {
    String defName;
    String value;
    String dataType;
    String declName;
    int isDefault;
    String startRange;
    String endRange;
    String lowerMult;
    String upperMult;

    public ParameterItem(String defName, String value, String dataType, String declName, int isDefault,
                         String startRange, String endRange, String lowerMult, String upperMult) {
        this.defName = defName;
        this.value = value;
        this.dataType = dataType;
        this.declName = declName;
        this.isDefault = isDefault;
        this.startRange = startRange;
        this.endRange = endRange;
        this.lowerMult = lowerMult;
        this.upperMult = upperMult;
    }
}


