
import javax.swing.tree.DefaultMutableTreeNode;

public class ContainerItem {
    String name;
    String UUID;
    String lowerMult;
    String upperMult;
    DefaultMutableTreeNode guiNode;
    // parent idx
    // list of children idx
    // current index
    // level

    public ContainerItem(String name, String UUID, String lowerMult, String upperMult) {
        this.name = name;
        this.UUID = UUID;
        this.lowerMult = lowerMult;
        this.upperMult = upperMult;
        this.guiNode = new DefaultMutableTreeNode(name);
    }
    
    public DefaultMutableTreeNode getGUINode(){
        return this.guiNode;
    }
    
    public void addChild(DefaultMutableTreeNode subContainerNode){
        this.guiNode.add(subContainerNode);
    }
    
    public void setGUINode(DefaultMutableTreeNode treeNode){
        this.guiNode = treeNode;
    }
   
    public String getName(){
        return this.name;
    }
}