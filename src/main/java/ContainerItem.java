import java.util.ArrayList;
import java.util.List;


import javax.swing.tree.DefaultMutableTreeNode;

public class ContainerItem {
    String name;
    String UUID;
    String lowerMult;
    String upperMult;
    List<ParameterItem> parametersList;

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
        this.parametersList = new ArrayList();
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
    
    public String getUUID(){
        return this.UUID;
    }
    
    public List<ParameterItem> getParametersList () {
        return parametersList;
    }


}