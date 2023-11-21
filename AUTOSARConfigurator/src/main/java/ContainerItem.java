
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

    //root:
    //getName: TreeNode
    //loop over children:
    //
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
    
    public void setChild(ContainerItem subContainer){
        guiNode.add(new DefaultMutableTreeNode(subContainer.name));
    }
    public String getName(){
        return this.name;
    }
}