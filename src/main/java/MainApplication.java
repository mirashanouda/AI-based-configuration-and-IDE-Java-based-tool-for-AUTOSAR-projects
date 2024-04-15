import java.awt.BorderLayout;
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

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import javafx.util.Pair; // Import Pair class
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;



public class MainApplication extends JFrame implements ConfiguratorInterface {

    // Member variables
    DefaultTreeModel BSWMDTree;
    DefaultTreeModel ARXMLTree;
    static final int maxNodes = 100000;
    private Map<String, String> errorMessages = new HashMap<>();

    static List<ContainerItem> BSWMDContainers = new ArrayList<>();
    static List<ContainerItem> ARXMLContainers = new ArrayList<>();
    Map<String, ArrayList<ContainerItem>> containers_children = new LinkedHashMap<>();
    static List<String>children_with_parents =  new ArrayList<>();
    static List<ContainerItem>ARXML_containers_with_no_parent =  new ArrayList<>();
    HashMap<Node, Node> direct_parent = new HashMap<>();
    static int[] BSWMDpar = new int[maxNodes];
    static int[] ARXMLpar = new int[maxNodes];
    HashMap<String,Pair<String, String>> containers_names_BSWMD = new HashMap<>();
    HashMap<String, String> containers_names_ARXML = new HashMap<>();
    HashMap<Pair<ParameterItem,String>, Pair<String, String>> parameters_names_BSWMD = new HashMap<>();
    HashMap<String, Pair<ParameterItem,String>> parameters_names_ARXML = new HashMap<>();
   String FilePath="output.arxml";
  

