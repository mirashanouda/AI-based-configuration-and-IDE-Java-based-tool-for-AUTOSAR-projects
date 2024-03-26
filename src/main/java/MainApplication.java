import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;


public class MainApplication extends JFrame implements ConfiguratorInterface {

    // Member variables
    DefaultTreeModel BSWMDTree;
    DefaultTreeModel ARXMLTree;
    static final int maxNodes = 100000;
    static List<ContainerItem> BSWMDContainers = new ArrayList<>();
    static List<ContainerItem> ARXMLContainers = new ArrayList<>();
    Map<String, ArrayList<ContainerItem>> containers_children = new LinkedHashMap<>();
    static List<String>children_with_parents =  new ArrayList<>();
    static List<ContainerItem>ARXML_containers_with_no_parent =  new ArrayList<>();
    HashMap<Node, Node> direct_parent = new HashMap<>();
    static int[] BSWMDpar = new int[maxNodes];
    static int[] ARXMLpar = new int[maxNodes];
     

    public MainApplication() {
        initComponents();
        // TODO: This title needs to be set each time we lose focus.
        BComponentName.setText("Can Network Manager");
        DSWMDConstructor();
        AComponentName.setText("Can Network Manager");
        ARXMLConstructor();
        String FilePath="output.arxml";
        generateArxml(FilePath);
    }

