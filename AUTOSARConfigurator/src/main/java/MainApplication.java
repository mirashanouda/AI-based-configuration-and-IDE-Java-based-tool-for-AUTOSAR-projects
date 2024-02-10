import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.w3c.dom.Document;

public class MainApplication extends JFrame implements ConfiguratorInterface {

    // Member variables
    DefaultTreeModel modulesTree;
    static final int maxNodes = 100000;
    static List<ContainerItem> containerDef = new ArrayList<>();
    static int[] par = new int[maxNodes];

    public MainApplication() {
        initComponents();
        // TODO: This title needs to be set each time the 
        jLabel2.setText("Can Network Manager");
        SidebarTreeConstruction();
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

    private void SidebarTreeConstruction()
    {
        String bswmdPath = "src/main/java/CanNM_BSWMD.arxml";
        
        Element root = FileReader(bswmdPath);
        Element containers = (Element) root.getElementsByTagName("CONTAINERS").item(0);

        BSWMDParserDFS(containers, -1);
        
        for (int i = containerDef.size() - 1; i > 0; i--) {
            DefaultMutableTreeNode parentNode = containerDef.get(par[i]).getGUINode();
            DefaultMutableTreeNode currentNode = containerDef.get(i).getGUINode();
            parentNode.add(currentNode);
            ContainerItem parentContainer = containerDef.get(par[i]);
            parentContainer.setGUINode(parentNode);
            containerDef.set(par[i], parentContainer); // containerDef[par[i]] = parentNode
        }

        // TODO when generalizing to all modules, make sure to attach all containers.
        // In case of CanNM, we only have one main container.
        DefaultMutableTreeNode canNM_root_node = new DefaultMutableTreeNode("CanNM");
        System.out.println(containerDef);
        ContainerItem c = containerDef.get(0);
        canNM_root_node.add(c.getGUINode()); 
        modulesTree = (DefaultTreeModel)jTree1.getModel();
        modulesTree.setRoot(canNM_root_node);
        modulesTree.reload();
        jTree1.setModel(modulesTree);
    }
    
    @Override
    public void BSWMDParserDFS(Node ecucContainer, int parentIndex)
    {
        NodeList containerNodes = ecucContainer.getChildNodes();
        for (int i = 0; i < containerNodes.getLength(); i++) {
            Node containerNode = containerNodes.item(i);
            if ( containerNode.getNodeName().equals("ECUC-PARAM-CONF-CONTAINER-DEF")) {
                ContainerItem c = processBSWMDContainer((Element) containerNode);
                containerDef.add(c);
                int idx = containerDef.size() - 1;
                par[idx] = parentIndex;

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
                containerDef.add(c);
                int idx = containerDef.size() - 1;
                par[idx] = parentIndex;

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
    public void PrintingContainersTree(){
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

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setBackground(new java.awt.Color(126, 231, 212));
        setFont(new java.awt.Font("Chilanka", 1, 24)); // NOI18N
        setForeground(new java.awt.Color(153, 255, 204));

        jLabel1.setFont(new java.awt.Font("Chilanka", 1, 24)); // NOI18N
        jLabel1.setText("Configurator");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N

        jScrollPane2.setHorizontalScrollBar(null);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1013, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 353, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1015, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(16, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(418, 418, 418))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTree1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseClicked
        TreePath clickedPath = jTree1.getPathForLocation(evt.getX(), evt.getY());
        JPanel innerPanel2 = new JPanel(new GridBagLayout());
        jScrollPane2.setViewportView(innerPanel2);
        jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
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
                jLabel2.setText(userObject.toString() + " Parameters:");
            }
            
            for (int i = 0; i < containerDef.size(); i++) {
                // TODO: change to map:
                if (containerDef.get(i).name.equals(userObject.toString())) {
//                    System.out.println(i);
                    ContainerItem c = containerDef.get(i);
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
