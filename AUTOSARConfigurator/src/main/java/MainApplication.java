
//import static data_structures.containerDef;
//import static data_structures.containerParameters;
//import static data_structures.par;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;



public class MainApplication extends javax.swing.JFrame {

    /**
     * Creates new form MainApplication
     */
    // Member variables
    DefaultTreeModel modulesTree;
    DefaultMutableTreeNode canNM_root_node = new DefaultMutableTreeNode("CanNM");
    static final int maxNodes = 100000;
    static List<ContainerItem> containerDef = new ArrayList<>();
    static int[] par = new int[maxNodes];
    static List<ParameterItem>[] containerParameters = new ArrayList[maxNodes];

    static {
        for (int i = 0; i < maxNodes; i++) {
            containerParameters[i] = new ArrayList<>();
        }
    }
    
    public MainApplication() {
        initComponents();
        SidebarTreeConstruction();
    }
    
    private void SidebarTreeConstruction(){
        String filePath = "/home/mira/Thesis/AI-based-configuration-and-IDE-Java-based-tool-for-AUTOSAR-projects/AUTOSARConfigurator/src/main/java/CanNM_BSWMD.arxml";
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(new File(filePath));

            Element root = doc.getDocumentElement();
            Element containers = (Element) root.getElementsByTagName("CONTAINERS").item(0);

            dfs(containers, 0, -1);
            
        } catch (IOException | ParserConfigurationException | SAXException e) {
        }
        
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
        canNM_root_node.add(containerDef.get(0).getGUINode()); 
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


                NodeList subContainersNodes = containerNode.getChildNodes();
                for (int k = 0; k < subContainersNodes.getLength(); k++) {
                    Node subContainerNode = subContainersNodes.item(k);
                    if (subContainerNode.getNodeType() == Node.ELEMENT_NODE && subContainerNode.getNodeName().equals("SUB-CONTAINERS")) {
                        dfs(subContainerNode, level + 1, idx);
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
        if (typ.equals("INTEGER")) {
             startRange = ecucParameter.getElementsByTagName("MIN").item(0).getTextContent();
             endRange = ecucParameter.getElementsByTagName("MAX").item(0).getTextContent();
        }
        else if (typ.equals("FLOAT")) {
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

//        System.out.println(defName + ", " + value + ", " + dataType + ", " + declName + ", " + isDefault + ", " +
//                startRange + ", " + endRange + ", " + LM + ", " + UM);

        return new ParameterItem(defName, value, dataType, declName, isDefault, startRange, endRange, LM, UM);
    }
    
    public void PrintingTree(){
        // Printing children
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setFont(new java.awt.Font("Chilanka", 1, 24)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Chilanka", 1, 24)); // NOI18N
        jLabel1.setText("Configurator");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(jTree1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(629, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(373, 373, 373))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
    
    // Member Variables
//    DefaultTreeModel modulesTree;
//    static final int maxNodes = 100000;
//    static List<ContainerItem> containerDef = new ArrayList<>();
//    static int[] par = new int[maxNodes];
//    static List<ParameterItem>[] containerParameters = new ArrayList[maxNodes];
//
//    static {
//        for (int i = 0; i < maxNodes; i++) {
//            containerParameters[i] = new ArrayList<>();
//        }
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
