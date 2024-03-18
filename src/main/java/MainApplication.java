import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import org.w3c.dom.Document;
import javafx.util.Pair; // Import Pair class
import java.awt.BorderLayout;
import java.math.BigDecimal;


public class MainApplication extends JFrame implements ConfiguratorInterface {

    // Member variables
    DefaultTreeModel BSWMDTree;
    DefaultTreeModel ARXMLTree;
    private JTextArea logMessagesTextArea;
    static final int maxNodes = 100000;
    private Map<String, String> errorMessages = new HashMap<>();
    static List<ContainerItem> BSWMDContainers = new ArrayList<>();
    static List<ContainerItem> ARXMLContainers = new ArrayList<>();
    static int[] BSWMDpar = new int[maxNodes];
    static int[] ARXMLpar = new int[maxNodes];


    // Method to validate XML (Part 1 of validating)
    private void validateXMLFile(String filePath) {
        appendLogMessage("\nCompiling input ARXML File...");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    appendLogMessage("Warning: " + exception.getMessage());
                }

                @Override
                public void error(SAXParseException exception) throws SAXParseException {
                    //appendLogMessage("Error: " + exception.getMessage());
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXParseException {
                    //appendLogMessage("Fatal error: " + exception.getMessage());
                    throw exception;
                }
            });

            // Parse the XML file
            Document document = builder.parse(new File(filePath));
            appendLogMessage("XML is well-formed.");
            } catch (SAXParseException e) {
                appendLogMessage("Error at line " + e.getLineNumber() +
                        ", column " + e.getColumnNumber() + ": " + e.getMessage());
            } catch (Exception e) {
                appendLogMessage("An error occurred while checking the XML file: " + e.getMessage());
                e.printStackTrace();
            }
        }



    public MainApplication() {
        initComponents();
        // TODO: This title needs to be set each time we lose focus.
        BComponentName.setText("Can Network Manager");
        DSWMDConstructor();
        AComponentName.setText("Can Network Manager");
        ARXMLConstructor();
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
        System.out.println(BSWMDContainers);
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
        // Try-Catch to validate if XML is well-formed
        try {
            String arxmlPath = "src/main/java/CanNm_Template.arxml";
            validateXMLFile(arxmlPath);
            Element root = FileReader(arxmlPath);
            //System.out.print(root);
            Element first = (Element) root.getElementsByTagName("AR-PACKAGES").item(0);
            Element second = (Element) first.getElementsByTagName("AR-PACKAGE").item(0);
            Element third = (Element) second.getElementsByTagName("ELEMENTS").item(0);
            Element fourth = (Element) third.getElementsByTagName("ECUC-MODULE-CONFIGURATION-VALUES").item(0);
            Element containers = (Element) fourth.getElementsByTagName("CONTAINERS").item(0);

            ARXMLParserDFS(containers, -1);

            for (int i = ARXMLContainers.size() - 1; i > 0; i--) {
                DefaultMutableTreeNode parentNode = ARXMLContainers.get(ARXMLpar[i]).getGUINode();
                DefaultMutableTreeNode currentNode = ARXMLContainers.get(i).getGUINode();
                parentNode.add(currentNode);
                ContainerItem parentContainer = ARXMLContainers.get(ARXMLpar[i]);
                parentContainer.setGUINode(parentNode);
                ARXMLContainers.set(ARXMLpar[i], parentContainer); // containerDef[par[i]] = parentNode
            }
            
            DefaultMutableTreeNode canNM_root_node = new DefaultMutableTreeNode("CanNM");
            System.out.println(ARXMLContainers);
            ContainerItem c = ARXMLContainers.get(0);
            canNM_root_node.add(c.getGUINode()); 
            ARXMLTree = (DefaultTreeModel)jTree2.getModel();
            ARXMLTree.setRoot(canNM_root_node);
            ARXMLTree.reload();
            jTree2.setModel(ARXMLTree);
        } catch (Exception e) {
            appendLogMessage("An error occurred and the XML cannot be processed: " + e.getMessage());
            return;
        }
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

    @Override
    public void ARXMLParserDFS(Node ecucContainer, int parentIndex)
    {
        NodeList containerNodes = ecucContainer.getChildNodes();

        for (int i = 0; i < containerNodes.getLength(); i++) {
            Node containerNode = containerNodes.item(i);
            if ( containerNode.getNodeName().equals("ECUC-CONTAINER-VALUE")) {
                String containerName = ((Element)containerNode).getElementsByTagName("SHORT-NAME").item(0).getTextContent();
                ContainerItem c = new ContainerItem(containerName, "", "", "");
                ARXMLContainers.add(c);
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
                    System.out.println(Integer.parseInt(val));
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
                    System.out.println(EnumValue.valueOf(val));
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

    
    Map<String,Pair<String,String>>chk_values_map = new HashMap<>(); // for checking the correct values of parameters
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
                chk_values_map.put(name, new Pair<String, String>(Float.toString(startRange), Float.toString(endRange)));
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
                chk_values_map.put(name, new Pair<>(Float.toString(startRange),Float.toString(endRange)));
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

        
        // Creating the log messages text area
        logMessagesTextArea = new JTextArea(5, 20); // Suggests height for 5 lines
        logMessagesTextArea.setEditable(false);
        logMessagesTextArea.setText("Welcome To Our Program!"); // Set initial message
        
        JScrollPane logMessagesScrollPane = new JScrollPane(logMessagesTextArea);
        logMessagesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Creating a panel for the log messages
        JPanel logPanel = new JPanel(new BorderLayout());
        // Create and add a title label for the log messages panel
        JLabel logTitleLabel = new JLabel("Log Messages");
        logTitleLabel.setFont(new Font("Arial", Font.BOLD, 12)); // Set font to Arial, bold, size 12
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // A panel to hold the title on the left
        titlePanel.add(logTitleLabel);
    
        logPanel.add(titlePanel, BorderLayout.NORTH); // Adding the title panel at the top of the log panel

        // Adding a border to the logPanel to create a frame around the log messages
        logPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        logPanel.add(logMessagesScrollPane);

        // Creating a main container panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Adding jLabel1 at the top of the mainPanel
        mainPanel.add(jLabel1, BorderLayout.NORTH);

        // Adding the JTabbedPane to the center of mainPanel
        mainPanel.add(jTabbedPane1, BorderLayout.CENTER);

        // Adding the log messages panel at the bottom of the mainPanel
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        // Setting the mainPanel as the content pane of the JFrame
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Method to append log messages
    public void appendLogMessage(String message) {
        // Ensure updates are made in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            logMessagesTextArea.append(message + "\n");
        });
    }

    // Method to set log message, overriding any existing text
    public void setLogMessage(String message) {
        // Ensure updates are made in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            logMessagesTextArea.setText(message + "\n"); // Set new message, overriding existing text
        });
    }

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
    Map<Pair<JTextField, String>,String> parameters_val_update = new HashMap<>(); // map that has text field and paramter name and key is parameter val
    public void print_paramters_val_update_map(Map<Pair<JTextField, String>,String> parameters_val_update){
        for (Map.Entry<Pair<JTextField, String>, String> entry : parameters_val_update.entrySet()) {
            Pair<JTextField, String> pair = entry.getKey();
            String value = entry.getValue();
            
            JTextField textField = pair.getLeft();
            String parameter_name = pair.getRight();
            
            System.out.println("Key (JTextField text): " + textField);
            System.out.println("Key (String): " + parameter_name);
            System.out.println("Value: " + value);
            System.out.println();
        }
    }


    // Handling the error messages independently
    private void validateAndDisplayErrors(String parameterName, String newValue, String min_val, String max_val) {
        BigDecimal newValuen = new BigDecimal(newValue);
        BigDecimal min_valn = new BigDecimal(min_val);
        BigDecimal max_valn = new BigDecimal(max_val);
        if(newValuen.compareTo(max_valn) <= 0 && newValuen.compareTo(min_valn) >= 0) {
            // If value is correct, remove any error for this parameter and reset background
            errorMessages.remove(parameterName);
        } else {
            // If value is incorrect, update error message and set background to red
            errorMessages.put(parameterName, "Error: Value for " + parameterName + " is out of range [" + min_val + ", " + max_val + "].");
        }
        updateLogMessageArea();
    }
    
    private void updateLogMessageArea() {
        // Concatenate all current error messages
        String allErrors = String.join("\n", errorMessages.values());
    
        // Display in log message area
        setLogMessage(allErrors.isEmpty() ? "No errors. Ready to Run..." : allErrors);
    }

    
    public Boolean compare_arxml_map_to_bswmd_map(Map<Pair<JTextField, String>,String> parameters_val_update,  Map<String,Pair<String,String>>chk_values_map){
        for (Map.Entry<Pair<JTextField, String>, String> entry : parameters_val_update.entrySet()) {
            Pair<JTextField, String> parametrs_val_key = entry.getKey();
            String value = entry.getValue();
            Pair<String, String> pair = chk_values_map.get(parametrs_val_key.getRight());
            String min_val = pair.getLeft();
            String max_val = pair.getRight();
            BigDecimal valuen = new BigDecimal(value);
            BigDecimal min_valn = new BigDecimal(min_val);
            BigDecimal max_valn = new BigDecimal(max_val);
             
            if(valuen.compareTo(max_valn) <= 0 && valuen.compareTo(min_valn) >= 0){
                parametrs_val_key.getLeft().setBackground(Color.WHITE); // Change to default color
            } 
          else {
              parametrs_val_key.getLeft().setBackground(Color.RED); // Change to red indicating incorrect value
            }
        }
        return true;
    }
    private class CustomActionListener implements ActionListener { // for chnging values in arxml 
        private String parameterName;

        public CustomActionListener(String parameterName) {
            this.parameterName = parameterName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField textField = (JTextField) e.getSource();
            String newValue = textField.getText();
            parameters_val_update.put(new Pair<>(textField, parameterName), newValue);
            print_paramters_val_update_map(parameters_val_update);
              Pair<String, String> pair = chk_values_map.get(parameterName);
              String min_val = pair.getLeft();
              String max_val = pair.getRight();
              BigDecimal newValuen = new BigDecimal(newValue);
              BigDecimal min_valn = new BigDecimal(min_val);
              BigDecimal max_valn = new BigDecimal(max_val);
             
              if(newValuen.compareTo(max_valn) <= 0.0 && newValuen.compareTo(min_valn) >= 0.0){
                  textField.setBackground(Color.WHITE); // Change to default color
              } 
            else {
                textField.setBackground(Color.RED); // Change to red indicating incorrect value
            }
            validateAndDisplayErrors(parameterName, newValue, min_val, max_val);

        }
    }
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
                                 parameters_val_update.put(new Pair<>(textField, intParam.getName()), String.valueOf(intParam.getValue()));
                                 textField.addActionListener(new CustomActionListener(intParam.getName()));
                            }
                            gbc.gridx = 1; // Column for text fields
                            targetPanel.add(textField, gbc);
                        }
                        else if (param instanceof FloatParameter) {
                            FloatParameter floatParam = (FloatParameter) param;
                            if (floatParam.hasDefaultValue() == true) {
                                textField.setText(String.valueOf(floatParam.getValue()));
                                 parameters_val_update.put(new Pair<>(textField, floatParam.getName()), String.valueOf(floatParam.getValue()));
                                 textField.addActionListener(new CustomActionListener(floatParam.getName()));
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
                                parameters_val_update.put(new Pair<>(textField, boolParam.getName()), String.valueOf(boolParam.getValue()));
                                 textField.addActionListener(new CustomActionListener(boolParam.getName()));
                            } else {
                                comboBox.setSelectedItem("Not Set");
                                 parameters_val_update.put(new Pair<>(textField, boolParam.getName()), String.valueOf(boolParam.getValue()));
                                 textField.addActionListener(new CustomActionListener(boolParam.getName()));
                            }
                            gbc.gridx = 1;
                            targetPanel.add(comboBox, gbc);
                        }
                        else if (param instanceof EnumParameter) {
                            EnumParameter enumParam = (EnumParameter) param;
                            String[] items = {"CANNM_PDU_BYTE_0", "CANNM_PDU_BYTE_1", "CANNM_PDU_OFF"};
                            JComboBox<String> comboBox = new JComboBox<>(items);
                             switch (enumParam.getValue()) {
                                 case CANNM_PDU_BYTE_0:
                                     comboBox.setSelectedItem("CANNM_PDU_BYTE_0");
                                      parameters_val_update.put(new Pair<>(textField, enumParam.getName().toString()), String.valueOf(enumParam.getValue()));
                                     textField.addActionListener(new CustomActionListener(enumParam.getName()));
                                     break;
                                 case CANNM_PDU_BYTE_1 :
                                     comboBox.setSelectedItem("CANNM_PDU_BYTE_1");
                                     parameters_val_update.put(new Pair<>(textField, enumParam.getName()), String.valueOf(enumParam.getValue()));
                                     textField.addActionListener(new CustomActionListener(enumParam.getName()));
                                     break;
                                 case CANNM_PDU_OFF :
                                     comboBox.setSelectedItem("CANNM_PDU_OFF");
                                     parameters_val_update.put(new Pair<>(textField, enumParam.getName()), String.valueOf(enumParam.getValue()));
                                     textField.addActionListener(new CustomActionListener(enumParam.getName()));
                                     break;
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
            compare_arxml_map_to_bswmd_map(parameters_val_update, chk_values_map);
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
}
