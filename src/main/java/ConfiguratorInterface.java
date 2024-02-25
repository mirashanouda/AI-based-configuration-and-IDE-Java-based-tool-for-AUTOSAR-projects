import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface ConfiguratorInterface {
    void DSWMDConstructor();
    void ARXMLConstructor();
    Element FileReader(String filePath);
    void BSWMDParserDFS(Node ecucContainer, int parentIndex);
    void ARXMLParserDFS(Node ecucContainer, int parentIndex);
    ContainerItem processBSWMDContainer(Element ecucContainer);
    void PrintingContainersTree(List<ContainerItem> containerDef);
    void PrintingParameters(ContainerItem container);
    void GetBSWMDParameters(Element params, int idx, ContainerItem c);
    void GetARXMLParameters(Element params, int idx, ContainerItem c);
    void processParameters(NodeList paramNodes, String type,ContainerItem c);
    ParameterItem processParameter(Element ecucParameter, String typ);
    Boolean ValidateTreeStructure(Element arxmlRoot);
    Boolean ValidateContainersParams(Element containerRoot, int index);
    void appendLogMessage(String message);
    void setLogMessage(String message);
}