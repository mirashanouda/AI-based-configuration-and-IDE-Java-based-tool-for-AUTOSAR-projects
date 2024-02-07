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


public class MainApplication extends javax.swing.JFrame {

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

    private void SidebarTreeConstruction(){
        String bswmdPath = "src/main/java/CanNM_BSWMD.arxml";
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(new File(bswmdPath));

            Element root = doc.getDocumentElement();
            Element containers = (Element) root.getElementsByTagName("CONTAINERS").item(0);

            dfs(containers, 0, -1);
        } catch (IOException | ParserConfigurationException | SAXException e) {}
        
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
    
    public static void dfs(Node ecucContainer, int level, int parentIndex) {
        NodeList containerNodes = ecucContainer.getChildNodes();
        for (int i = 0; i < containerNodes.getLength(); i++) {
            Node containerNode = containerNodes.item(i);
            if ( containerNode.getNodeName().equals("ECUC-PARAM-CONF-CONTAINER-DEF")) {
                ContainerItem c = processContainer((Element) containerNode);
                containerDef.add(c);
                int idx = containerDef.size() - 1;
                par[idx] = parentIndex;

                NodeList childrenNodes = containerNode.getChildNodes();
                for (int k = 0; k < childrenNodes.getLength(); k++) {
                    Node childNode = childrenNodes.item(k);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("SUB-CONTAINERS")) {
                        dfs(childNode, level + 1, idx);
                    }
                    else if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("PARAMETERS")) {
                        getParameters((Element) childNode, idx,c);
                    }
                }
            }
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

    private static void getParameters(Element params, int idx,ContainerItem c) {
        NodeList integerParams = params.getElementsByTagName("ECUC-INTEGER-PARAM-DEF");
        processParameters(integerParams, idx, "INTEGER",c);

        NodeList floatParams = params.getElementsByTagName("ECUC-FLOAT-PARAM-DEF");
        processParameters(floatParams, idx, "FLOAT",c);

        NodeList booleanParams = params.getElementsByTagName("ECUC-BOOLEAN-PARAM-DEF");
        processParameters(booleanParams, idx, "BOOLEAN",c);

        NodeList enumerationParams = params.getElementsByTagName("ECUC-ENUMERATION-PARAM-DEF");
        processParameters(enumerationParams, idx, "ENUMERATION",c);
    }

    private static void processParameters(NodeList paramNodes, int idx, String type,ContainerItem c) {
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Element paramNode = (Element) paramNodes.item(i);
            ParameterItem p = processParameter(paramNode, type);
            c.parametersList.add(p);
        }
    }

    private static ParameterItem processParameter(Element ecucParameter, String typ) {
        String name = ecucParameter.getElementsByTagName("SHORT-NAME").item(0).getTextContent();
        String UUID = "";
        String Desc = ecucParameter.getElementsByTagName("DESC").item(0).getTextContent();
        
        Element defaultValueElement = (Element) ecucParameter.getElementsByTagName("DEFAULT-VALUE").item(0);
        boolean hasDefaultValue = (defaultValueElement != null) ? true : false;
        
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

        if (typ.equals("INTEGER")) {
            int defVal = -1;
            String value;
            if (hasDefaultValue) {
                value = defaultValueElement.getTextContent();
                defVal = Integer.parseInt(value);
            }
            return new IntegerParameter(name, UUID, "", Desc, LM, UM, hasDefaultValue, defVal, new Range(startRange, endRange));
        }
        else if (typ.equals("FLOAT")) {
            float defVal = -1;
            String value;
            if (hasDefaultValue) {
                value = defaultValueElement.getTextContent();
                defVal = Float.parseFloat(value);
            }
            return new FloatParameter(name, UUID, "", Desc, LM, UM, hasDefaultValue, defVal, new Range(startRange, endRange));
        }
        else if (typ.equals("BOOLEAN")) {
            boolean defVal = false;
            String value;
            if (hasDefaultValue) {
                value = defaultValueElement.getTextContent();
                defVal = (value == "false") ? false : true;
            }
            return new BooleanParameter(name, UUID, "", Desc, LM, UM, hasDefaultValue, defVal);
        }
        else { // enum 
            return new EnumParameter(name, UUID, "", Desc, LM, UM);
        }
    }
    
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
                            IntegerParameter intParam = (IntegerParameter) param;
                            paramName.setText(paramName.getText() + ": Integer");
                        }
                        
                        else if (param instanceof FloatParameter) {
                            FloatParameter floatParam = (FloatParameter) param;
                            paramName.setText(paramName.getText() + ": Float");
                        }
                        
                        else if (param instanceof BooleanParameter) {
                            BooleanParameter boolParam = (BooleanParameter) param;
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
                            EnumParameter enumParam = (EnumParameter) param;
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

                        if (param instanceof IntegerParameter) {
                            IntegerParameter intParam = (IntegerParameter) param;
                            if (intParam.hasDefaultValue()){
                                textArea2.setText(textArea2.getText() + intParam.getDefaultValue());
                            }
                            else {
                                textArea2.setText(textArea2.getText() + "None");
                            }
                            textArea2.setText(textArea2.getText() + "\n");
                            textArea2.setText(textArea2.getText() + "Range: " + (int)intParam.getRange().getMin() + " --> " + (int)intParam.getRange().getMax());
                        }
                        
                        else if (param instanceof FloatParameter) {
                            FloatParameter floatParam = (FloatParameter) param;
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
                        
                        else if (param instanceof EnumParameter) {
                            EnumParameter enumParam = (EnumParameter) param;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