    // Method to validate XML (Part 1 of validating)
    private void validateXMLFile(String filePath) {
        appendLogMessage("Compiling input ARXML File...");
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
        ARXMLConstructor(false,"");
        
//        generateArxml(FilePath);
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
    public void ARXMLConstructor(boolean whichPath, String arxmlPath)
    {
        // Try-Catch to validate if XML is well-formed
        // try {
            String path = whichPath ? arxmlPath: "src/main/java/CanNm_Template.arxml" ;
            System.out.println(path);
            validateXMLFile(path);
            Element root = FileReader(path);
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
            // TODO: uncomment the exception and solve the bug
        //  } catch (Exception e) {
        //      appendLogMessage("An error occurred and the XML cannot be processed: " + e.getMessage());
        //      return;
        //  }

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
                String value = containers_names_ARXML.get(containerName);
                if (value != null) {
                    containers_names_ARXML.put(containerName, String.valueOf(Integer.parseInt(value) + 1));
                } else {
                     containers_names_ARXML.put(containerName, "1");
                }

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
        Pair<String, String> pair = new Pair<>((LM), (UM));
        containers_names_BSWMD.put(name, pair);

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
                    Pair<ParameterItem,String>pair = parameters_names_ARXML.get(name);
                    
                    if (pair != null) {
                        String value = parameters_names_ARXML.get(name).getRight();
                         parameters_names_ARXML.put(p.name,new Pair<>(p, String.valueOf(Integer.parseInt(value) + 1)));
                    } else {
                         parameters_names_ARXML.put(p.name,new Pair<>(p, "1"));
                    }
                    c.parametersList.add(p);
                    p.container = c;

                }
                if ("ECUC-FLOAT-PARAM-DEF".equals(destAttribute)) {
                    String name = definitionRefElement.getTextContent();
                    String[] pathParts = name.split("/");
                    name = pathParts[pathParts.length - 1];
                    ParameterItem p = new FloatParameter( name,"", "", "", 0,0, true, Float.parseFloat(val), null);
                    Pair<ParameterItem,String>pair = parameters_names_ARXML.get(name);
                    
                    if (pair != null) {
                        String value = parameters_names_ARXML.get(name).getRight();
                         parameters_names_ARXML.put(p.name,new Pair<>(p, String.valueOf(Float.parseFloat(value) + 1)));
                    } else {
                         parameters_names_ARXML.put(p.name,new Pair<>(p, "1"));
                    }
                    c.parametersList.add(p);
                    p.container = c;
                }
                if ("ECUC-BOOLEAN-PARAM-DEF".equals(destAttribute)) {
                    String name = definitionRefElement.getTextContent();
                    String[] pathParts = name.split("/");
                    name = pathParts[pathParts.length - 1];
                    ParameterItem p = new BooleanParameter(name, "", "", "", 0, 0, true, Boolean.parseBoolean(val));
                    Pair<ParameterItem,String>pair = parameters_names_ARXML.get(name);
                    
                    if (pair != null) {
                        String value = parameters_names_ARXML.get(name).getRight();
                         parameters_names_ARXML.put(p.name,new Pair<>(p, String.valueOf(Integer.parseInt(value) + 1)));
                    } else {
                         parameters_names_ARXML.put(p.name,new Pair<>(p, "1"));
                    }
                    c.parametersList.add(p);
                    p.container = c;
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
                   Pair<ParameterItem,String>pair = parameters_names_ARXML.get(name);
                    
                    if (pair != null) {
                        String value = parameters_names_ARXML.get(name).getRight();
                         parameters_names_ARXML.put(p.name,new Pair<>(p, String.valueOf(Integer.parseInt(value) + 1)));
                    } else {
                         parameters_names_ARXML.put(p.name,new Pair<>(p, "1"));
                    }
                    c.parametersList.add(p);
                    p.container = c;
                }
            }
        }
    }
    
    /**
     *
     * @param paramNodes
     * @param type
     * @param c
     */

    @Override
    public void processParameters(NodeList paramNodes, String type,ContainerItem c) {
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Element paramNode = (Element) paramNodes.item(i);
            ParameterItem p = processParameter(paramNode, type,c);
            p.container = c;
            Pair<String, String> pair = new Pair<>(String.valueOf(p.lowerMultiplicity), String.valueOf(p.upperMultiplicity));
            parameters_names_BSWMD.put(new Pair<>(p, p.name), pair);

            c.parametersList.add(p);
        }
    }

    Map<String,Pair<String,String>>chk_values_map = new HashMap<>(); // for checking the correct values of parameters

    /**
     *
     * @param ecucParameter
     * @param typ
     * @param c
     * @return
     */
    public ParameterItem processParameter(Element ecucParameter, String typ, ContainerItem c) {
    
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
     public static void printHashmap_BSWMD(HashMap<ParameterItem, Pair<String, String>> map) {
        System.out.print("Contents of BSWMD hasmap");
        for (Map.Entry<ParameterItem, Pair<String, String>> entry : map.entrySet()) {
            String key = entry.getKey().name;
            Pair<String, String> value = entry.getValue();
            System.out.println("Key: " + key + ", Value: (" + value.getLeft() + ", " + value.getRight() + ")");
        }
    }

    // Function to print the contents of a HashMap<String, Integer>
    public static void printHashmap_ARXML(HashMap<ParameterItem, String> map) {
        for (Map.Entry<ParameterItem, String> entry : map.entrySet()) {
            String key = entry.getKey().name;
            String value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jButton4 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
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
        jButton1 = new javax.swing.JButton();
        logPanel = new javax.swing.JPanel();
        titlePanel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logMessagesTextArea = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        jDialog1.setTitle("English Text Description");
        jDialog1.setAlwaysOnTop(true);

        jButton4.setBackground(new java.awt.Color(255, 255, 204));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setText("Done!");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("  Enter Your English Description for your ARXML File:");

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(10);
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

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
                    .addComponent(BComponentName, javax.swing.GroupLayout.PREFERRED_SIZE, 755, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BParamsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 1075, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(BjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(BjPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BTreeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(1098, Short.MAX_VALUE)))
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

        jButton1.setBackground(new java.awt.Color(153, 255, 153));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setText("Extract ARXML");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        titlePanel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        titlePanel.setText("Log Messages");

        logMessagesTextArea.setEditable(false);
        logMessagesTextArea.setBackground(new java.awt.Color(255, 255, 255));
        logMessagesTextArea.setColumns(20);
        logMessagesTextArea.setRows(5);
        jScrollPane1.setViewportView(logMessagesTextArea);

        javax.swing.GroupLayout logPanelLayout = new javax.swing.GroupLayout(logPanel);
        logPanel.setLayout(logPanelLayout);
        logPanelLayout.setHorizontalGroup(
            logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(logPanelLayout.createSequentialGroup()
                        .addComponent(titlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        logPanelLayout.setVerticalGroup(
            logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logPanelLayout.createSequentialGroup()
                .addComponent(titlePanel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton2.setBackground(new java.awt.Color(255, 153, 153));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setText("Generate C & H");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(255, 204, 255));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setText("AI Magic");
        jButton3.setBorder(null);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(204, 204, 204));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setText("Upload ARXML File");
        jButton5.setToolTipText("");
        jButton5.setBorder(null);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1348, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(logPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 734, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(22, 22, 22))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2))))
                    .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("BSWMD");

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
    Map<String, String> parameters_val_update = new HashMap<>(); // map: paramter name and key
    public void print_paramters_val_update_map(Map<String ,String> parameters_val_update){
        for (Map.Entry<String, String> entry : parameters_val_update.entrySet()) {
            String parameterName = entry.getKey();
            String value = entry.getValue();
            
            System.out.println("Parameter Name: " + parameterName);
            System.out.println("Updated Value: " + value);
            System.out.println();
        }
    }


    // Handling the error messages independently
    private void validateAndDisplayErrors(String containerName, String parameterName, String newValue, String min_val, String max_val) {
        BigDecimal newValuen = new BigDecimal(newValue);
        BigDecimal min_valn = new BigDecimal(min_val);
        BigDecimal max_valn = new BigDecimal(max_val);
        if(newValuen.compareTo(max_valn) <= 0 && newValuen.compareTo(min_valn) >= 0) {
            // If value is correct, remove any error for this parameter and reset background
            errorMessages.remove(containerName + "." + parameterName);
        } else {
            // If value is incorrect, update error message and set background to red
            errorMessages.put(containerName + "." + parameterName, 
            "Error in Container (" + containerName + "): Value for Parameter (" + parameterName + 
            ") is out of range [" + min_val + ", " + max_val + "].");
    }        
        updateLogMessageArea();
    }
    
    private void updateLogMessageArea() {
        // Concatenate all current error messages
        String allErrors = String.join("\n", errorMessages.values());
        // Display in log message area
        setLogMessage(allErrors.isEmpty() ? "No errors. Ready to Run..." : allErrors);
    }

    
    public Boolean compare_arxml_map_to_bswmd_map(Map<String, String> parameters_val_update, Map<String, Pair<String, String>> chk_values_map) {    
        boolean allValuesValid = true; // Tracks if all values are valid
        for (Map.Entry<String, String> entry : parameters_val_update.entrySet()) {
            String parameterName = entry.getKey();
            String value = entry.getValue();
            Pair<String, String> range = chk_values_map.get(parameterName);
    
            if (range != null) { // Ensure the parameter is found in the chk_values_map
                String min_val = range.getLeft();
                String max_val = range.getRight();
                BigDecimal valuen = new BigDecimal(value);
                BigDecimal min_valn = new BigDecimal(min_val);
                BigDecimal max_valn = new BigDecimal(max_val);
    
                // Not needed
                if (!(valuen.compareTo(min_valn) >= 0 && valuen.compareTo(max_valn) <= 0)) {
                    // If any value is not valid, update allValuesValid to false
                    //allValuesValid = false;
                    // Log or handle the invalid value
                    //System.out.println("Parameter " + parameterName + " with value " + value + " is out of range [" + min_val + ", " + max_val + "]");

                }
            }
        }
    
        // Return true if all values are valid, false otherwise
        return allValuesValid;
    }

    public void check_multiplicity_of_continers_and_parameters(){ // checking the upper and lower multiplicty of containers
        for (Map.Entry<String, Pair<String, String>> entry : containers_names_BSWMD.entrySet()) {
            String container_name = entry.getKey();
            Pair<String, String> multiplicty = entry.getValue();
            Integer upper_multiplicty; 
             
            if(entry.getValue().getRight() == "Infinity") upper_multiplicty = Integer.MAX_VALUE - 1;
            else upper_multiplicty = Integer.parseInt(entry.getValue().getRight()); 
            Integer lower_multiplicity = Integer.parseInt(entry.getValue().getLeft());
            Integer arxml_container_multiplicty;
            if(containers_names_ARXML.containsKey(container_name)){
                arxml_container_multiplicty = Integer.parseInt(containers_names_ARXML.get(container_name));
            }
            else arxml_container_multiplicty = 0;
            if(arxml_container_multiplicty < lower_multiplicity ||  arxml_container_multiplicty > upper_multiplicty){
                
                errorMessages.put(arxml_container_multiplicty.toString(), "Error: Container Multiplicty for " + container_name + " is out of range [" + lower_multiplicity + ", " + upper_multiplicty + "].");
                //return false;
            }
        }
        for (Map.Entry<Pair<ParameterItem,String>, Pair<String, String>> entry : parameters_names_BSWMD.entrySet()) {
            String parameter_name = entry.getKey().getRight();
            Pair<String, String> multiplicty = entry.getValue();
            Integer upper_multiplicty;
            
            
            if(entry.getKey().getLeft() instanceof IntegerParameter || entry.getKey().getLeft() instanceof FloatParameter){
                if(entry.getValue().getRight() == "Infinity") upper_multiplicty = Integer.MAX_VALUE-1;
                else upper_multiplicty = Integer.parseInt(entry.getValue().getRight()); 
                Integer lower_multiplicity = Integer.parseInt(entry.getValue().getLeft());
                Integer arxml_paramter_multiplicty = 0;
                System.out.println(entry.getKey());
                if(parameters_names_ARXML.containsKey(entry.getKey())){
                    System.out.println(parameter_name);
                    arxml_paramter_multiplicty = Integer.parseInt(parameters_names_ARXML.get(parameter_name).getRight());
                }
                
                if(containers_names_ARXML.containsKey(entry.getKey().getRight()) && (arxml_paramter_multiplicty < lower_multiplicity ||  arxml_paramter_multiplicty > upper_multiplicty)){
                    errorMessages.put(arxml_paramter_multiplicty.toString(), "Error: Parameter Multiplicty for " + parameter_name + " in " + entry.getKey().getRight()+" is out of range [" + lower_multiplicity + ", " + upper_multiplicty + "].");
                    //return false;
                }
            }
        }
        //return true;
    }
    public Boolean check_names_of_configurator_and_paramters(){
        for (Map.Entry<String, String> entry : containers_names_ARXML.entrySet()){
            String container_name = entry.getKey();
            if(!containers_names_BSWMD.containsKey(container_name)){
                errorMessages.put(container_name, "Error: Container Name " + container_name + " is not a container name in the basic software module definition ");
                return false;
            }
        }
        for (Map.Entry<String, Pair<ParameterItem,String>> entry : parameters_names_ARXML.entrySet()){
            String parameter_name = entry.getKey();
            if(!parameters_names_BSWMD.containsKey(parameter_name) && containers_names_BSWMD.containsKey(entry.getKey())){
                errorMessages.put(parameter_name, "Error: Parameter Name " + parameter_name + " is not a parameter name in the basic software module definition ");
                return false;
            }
        }
         return true;   
    }

    public ParameterItem processParameter(Element ecucParameter, String typ) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }



    private class CustomActionListener implements ActionListener { // for chnging values in arxml 
        private int index;
        private ContainerItem c;
        private String parameterName;
        public CustomActionListener(int index, ContainerItem c, String parameterName) {
            this.index = index;
            this.c = c;
            this.parameterName = parameterName;
        }

       

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            String newValue = null;

            // Reading the entered value
            if (source instanceof JTextField) {
                JTextField textField = (JTextField) source;
                newValue = textField.getText();
            } else if (source instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) source;
                String selectedValue = (String) comboBox.getSelectedItem();
                
                // Determine if the parameter is boolean or enum based on the items in the JComboBox
                if (comboBox.getItemCount() > 0 && ("True".equals(comboBox.getItemAt(0)) || "False".equals(comboBox.getItemAt(0)) || "Not Set".equals(comboBox.getItemAt(0)))) {
                    // Handle boolean parameters, including "Not Set" as a possible value.
                    newValue = "True".equals(selectedValue) ? "True" : "False".equals(selectedValue) ? "False" : "Not Set"; 
                } else {
                    // Handle enum parameters.
                    newValue = selectedValue; // Directly use the enum value as the new value.
                }
            }

            if (newValue != null) {
                // Here, we update the parameters with the new values
                // WHETHER IT'S RIGHT OR WRONG. DOESN'T MATTER FOR NOW
                // Cause when generating ARXML, we check over the error messages first
                parameters_val_update.put(parameterName, newValue);
                ParameterItem parameterToUpdate = c.parametersList.get(index);
                if (parameterToUpdate instanceof IntegerParameter) {
                    ((IntegerParameter) parameterToUpdate).setValue(Integer.parseInt(newValue));
                } else if (parameterToUpdate instanceof FloatParameter) {
                    ((FloatParameter) parameterToUpdate).setValue(Float.parseFloat(newValue));
                } else if (parameterToUpdate instanceof BooleanParameter) {
                    ((BooleanParameter) parameterToUpdate).setValue(Boolean.parseBoolean(newValue));
                } else if (parameterToUpdate instanceof EnumParameter) {
                    EnumValue enumValue = Enum.valueOf(EnumValue.class, newValue);
                    ((EnumParameter) parameterToUpdate).setValue(enumValue);                                
                }

                            

                // Validation and feedback logic for JTextField inputs.
                if (source instanceof JTextField) {
                    validateTextFieldAndUpdateUI((JTextField) source, parameterName, newValue);
                } else if (source instanceof JComboBox) {
                    // For JComboBox, you might want to add additional validation or feedback logic here.
                    // Note: Since enum and boolean values are straightforward, extensive validation might not be necessary,
                    // but you can implement any specific logic as needed.
                }

            }
        }

        
        
        private void validateTextFieldAndUpdateUI(JTextField textField, String parameterName, String newValue) {
            Pair<String, String> pair = chk_values_map.get(parameterName);
            if (pair != null) {
                String min_val = pair.getLeft();
                String max_val = pair.getRight();
                try {
                    BigDecimal newValuen = new BigDecimal(newValue);
                    BigDecimal min_valn = new BigDecimal(min_val);
                    BigDecimal max_valn = new BigDecimal(max_val);

                    if (newValuen.compareTo(min_valn) >= 0 && newValuen.compareTo(max_valn) <= 0) {
                        textField.setBackground(Color.WHITE); // Value within range.
                    } else {
                        textField.setBackground(Color.RED); // Value out of range.
                    }
                    validateAndDisplayErrors(c.name, parameterName, newValue, min_val, max_val);
                } catch (NumberFormatException nfe) {
                    textField.setBackground(Color.RED); // Invalid format.
                    errorMessages.put(c.name + "." + parameterName, "Invalid format for " + parameterName);
                    updateLogMessageArea();
                }
            }
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
                    
                        JPanel targetPanel = (j % 2 == 0) ? innerPanel : innerPanel2;
                        gbc.gridx = 0; // Column for labels
                        targetPanel.add(paramLabel, gbc);
                    
                        if (param instanceof IntegerParameter || param instanceof FloatParameter) {
                            JTextField textField = new JTextField();
                            textField.setPreferredSize(new Dimension(100, 30));
                    
                            // Fetch the updated value if it exists; otherwise, use the default value
                            String value = parameters_val_update.getOrDefault(param.getName(), 
                            (param instanceof IntegerParameter) ? String.valueOf(((IntegerParameter) param).getDefaultValue()) :
                            (param instanceof FloatParameter) ? String.valueOf(((FloatParameter) param).getDefaultValue()) : "");
                        
                            textField.setText(value);
                                         
                            textField.addActionListener(new CustomActionListener(j ,c, param.getName()));
                            gbc.gridx = 1; // Column for text fields
                            targetPanel.add(textField, gbc);

                        } else if (param instanceof BooleanParameter) {

                            BooleanParameter boolParam = (BooleanParameter) param;
                           
                            String[] items = {"True", "False", "Not Set"};
                            JComboBox<String> comboBox = new JComboBox<>(items);

                            // Attempt to fetch an updated value for this parameter, if available
                            String updatedValue = parameters_val_update.get(param.getName());
                            String selectedValue;

                            if (updatedValue != null) {
                                selectedValue = updatedValue;
                            } else if (boolParam.hasDefaultValue()) {
                                // Use the default value if no updated value is found
                                selectedValue = boolParam.getDefaultValue() ? "True" : "False";
                            } else {
                                selectedValue = "Not Set";

                            }
                            
                            comboBox.setSelectedItem(selectedValue);
                            comboBox.addActionListener(new CustomActionListener(j, c, param.getName()));
                            gbc.gridx = 1;
                            targetPanel.add(comboBox, gbc);
                            
                        
                        } else if (param instanceof EnumParameter) {
                            EnumParameter enumParam = (EnumParameter) param;
                            String[] items = {"CANNM_PDU_BYTE_0", "CANNM_PDU_BYTE_1", "CANNM_PDU_OFF"};
                            JComboBox<String> comboBox = new JComboBox<>(items);
                    
                            String selectedValue = parameters_val_update.getOrDefault(param.getName(), 
                            enumParam.getValue().toString());
                            
                            comboBox.setSelectedItem(selectedValue);
                            
                            comboBox.addActionListener(new CustomActionListener(j, c, param.getName()));

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

//        generateArxml(FilePath);

        
        
    }//GEN-LAST:event_jTree2MouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
    if (errorMessages.isEmpty()) {
        generateArxml(FilePath);
    } else {
        appendLogMessage("Cannot extract ARXML file before getting valid arguments/values.");
    }
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        try {
            String pythonScriptPath = "ARXMLConversion.py"; // Replace with the actual script path
            ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath);
            Process p = pb.start();

            // If you want to capture the Python script's output
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = p.waitFor();
            if (exitCode == 0) {
                System.out.println("Script executed successfully.");
                appendLogMessage("Script executed successfully.");
                appendLogMessage("C and H Files Generated From output.arxml.");
                
                
            } else {
                System.out.println("Script execution failed.");
                appendLogMessage("Script execution failed.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
    }
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
            jDialog1.pack(); // Optional: To adjust dialog size to fit its contents
            jDialog1.setLocationRelativeTo(this); // To center the dialog relative to the main frame
            jDialog1.setVisible(true); // To make the dialog visible        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
            try {
            String textFromTextArea = jTextArea1.getText();
            // Write the text to a file
            File file = new File("final_model/user_input.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(textFromTextArea);
            writer.close();
            
            String pythonScriptPath = "final_model/ai_arxml_generation.py"; // Replace with the actual script path
            ProcessBuilder builder = new ProcessBuilder("/home/gadmin/miniconda3/bin/python3", pythonScriptPath);
            Process p = builder.start();
            
            InputStream stdout = p.getInputStream();
            InputStream stderr = p.getErrorStream();

            // Create buffered readers to read the output and errors
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(stdout));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(stderr));

            // Read and display the output from stdout
            System.out.println("Output from the Python script:");
            String line;
            while ((line = outputReader.readLine()) != null) {
                System.out.println(line);
            }

            // Read and display the errors from stderr
            System.out.println("Errors from the Python script (if any):");
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }

            // If you want to capture the Python script's output
            BufferedReader inn = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line2;
            while ((line2 = inn.readLine()) != null) {
                System.out.println(line2);
            }

            int exitCode = p.waitFor();
            if (exitCode == 0) {
                System.out.println("Script executed successfully.");
                appendLogMessage("Script executed successfully.");
                
                
                // Check the contents of error.txt
                File file_error = new File("yaml_conversion/error.txt");
                BufferedReader fileReader = new BufferedReader(new FileReader(file_error));
                String status = fileReader.readLine(); // Assumes only one of the two strings is in the file
                fileReader.close();

                if ("Success!".equals(status)) {
                    System.out.println("AI-Generated ARXML is generated Successfully in /final_model/generated_arxml.arxml");
                    appendLogMessage("AI-Generated ARXML is generated Successfully in /final_model/generated_arxml.arxml.");
                } else if ("Failure!".equals(status)) {
                    System.out.println("The user input is not valid/vague.");
                    appendLogMessage("The user input is not valid/vague.");
                }
                
                
            } else {
                System.out.println("Script execution failed.");
                appendLogMessage("Script execution failed.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
    }
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // Run the file chooser in a separate thread to ensure the UI remains responsive
        new Thread(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select an ARXML File");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ARXML Files", "arxml"));

            // Ensures the file chooser is displayed in the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                int result = fileChooser.showOpenDialog(MainApplication.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    // Run the file handling in the EDT
                    SwingUtilities.invokeLater(() -> {
                        handleSelectedFile(selectedFile);
                    });
                }
            });
        }).start();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void handleSelectedFile(File file) {
        // Implement the file processing logic here
        System.out.println("Selected file: " + file.getAbsolutePath());
        ARXMLConstructor(true,file.getAbsolutePath());
        // Here you might set the path to the file or perform other actions based on the file path
        // For example, you could start parsing the file or pass the file path to another part of your application
    }

    
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
            appendLogMessage("ARXML file has been generated at: " + filePath);

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
        Element parameterValue = doc.createElement("VALUE");
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTree jTree2;
    private javax.swing.JTextArea logMessagesTextArea;
    private javax.swing.JPanel logPanel;
    private javax.swing.JLabel titlePanel;
    // End of variables declaration//GEN-END:variables

//    @Override
//    public void ARXMLParserDFS(Node ecucContainer, int parentIndex) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
}