    @Override
    public Element FileReader(String filePath)
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new File(filePath));
        } catch (IOException | ParserConfigurationException | SAXException e) {}
        return doc.getDocumentElement();
    }
    
    @Override
    public void DSWMDConstructor()
    {
        String bswmdPath = "src/main/java/CanNM_BSWMD.arxml";
        
        Element root = FileReader(bswmdPath);
        Element containers = (Element) root.getElementsByTagName("CONTAINERS").item(0);

        BSWMDParserDFS(containers, -1);
        
        for (int i = BSWMDContainers.size() - 1; i > 0; i--) {
            DefaultMutableTreeNode parentNode = BSWMDContainers.get(BSWMDpar[i]).getGUINode();
            DefaultMutableTreeNode currentNode = BSWMDContainers.get(i).getGUINode();
            parentNode.add(currentNode);
            ContainerItem parentContainer = BSWMDContainers.get(BSWMDpar[i]);
            parentContainer.setGUINode(parentNode);
            BSWMDContainers.set(BSWMDpar[i], parentContainer);
        }

        // TODO when generalizing to all modules, make sure to attach all containers.
        // In case of CanNM, we only have one main container.
        DefaultMutableTreeNode canNM_root_node = new DefaultMutableTreeNode("CanNM");
       // System.out.println(BSWMDContainers);
        ContainerItem c = BSWMDContainers.get(0);
        canNM_root_node.add(c.getGUINode()); 
        BSWMDTree = (DefaultTreeModel)jTree1.getModel();
        BSWMDTree.setRoot(canNM_root_node);
        BSWMDTree.reload();
        jTree1.setModel(BSWMDTree);
    }
    
    @Override
    public void ARXMLConstructor()
    {
        String arxmlPath = "src/main/java/CanNm_Template.arxml";
        
        Element root = FileReader(arxmlPath);
        //System.out.print(root);
        Element first = (Element) root.getElementsByTagName("AR-PACKAGES").item(0);
        Element second = (Element) first.getElementsByTagName("AR-PACKAGE").item(0);
        Element third = (Element) second.getElementsByTagName("ELEMENTS").item(0);
        Element fourth = (Element) third.getElementsByTagName("ECUC-MODULE-CONFIGURATION-VALUES").item(0);
        Element containers = (Element) fourth.getElementsByTagName("CONTAINERS").item(0);

        ARXMLParserDFS(containers, -1);
        

        for (int i = ARXMLContainers.size() - 1; i > 0; i--) {
            //System.out.println(ARXMLContainers.get(i).name + " "+ARXMLContainers.get(i).UUID);
            //System.out.println(ARXMLContainers.get(i).name + " "+ ARXMLpar[i]);
            //if(ARXMLpar[i] != -1){
                DefaultMutableTreeNode parentNode = ARXMLContainers.get(ARXMLpar[i]).getGUINode();
                DefaultMutableTreeNode currentNode = ARXMLContainers.get(i).getGUINode();
                parentNode.add(currentNode);
                ContainerItem parentContainer = ARXMLContainers.get(ARXMLpar[i]);
                parentContainer.setGUINode(parentNode);
                ARXMLContainers.set(ARXMLpar[i], parentContainer); // containerDef[par[i]] = parentNode
            //}
        }
        
        DefaultMutableTreeNode canNM_root_node = new DefaultMutableTreeNode("CanNM");
        //System.out.println(ARXMLContainers);
        ContainerItem c = ARXMLContainers.get(0);
        canNM_root_node.add(c.getGUINode()); 
        ARXMLTree = (DefaultTreeModel)jTree2.getModel();
        ARXMLTree.setRoot(canNM_root_node);
        ARXMLTree.reload();
        jTree2.setModel(ARXMLTree);
    }
    
    @Override
    public void BSWMDParserDFS(Node ecucContainer, int parentIndex)
    {
        NodeList containerNodes = ecucContainer.getChildNodes();
        for (int i = 0; i < containerNodes.getLength(); i++) {
            Node containerNode = containerNodes.item(i);
            if ( containerNode.getNodeName().equals("ECUC-PARAM-CONF-CONTAINER-DEF")) {
                ContainerItem c = processBSWMDContainer((Element) containerNode);
                BSWMDContainers.add(c);
                int idx = BSWMDContainers.size() - 1;
                BSWMDpar[idx] = parentIndex;

                NodeList childrenNodes = containerNode.getChildNodes();
                for (int k = 0; k < childrenNodes.getLength(); k++) {
                    Node childNode = childrenNodes.item(k);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("SUB-CONTAINERS")) {
                        BSWMDParserDFS(childNode, idx);
                    }
                    else if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("PARAMETERS")) {
                        GetBSWMDParameters((Element) childNode, idx,c);
                    }
                }
            }
        }
    }

    Integer id = 0;

    /**
     *
     * @param ecucContainer
     * @param parentIndex
     */
    public void ARXMLParserDFS(Node ecucContainer, int parentIndex)
    {
        NodeList containerNodes = ecucContainer.getChildNodes();

        for (int i = 0; i < containerNodes.getLength(); i++) {
            Node containerNode = containerNodes.item(i);
            if ( containerNode.getNodeName().equals("ECUC-CONTAINER-VALUE")) {
                String containerName = ((Element)containerNode).getElementsByTagName("SHORT-NAME").item(0).getTextContent();
                ContainerItem c = new ContainerItem(containerName, String.valueOf(id), "", "");
                id++;
                ARXMLContainers.add(c);
                //containers_children.put(c.UUID, new ArrayList<>());
                int idx = ARXMLContainers.size() - 1;
                ARXMLpar[idx] = parentIndex;

                NodeList parametersNodes = containerNode.getChildNodes();
                for (int j = 0; j < parametersNodes.getLength(); j++) {
                    Node paramsNode = parametersNodes.item(j);
                    if (paramsNode.getNodeType() == Node.ELEMENT_NODE && paramsNode.getNodeName().equals("PARAMETER-VALUES")) {
                        GetARXMLParameters((Element) paramsNode, idx, c);
                    }
                }

                NodeList subContainersNodes = containerNode.getChildNodes();
                for (int k = 0; k < subContainersNodes.getLength(); k++) {
                    Node subContainerNode = subContainersNodes.item(k);
                    if (subContainerNode.getNodeType() == Node.ELEMENT_NODE && subContainerNode.getNodeName().equals("SUB-CONTAINERS")) {
                        Node sub_node = subContainerNode.getFirstChild();
                          
                        ARXMLParserDFS(subContainerNode, idx);
                    }
                }
                
            }
        }
    }

    
    @Override
    public ContainerItem processBSWMDContainer(Element ecucContainer) {
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

    @Override
    public void GetBSWMDParameters(Element params, int idx,ContainerItem c) {
        NodeList integerParams = params.getElementsByTagName("ECUC-INTEGER-PARAM-DEF");
        processParameters(integerParams, "INTEGER",c);

        NodeList floatParams = params.getElementsByTagName("ECUC-FLOAT-PARAM-DEF");
        processParameters(floatParams, "FLOAT",c);

        NodeList booleanParams = params.getElementsByTagName("ECUC-BOOLEAN-PARAM-DEF");
        processParameters(booleanParams, "BOOLEAN",c);

        NodeList enumerationParams = params.getElementsByTagName("ECUC-ENUMERATION-PARAM-DEF");
        processParameters(enumerationParams, "ENUMERATION",c);
    }
    
    @Override
    public void GetARXMLParameters(Element params, int idx,ContainerItem c) {
        NodeList numericalVals = params.getElementsByTagName("ECUC-NUMERICAL-PARAM-VALUE");
        NodeList TextualVals = params.getElementsByTagName("ECUC-TEXTUAL-PARAM-VALUE");


        for (int i = 0; i < numericalVals.getLength(); i++) {
            Element numericalValElement = (Element) numericalVals.item(i);
            NodeList valueList = numericalValElement.getElementsByTagName("VALUE");
            NodeList definitionRefList = numericalValElement.getElementsByTagName("DEFINITION-REF");
            String val = "";

            if (valueList.getLength() > 0) {
                Element valueElement = (Element) valueList.item(0);
                val = valueElement.getTextContent();
            }

            if (definitionRefList.getLength() > 0) {
                Element definitionRefElement = (Element) definitionRefList.item(0);
                String destAttribute = definitionRefElement.getAttribute("DEST");
                if ("ECUC-INTEGER-PARAM-DEF".equals(destAttribute)) {
                    String name = definitionRefElement.getTextContent();
                    String[] pathParts = name.split("/");
                    name = pathParts[pathParts.length - 1];
                    Range range = null;

                    ParameterItem p = new IntegerParameter( name,"", "","", 0, 0, true, Integer.parseInt(val), range);
                    //System.out.println(Integer.parseInt(val));
                    c.parametersList.add(p);
                }
                if ("ECUC-FLOAT-PARAM-DEF".equals(destAttribute)) {
                    String name = definitionRefElement.getTextContent();
                    String[] pathParts = name.split("/");
                    name = pathParts[pathParts.length - 1];
                    ParameterItem p = new FloatParameter( name,"", "", "", 0,0, true, Float.parseFloat(val), null);
                    c.parametersList.add(p);
                }
                if ("ECUC-BOOLEAN-PARAM-DEF".equals(destAttribute)) {
                    String name = definitionRefElement.getTextContent();
                    String[] pathParts = name.split("/");
                    name = pathParts[pathParts.length - 1];
                    ParameterItem p = new BooleanParameter(name, "", "", "", 0, 0, true, Boolean.parseBoolean(val));
                    c.parametersList.add(p);
                }
            }
        }
        for (int i = 0; i < TextualVals.getLength(); i++) {
            Element TextualValElement = (Element) TextualVals.item(i);

            NodeList valueList = TextualValElement.getElementsByTagName("VALUE");
            NodeList definitionRefList = TextualValElement.getElementsByTagName("DEFINITION-REF");

            String val = "";
            if (valueList.getLength() > 0) {
                Element valueElement = (Element) valueList.item(0);
                 val = valueElement.getTextContent();
            }

            if (definitionRefList.getLength() > 0) {
                Element definitionRefElement = (Element) definitionRefList.item(0);
                String destAttribute = definitionRefElement.getAttribute("DEST");
                if ("ECUC-ENUMERATION-PARAM-DEF".equals(destAttribute)) {
                    String name = definitionRefElement.getTextContent();
                    String[] pathParts = name.split("/");
                    name = pathParts[pathParts.length - 1];
                    ParameterItem p = new EnumParameter(name,"", "", "",0, 0, EnumValue.valueOf(val));
                   // System.out.println(EnumValue.valueOf(val));
                    c.parametersList.add(p);
                }
            }
        }
    }
    
    @Override
    public void processParameters(NodeList paramNodes, String type,ContainerItem c) {
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Element paramNode = (Element) paramNodes.item(i);
            ParameterItem p = processParameter(paramNode, type);
            c.parametersList.add(p);
        }
    }

    @Override
    public ParameterItem processParameter(Element ecucParameter, String typ) {
        String name = ecucParameter.getElementsByTagName("SHORT-NAME").item(0).getTextContent();
        String UUID = "";
        String Desc = ecucParameter.getElementsByTagName("DESC").item(0).getTextContent();
        
        Element defaultValueElement = (Element) ecucParameter.getElementsByTagName("DEFAULT-VALUE").item(0);
        boolean hasDefaultValue = (defaultValueElement != null);
        
        int LM = Integer.parseInt(ecucParameter.getElementsByTagName("LOWER-MULTIPLICITY").item(0).getTextContent());
        String UMElementName = "UPPER-MULTIPLICITY-INFINITE";
        Element UMElement = (Element) ecucParameter.getElementsByTagName(UMElementName).item(0);
        int UM;
        if (UMElement != null) {
            UM = Integer.MAX_VALUE;
        }
        else {
            NodeList UMList = ecucParameter.getElementsByTagName("UPPER-MULTIPLICITY");
            if (UMList.getLength() > 0) {
                UM = Integer.parseInt(UMList.item(0).getTextContent());
            } else {
                UM = 0;  // or any default value you want to assign when both are null
            }
        }

        int MINLength = ecucParameter.getElementsByTagName("MIN").getLength();
        int MAXLength = ecucParameter.getElementsByTagName("MAX").getLength();
        
        float startRange = (MINLength > 0) ? Float.parseFloat(ecucParameter.getElementsByTagName("MIN").item(0).getTextContent()) : -1;
        float endRange = Float.POSITIVE_INFINITY;
        if (MAXLength > 0) {
            String end = ecucParameter.getElementsByTagName("MAX").item(0).getTextContent();
            endRange = (!end.equals("Inf")) ? Float.parseFloat(end) : Float.POSITIVE_INFINITY;
        }

        switch (typ) {
            case "INTEGER":
            {
                int defVal = -1;
                String value;
                if (hasDefaultValue) {
                    value = defaultValueElement.getTextContent();
                    defVal = Integer.parseInt(value);
                }
                return new IntegerParameter(name, UUID, "", Desc, LM, UM, hasDefaultValue, defVal, new Range(startRange, endRange));
            }
            case "FLOAT":
            {
                float defVal = -1;
                String value;
                if (hasDefaultValue) {
                    value = defaultValueElement.getTextContent();
                    defVal = Float.parseFloat(value);
                }
                return new FloatParameter(name, UUID, "", Desc, LM, UM, hasDefaultValue, defVal, new Range(startRange, endRange));
            }
            case "BOOLEAN":
            {
                boolean defVal = false;
                String value;
                if (hasDefaultValue) {
                    value = defaultValueElement.getTextContent();
                    defVal = (!"false".equals(value));
                }
                return new BooleanParameter(name, UUID, "", Desc, LM, UM, hasDefaultValue, defVal);
            }
            default:
                return new EnumParameter(name, UUID, "", Desc, LM, UM, null);
        }
    }
    
    @Override
    public void PrintingContainersTree(List<ContainerItem> containerDef){
        // Printing children containers
        for (int i = 0 ; i < containerDef.size();i++) {
            // if the node has children
            if (containerDef.get(i).getGUINode().getChildCount() >= 1){
                System.out.printf("Container: %s\n", containerDef.get(i).name);
                for (int j = 0; j < containerDef.get(i).getGUINode().getChildCount(); j++)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) containerDef.get(i).getGUINode().getChildAt(j);
                    System.out.printf("\t %s\n", node.getUserObject());
                    for (int k = 0; k < node.getChildCount(); k++){
                        DefaultMutableTreeNode child_node = (DefaultMutableTreeNode) node.getChildAt(k);
                        System.out.printf("\t\t %s\n", child_node.getUserObject());
                    }
                }
                break;
            }
        }
    }

    @Override
    public void PrintingParameters(ContainerItem container){
        // Printing children Parameters
        List<ParameterItem> paramsList = container.getParametersList();
        System.out.printf("Parameters of: %s (%d)\n", container.getName(), paramsList.size());
        for (int i = 0 ; i < paramsList.size();i++) {
            System.out.printf("\t%s\n", paramsList.get(i).getName());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        BjPanel = new javax.swing.JPanel();
        BTreeScrollPane = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        BParamsScrollPane = new javax.swing.JScrollPane();
        BParamPanel = new javax.swing.JPanel();
        BComponentName = new javax.swing.JLabel();
        AjPanel = new javax.swing.JPanel();
        AComponentName = new javax.swing.JLabel();
        ATreeScrollPane = new javax.swing.JScrollPane();
        jTree2 = new javax.swing.JTree();
        AParamScrollPane = new javax.swing.JScrollPane();
        AParamPanel = new javax.swing.JPanel();
        AParamScrollPane2 = new javax.swing.JScrollPane();
        AParamPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setBackground(new java.awt.Color(126, 231, 212));
        setFont(new java.awt.Font("Chilanka", 1, 24)); // NOI18N
        setForeground(new java.awt.Color(153, 255, 204));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree1MouseClicked(evt);
            }
        });
        BTreeScrollPane.setViewportView(jTree1);

        BParamsScrollPane.setHorizontalScrollBar(null);

        javax.swing.GroupLayout BParamPanelLayout = new javax.swing.GroupLayout(BParamPanel);
        BParamPanel.setLayout(BParamPanelLayout);
        BParamPanelLayout.setHorizontalGroup(
            BParamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 3513, Short.MAX_VALUE)
        );
        BParamPanelLayout.setVerticalGroup(
            BParamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 353, Short.MAX_VALUE)
        );

        BParamsScrollPane.setViewportView(BParamPanel);

        BComponentName.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N

        javax.swing.GroupLayout BjPanelLayout = new javax.swing.GroupLayout(BjPanel);
        BjPanel.setLayout(BjPanelLayout);
        BjPanelLayout.setHorizontalGroup(
            BjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BjPanelLayout.createSequentialGroup()
                .addGap(269, 269, 269)
                .addGroup(BjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BjPanelLayout.createSequentialGroup()
                        .addComponent(BComponentName, javax.swing.GroupLayout.PREFERRED_SIZE, 755, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 311, Short.MAX_VALUE))
                    .addComponent(BParamsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(BjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(BjPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BTreeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(1089, Short.MAX_VALUE)))
        );
        BjPanelLayout.setVerticalGroup(
            BjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BjPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(BComponentName, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(BParamsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(BjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(BjPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane1.addTab("BSWMD", BjPanel);

        AjPanel.setPreferredSize(new java.awt.Dimension(379, 308));

        AComponentName.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N

        treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree2.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree2MouseClicked(evt);
            }
        });
        ATreeScrollPane.setViewportView(jTree2);

        AParamScrollPane.setHorizontalScrollBar(null);

        javax.swing.GroupLayout AParamPanelLayout = new javax.swing.GroupLayout(AParamPanel);
        AParamPanel.setLayout(AParamPanelLayout);
        AParamPanelLayout.setHorizontalGroup(
            AParamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1040, Short.MAX_VALUE)
        );
        AParamPanelLayout.setVerticalGroup(
            AParamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 353, Short.MAX_VALUE)
        );

        AParamScrollPane.setViewportView(AParamPanel);

        AParamScrollPane2.setHorizontalScrollBar(null);

        javax.swing.GroupLayout AParamPanel2Layout = new javax.swing.GroupLayout(AParamPanel2);
        AParamPanel2.setLayout(AParamPanel2Layout);
        AParamPanel2Layout.setHorizontalGroup(
            AParamPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1040, Short.MAX_VALUE)
        );
        AParamPanel2Layout.setVerticalGroup(
            AParamPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 353, Short.MAX_VALUE)
        );

        AParamScrollPane2.setViewportView(AParamPanel2);

        javax.swing.GroupLayout AjPanelLayout = new javax.swing.GroupLayout(AjPanel);
        AjPanel.setLayout(AjPanelLayout);
        AjPanelLayout.setHorizontalGroup(
            AjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AjPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ATreeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(AjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(AComponentName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(AjPanelLayout.createSequentialGroup()
                        .addComponent(AParamScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 529, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(AParamScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 529, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        AjPanelLayout.setVerticalGroup(
            AjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AjPanelLayout.createSequentialGroup()
                .addGroup(AjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(AjPanelLayout.createSequentialGroup()
                        .addComponent(AComponentName, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(AjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(AParamScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AParamScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, AjPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ATreeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("ARXML Reader", AjPanel);

        jLabel1.setFont(new java.awt.Font("Chilanka", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("AUTOSAR Configurator");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1341, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("BSWMD");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTree1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseClicked
        TreePath clickedPath = jTree1.getPathForLocation(evt.getX(), evt.getY());
        JPanel innerPanel2 = new JPanel(new GridBagLayout());
        BParamsScrollPane.setViewportView(innerPanel2);
        BParamsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;  // X position
        gbc.gridy = 0;  // Y position, increment this for each component
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Fill horizontally
        gbc.insets = new Insets(5, 5, 5, 5);  // Optional: margins around components

        //        Border paddingBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        // Check if a valid path is clicked
       
        if (clickedPath != null) {
            // Get the last component of the path (typically a leaf node)
            DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();

            // Get the user object associated with the clicked node
            Object userObject = clickedNode.getUserObject();
            // Container Name:
            if (!userObject.toString().equals("CanNM")) {
                BComponentName.setText(userObject.toString() + " Parameters:");
            }

            for (int i = 0; i < BSWMDContainers.size(); i++) {
                // TODO: change to map:
                if (BSWMDContainers.get(i).name.equals(userObject.toString())) {
                    //                    System.out.println(i);
                    ContainerItem c = BSWMDContainers.get(i);
                    for (int j = 0; j < c.parametersList.size(); j++) {
                        ParameterItem param = c.parametersList.get(j);
                        JLabel paramName = new JLabel(param.name);
                        paramName.setFont(new Font("Arial", Font.BOLD, 16));

                        if (param instanceof IntegerParameter) {
                            paramName.setText(paramName.getText() + ": Integer");
                        }

                        else if (param instanceof FloatParameter) {
                            paramName.setText(paramName.getText() + ": Float");
                        }

                        else if (param instanceof BooleanParameter) {
                            paramName.setText(paramName.getText() + ": Boolean");
                            //String[] items = {"True", "False", "Not Set"};
                            //JComboBox<String> comboBox = new JComboBox<>(items);
                            //                            if (boolParam.hasDefaultValue) {
                                //                                comboBox.setSelectedItem(boolParam.getValue() ? "True" : "False");
                                //                            } else {
                                //                                comboBox.setSelectedItem("Not Set");
                                //                            }
                        }
                        else if (param instanceof EnumParameter) {
                            paramName.setText(paramName.getText() + ": Enum");
                            //String[] items = {"CANNM_PDU_BYTE_0", "CANNM_PDU_BYTE_1", "CANNM_PDU_OFF"};
                            //JComboBox<String> comboBox = new JComboBox<>(items);
                            // switch (enumParam.getValue()) {
                                //     case CANNM_PDU_BYTE_0:
                                //         comboBox.setSelectedItem("CANNM_PDU_BYTE_0");
                                //         break;
                                //     case CANNM_PDU_BYTE_1:
                                //         comboBox.setSelectedItem("CANNM_PDU_BYTE_1");
                                //         break;
                                //     case CANNM_PDU_OFF:
                                //         comboBox.setSelectedItem("CANNM_PDU_OFF");
                                //         break;
                                // }
                            //comboBox.setSelectedItem(enumParam.getValue());
                        }
                        innerPanel2.add(paramName, gbc);
                        gbc.gridy++;

                        JTextArea textArea = new JTextArea();
                        textArea.setWrapStyleWord(true);
                        textArea.setLineWrap(true);
                        textArea.setText("Description: " + param.getDesc().replaceAll("[\n\t]", "").trim());
                        innerPanel2.add(textArea, gbc);
                        gbc.gridy++;

                        JTextArea textArea2 = new JTextArea();
                        textArea2.setWrapStyleWord(true);
                        textArea2.setLineWrap(true);
                        textArea2.setText("Default Value: ");

                        if (param instanceof IntegerParameter intParam) {
                            if (intParam.hasDefaultValue()){
                                textArea2.setText(textArea2.getText() + intParam.getDefaultValue());
                            }
                            else {
                                textArea2.setText(textArea2.getText() + "None");
                            }
                            textArea2.setText(textArea2.getText() + "\n");
                            textArea2.setText(textArea2.getText() + "Range: " + (int)intParam.getRange().getMin() + " --> " + (int)intParam.getRange().getMax());
                        }
                        else if (param instanceof FloatParameter floatParam) {
                            if (floatParam.hasDefaultValue()){
                                textArea2.setText(textArea2.getText() + floatParam.getDefaultValue());
                            }
                            else {
                                textArea2.setText(textArea2.getText() + "None");
                            }
                            textArea2.setText(textArea2.getText() + "\n");
                            textArea2.setText(textArea2.getText() + "Range: " + floatParam.getRange().getMin() + " --> " + floatParam.getRange().getMax());

                        }

                        else if (param instanceof BooleanParameter) {
                            BooleanParameter boolParam = (BooleanParameter) param;
                            if (boolParam.hasDefaultValue()){
                                textArea2.setText(textArea2.getText() + boolParam.getDefaultValue());
                            }
                            else {
                                textArea2.setText(textArea2.getText() + "None");
                            }
                        }

                        else if (param instanceof EnumParameter enumParam) {
                            textArea2.setText(textArea2.getText() + "None");
                            textArea2.setText(textArea2.getText() + "\n");
                            textArea2.setText(textArea2.getText() + "Range: CANNM_PDU_BYTE_0, CANNM_PDU_BYTE_1, CANNM_PDU_OFF");

                        }

                        innerPanel2.add(textArea2, gbc);
                        gbc.gridy++;
                    }
                    // Break the loop after finding the matching container
                    break;
                }
            }
        }
    }//GEN-LAST:event_jTree1MouseClicked

    private void jTree2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree2MouseClicked
        TreePath clickedPath = jTree2.getPathForLocation(evt.getX(), evt.getY());
        JPanel innerPanel = new JPanel(new GridBagLayout());
        AParamScrollPane.setViewportView(innerPanel);
        JPanel innerPanel2 = new JPanel(new GridBagLayout());
        AParamScrollPane2.setViewportView(innerPanel2);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 1, 5, 1); // Padding between components
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left (west)
        //System.out.println("contents");
        for (Map.Entry<String, ArrayList<ContainerItem>> entry : containers_children.entrySet()) {
            String parent = entry.getKey();
            ArrayList<ContainerItem> children = entry.getValue();
            //System.out.println("Parent: " + parent);
            for(ContainerItem child : children){
               // System.out.println("child: " + child.name);
                children_with_parents.add(child.UUID);
            }
           //System.out.println();
        }
        for(ContainerItem c:ARXMLContainers){
            System.out.println(c.UUID);
            System.out.println(c.name);
        }
        // Check if a valid path is clicked
        if (clickedPath != null) {
            // Get the last component of the path (typically a leaf node)
            DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();

            // Get the user object associated with the clicked node
            Object userObject = clickedNode.getUserObject();
            // Container Name:
            if (!userObject.toString().equals("CanNM")) {
                AComponentName.setText(userObject.toString() + " Parameters:");
            }
            for (int i = 0; i < ARXMLContainers.size(); i++) {
                // TODO: change to map:
                if (ARXMLContainers.get(i).name.equals(userObject.toString())) {
                    ContainerItem c = ARXMLContainers.get(i);

                    for (int j = 0; j < c.parametersList.size(); j++) {
                        ParameterItem param = c.parametersList.get(j);
                        JLabel paramLabel = new JLabel(param.name);
                        paramLabel.setFont(new Font("Arial", Font.PLAIN, 16));

                        JTextField textField = new JTextField();
                        textField.setPreferredSize(new Dimension(100, 30));
                        
                        JPanel targetPanel = (j % 2 == 0) ? innerPanel : innerPanel2;
                        gbc.gridx = 0; // Column for labels
                        targetPanel.add(paramLabel, gbc);
                        

                        if (param instanceof IntegerParameter) {
                            IntegerParameter intParam = (IntegerParameter) param;
                            if (intParam.hasDefaultValue() == true) {
                                textField.setText(String.valueOf(intParam.getValue()));
                            }
                            gbc.gridx = 1; // Column for text fields
                            targetPanel.add(textField, gbc);
                        }
                        else if (param instanceof FloatParameter) {
                            FloatParameter floatParam = (FloatParameter) param;
                            if (floatParam.hasDefaultValue() == true) {
                                textField.setText(String.valueOf(floatParam.getValue()));
                            }
                            gbc.gridx = 1; // Column for text fields
                            targetPanel.add(textField, gbc);
                        }
                        else if (param instanceof BooleanParameter) {
                            BooleanParameter boolParam = (BooleanParameter) param;
                            String[] items = {"True", "False", "Not Set"};
                            JComboBox<String> comboBox = new JComboBox<>(items);
                            if (boolParam.hasDefaultValue() ==  true) {
                                comboBox.setSelectedItem(boolParam.getValue() ? "True" : "False");
                            } else {
                                comboBox.setSelectedItem("Not Set");
                            }
                            gbc.gridx = 1;
                            targetPanel.add(comboBox, gbc);
                        }
                        else if (param instanceof EnumParameter) {
                            EnumParameter enumParam = (EnumParameter) param;
                            String[] items = {"CANNM_PDU_BYTE_0", "CANNM_PDU_BYTE_1", "CANNM_PDU_OFF"};
                            JComboBox<String> comboBox = new JComboBox<>(items);
                             switch (enumParam.getValue()) {
                                 case CANNM_PDU_BYTE_0 -> comboBox.setSelectedItem("CANNM_PDU_BYTE_0");
                                 case CANNM_PDU_BYTE_1 -> comboBox.setSelectedItem("CANNM_PDU_BYTE_1");
                                 case CANNM_PDU_OFF -> comboBox.setSelectedItem("CANNM_PDU_OFF");
                             }
                            comboBox.setSelectedItem(enumParam.getValue());
                            gbc.gridx = 1;
                            targetPanel.add(comboBox, gbc);
                        }
                        gbc.gridy++; // Move to the next row for the next set of components
                    }
                    // Break the loop after finding the matching container
                    break;
                }
            }   
        }
    }//GEN-LAST:event_jTree2MouseClicked

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainApplication().setVisible(true);
            }
        });
        
        
    }

    @Override
    public void generateArxml( String filePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            
            // Root element with namespace and schema location
            Element autosar = doc.createElement("AUTOSAR");
            autosar.setAttribute("xmlns", "http://autosar.org/schema/r4.0");
            autosar.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            autosar.setAttribute("xsi:schemaLocation", "http://autosar.org/schema/r4.0 AUTOSAR_00046.xsd");
            doc.appendChild(autosar);
            
            
            // AR-PACKAGES element
            Element arPackages = doc.createElement("AR-PACKAGES");
            autosar.appendChild(arPackages);
            
            // Add the following lines right after the above code to insert the <AR-PACKAGE> element
            Element arPackage = doc.createElement("AR-PACKAGE");
            arPackages.appendChild(arPackage); // Append <AR-PACKAGE> to <AR-PACKAGES>

            // Create and append the <SHORT-NAME> element to <AR-PACKAGE>
            Element shortName = doc.createElement("SHORT-NAME");
            shortName.appendChild(doc.createTextNode("AutosarConfigurator")); // Set the text content for <SHORT-NAME>
            arPackage.appendChild(shortName); // Append <SHORT-NAME> to <AR-PACKAGE>

            // Create and append the <ELEMENTS> element to <AR-PACKAGE>, if you plan to add elements under it
            Element elements = doc.createElement("ELEMENTS");
            arPackage.appendChild(elements); // Append <ELEMENTS> to <AR-PACKAGE>
            
            //module
            Element ecucModuleConfigurationValues = doc.createElement("ECUC-MODULE-CONFIGURATION-VALUES");
            //ecucModuleConfigurationValues.setAttribute("UUID", ""); // For an empty UUID for now
            elements.appendChild(ecucModuleConfigurationValues);
            
            // Create and append <SHORT-NAME>
            Element modShortName = doc.createElement("SHORT-NAME");
            modShortName.appendChild(doc.createTextNode("CanNm"));
            ecucModuleConfigurationValues.appendChild(modShortName); // Append to <ECUC-MODULE-CONFIGURATION-VALUES>

            // Create and append <DEFINITION-REF>
            Element definitionRef = doc.createElement("DEFINITION-REF");
            definitionRef.setAttribute("DEST", "ECUC-MODULE-DEF");
            definitionRef.appendChild(doc.createTextNode("/AUTOSAR/EcucDefs/CanNm"));
            ecucModuleConfigurationValues.appendChild(definitionRef); // Append to <ECUC-MODULE-CONFIGURATION-VALUES>

            // Create and append <IMPLEMENTATION-CONFIG-VARIANT>
            Element implementationConfigVariant = doc.createElement("IMPLEMENTATION-CONFIG-VARIANT");
            implementationConfigVariant.appendChild(doc.createTextNode("VARIANT-POST-BUILD"));
            ecucModuleConfigurationValues.appendChild(implementationConfigVariant); // Append to <ECUC-MODULE-CONFIGURATION-VALUES>

            // Create and append <MODULE-DESCRIPTION-REF>
            Element moduleDescriptionRef = doc.createElement("MODULE-DESCRIPTION-REF");
            moduleDescriptionRef.setAttribute("DEST", "BSW-IMPLEMENTATION");
            moduleDescriptionRef.appendChild(doc.createTextNode("/AUTOSAR/EcucDefs/CanNm"));
            ecucModuleConfigurationValues.appendChild(moduleDescriptionRef); // Append to <ECUC-MODULE-CONFIGURATION-VALUES>
            
            //CONTAINERS
            Element containers = doc.createElement("CONTAINERS");
            ecucModuleConfigurationValues.appendChild(containers);
            ARXML_containers_with_no_parent.add(ARXMLContainers.get(0));
            System.out.println(ARXMLContainers.get(0).parametersList);
            for(ContainerItem container : ARXMLContainers){
                  //System.out.println(container.name+" "+container.UUID );
                if(container.UUID == String.valueOf(0)){
                   
                    ARXML_containers_with_no_parent.add(container);
                }
            }
            for(int i = 1; i < ARXMLContainers.size();i++){
                if (!containers_children.containsKey(ARXMLContainers.get(ARXMLpar[i]).UUID)) {
                    containers_children.put(ARXMLContainers.get(ARXMLpar[i]).UUID, new ArrayList<>());
                }
                containers_children.get(ARXMLContainers.get(ARXMLpar[i]).UUID).add(ARXMLContainers.get(i));
            }
//            for (Map.Entry<String, ArrayList<ContainerItem>> entry : containers_children.entrySet()) {
//                String key = entry.getKey();
//                ArrayList<ContainerItem> value = entry.getValue();
//                for(ContainerItem c: value)
//                    System.out.println("Key: " + key + ", Values: " + c.name);
//                System.out.println();
//            }
            
            
            // Loop through each container to add them to the document
            
            for (ContainerItem container : ARXML_containers_with_no_parent) {
                System.out.println(container.name);
                containers.appendChild(createContainerElement(doc, container));
                containers.appendChild(ARXML_DFS_Costruct_containers(doc, container));
            }

            // Write the content into the ARXML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            // Set properties for formatted output
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // Set indentation amount to 4 spaces

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));

            transformer.transform(source, result);

            System.out.println("ARXML file has been generated at: " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private Element createContainerElement(Document doc, ContainerItem container) {
        Element containerElement = doc.createElement("ECUC-CONTAINER-VALUE");
        //containerElement.setAttribute("UUID", container.getUUID());
        
        // Short name
        Element shortName = doc.createElement("SHORT-NAME");
        shortName.setTextContent(container.getName());
        containerElement.appendChild(shortName);
    
        // Definition reference
        Element definitionRef = doc.createElement("DEFINITION-REF");
        definitionRef.setAttribute("DEST", "ECUC-PARAM-CONF-CONTAINER-DEF");
        definitionRef.setTextContent("/AUTOSAR/EcucDefs/CanNm/"+container.name);
        containerElement.appendChild(definitionRef);

        // Parameter values
        Element parameterValues = doc.createElement("PARAMETER-VALUES");
        containerElement.appendChild(parameterValues);

        // Adding parameters
        //System.out.println(container);
        for (ParameterItem parameter : container.getParametersList()) {
            System.out.println(container.name);
            Element parameterElement = createParameterElement(doc, parameter);
            parameterValues.appendChild(parameterElement);
        }
        return containerElement;

    }
    

    private Element createParameterElement(Document doc, ParameterItem parameter) {
        String elementType = "ECUC-NUMERICAL-PARAM-VALUE"; // Default type
        String value = null;
        if ( parameter instanceof IntegerParameter){
            elementType = "ECUC-INTEGER-PARAM-DEF";
            value = String.valueOf(((IntegerParameter) parameter).getValue());
        }
        
        else if ( parameter instanceof FloatParameter){
            elementType = "ECUC-FLOAT-PARAM-DEF";
            value = String.valueOf(((FloatParameter) parameter).getValue());
        }
        
        else if ( parameter instanceof BooleanParameter){
            elementType = "ECUC-BOOLEAN-PARAM-DEF";
             value = String.valueOf(((BooleanParameter) parameter).getValue());
        }
        
        else if ( parameter instanceof EnumParameter){
            elementType = "ECUC-ENUMERATION-PARAM-DEF";
            value = String.valueOf(((EnumParameter) parameter).getValue());
        }
        Element parametertype = doc.createElement("ECUC-NUMERICAL-PARAM-VALUE");
        Element parameterElement = doc.createElement("DEFINITION-REF");
        parameterElement.setAttribute("DEST", elementType);
        parameterElement.setTextContent("/AUTOSAR/EcucDefs/CanNm/"+parameter.name);
        Element parameterValue = doc.createElement("Value");
       // System.out.print(value);
        parameterValue.setTextContent(value);
        parametertype.appendChild(parameterElement);
        parametertype.appendChild(parameterValue);
        return parametertype;
    
    }
    public Element ARXML_DFS_Costruct_containers(Document doc,ContainerItem c)
    {   
        
        ArrayList<ContainerItem> children = containers_children.get(c.UUID);
       
        if (children != null  && !children.isEmpty()) {
            Element contain = doc.createElement("SUB-CONTAINERS");
            for (ContainerItem child : children) {
                Element subContainer = createContainerElement(doc, child); // Create sub-container
                Element subContainersElement = ARXML_DFS_Costruct_containers(doc, child); // Recursive call

                if (subContainersElement != null) {
                subContainer.appendChild(subContainersElement);
                }

                // Append the sub-container to the main container
                contain.appendChild(subContainer);
            }
            return contain;
        }
        else return null;
        
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AComponentName;
    private javax.swing.JPanel AParamPanel;
    private javax.swing.JPanel AParamPanel2;
    private javax.swing.JScrollPane AParamScrollPane;
    private javax.swing.JScrollPane AParamScrollPane2;
    private javax.swing.JScrollPane ATreeScrollPane;
    private javax.swing.JPanel AjPanel;
    private javax.swing.JLabel BComponentName;
    private javax.swing.JPanel BParamPanel;
    private javax.swing.JScrollPane BParamsScrollPane;
    private javax.swing.JScrollPane BTreeScrollPane;
    private javax.swing.JPanel BjPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTree jTree2;
    // End of variables declaration//GEN-END:variables

//    @Override
//    public void ARXMLParserDFS(Node ecucContainer, int parentIndex) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
}
