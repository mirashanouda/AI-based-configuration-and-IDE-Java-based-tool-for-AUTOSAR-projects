import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
enum Variant {
    VARIANT_PRE_COMPILE,
    VARIANT_LINK_TIME,
    VARIANT_POST_BUILD
}

enum Container {
    PRE_COMPILE_TIME,
    LINK_TIME,
    POST_BUILD_TIME
}

enum Scope {
    ECU,
    LOCAL
}
*/

abstract class Parameter {
    protected String name;
    protected String UUID;
    protected String parentContainer;
    protected String description;
    protected int minMultiplicity;
    protected int maxMultiplicity;
/*
    protected boolean postBuildVariantValue;
    protected boolean postBuildVariantMultiplicity;
    protected Map<Container, List<Variant>> valueConfigurationClass;
    protected Map<Container, List<Variant>> MultiplicityConfigurationClass;
    protected Scope scope;
    protected DefaultMutableTreeNode guiNode;
*/

    // Constructors, getters, and setters...

    // Abstract method for updating GUI node
    // protected abstract void updateGUINode();
}

class BooleanParameter extends Parameter {
    private boolean value;
    private boolean defaultValue;

    public BooleanParameter(String name, String UUID, String parentContainer, String description,
                            int minMultiplicity, int maxMultiplicity, /*boolean postBuildVariantValue,
                            boolean postBuildVariantMultiplicity, Map<Container, List<Variant>> valueConfigurationClass,
                            Scope scope, DefaultMutableTreeNode guiNode,*/ boolean value, boolean defaultValue) {
        this.name = name;
        this.UUID = UUID;
        this.parentContainer = parentContainer;
        this.description = description;
        this.minMultiplicity = minMultiplicity;
        this.maxMultiplicity = maxMultiplicity;
        /*
        this.postBuildVariantValue = postBuildVariantValue;
        this.postBuildVariantMultiplicity = postBuildVariantMultiplicity;
        this.valueConfigurationClass = valueConfigurationClass;
        this.scope = scope;
        this.guiNode = guiNode;
        */
        this.value = value;
        this.defaultValue = defaultValue;
        // updateGUINode();
    }

    // Getter for value
    public bool getValue() {
        return value;
    }

    // Setter for value
    public void setValue(bool value) {
        this.value = value;
    }
}

class FloatParameter extends Parameter {
    private float value;
    private float defaultValue;
    private Range range;

    public FloatParameter(String name, String UUID, String parentContainer, String description,
                          int minMultiplicity, int maxMultiplicity,/* boolean postBuildVariantValue,
                          boolean postBuildVariantMultiplicity, Map<Container, List<Variant>> valueConfigurationClass,
                          Scope scope, DefaultMutableTreeNode guiNode, */ float value, float defaultValue, Range range) {
        this.name = name;
        this.UUID = UUID;
        this.parentContainer = parentContainer;
        this.description = description;
        this.minMultiplicity = minMultiplicity;
        this.maxMultiplicity = maxMultiplicity;
        /*
        this.postBuildVariantValue = postBuildVariantValue;
        this.postBuildVariantMultiplicity = postBuildVariantMultiplicity;
        this.valueConfigurationClass = valueConfigurationClass;
        this.scope = scope;
        this.guiNode = guiNode;
        */
        this.value = value;
        this.defaultValue = defaultValue;
        this.range = range;
        // updateGUINode();
    }

    // Getter for value
    public float getValue() {
        return value;
    }

    // Setter for value
    public void setValue(float value) {
        this.value = value;
    }
}

class IntegerParameter extends Parameter {
    private int value;
    private int defaultValue;
    private Range range;

    public IntegerParameter(String name, String UUID, String parentContainer, String description,
                            int minMultiplicity, int maxMultiplicity, /*boolean postBuildVariantValue,
                            boolean postBuildVariantMultiplicity, Map<Container, List<Variant>> valueConfigurationClass,
                            Scope scope, DefaultMutableTreeNode guiNode,*/ int value, int defaultValue, Range range) {
        this.name = name;
        this.UUID = UUID;
        this.parentContainer = parentContainer;
        this.description = description;
        this.minMultiplicity = minMultiplicity;
        this.maxMultiplicity = maxMultiplicity;
        /*
        this.postBuildVariantValue = postBuildVariantValue;
        this.postBuildVariantMultiplicity = postBuildVariantMultiplicity;
        this.valueConfigurationClass = valueConfigurationClass;
        this.scope = scope;
        this.guiNode = guiNode;
        */
        this.value = value;
        this.defaultValue = defaultValue;
        this.range = range;
        // updateGUINode();
    }

    // Getter for value
    public int getValue() {
        return value;
    }

    // Setter for value
    public void setValue(int value) {
        this.value = value;
    }
}

class EnumParameter extends Parameter {
    private EnumValue value;

    public EnumParameter(String name, String UUID, String parentContainer, String description,
                         int minMultiplicity, int maxMultiplicity, /* boolean postBuildVariantValue,
                         boolean postBuildVariantMultiplicity, Map<Container, List<Variant>> valueConfigurationClass,
                         Scope scope, DefaultMutableTreeNode guiNode, */ EnumValue value) {
        this.name = name;
        this.UUID = UUID;
        this.parentContainer = parentContainer;
        this.description = description;
        this.minMultiplicity = minMultiplicity;
        this.maxMultiplicity = maxMultiplicity;
        /*
        this.postBuildVariantValue = postBuildVariantValue;
        this.postBuildVariantMultiplicity = postBuildVariantMultiplicity;
        this.valueConfigurationClass = valueConfigurationClass;
        this.scope = scope;
        this.guiNode = guiNode;
        */
        this.value = value;
        // updateGUINode();
    }

    // Getter for value
    public EnumValue getValue() {
        return value;
    }

    // Setter for value
    public void setValue(EnumValue value) {
        this.value = value;
    }
}

class Range {
    private float min;
    private float max;

    public Range(float min, float max) {
        this.min = min;
        this.max = max;
    }

    // Getter for min
    public float getMin() {
        return min;
    }

    // Setter for min
    public void setMin(float min) {
        this.min = min;
    }

    // Getter for max
    public float getMax() {
        return max;
    }

    // Setter for max
    public void setMax(float max) {
        this.max = max;
    }
}

enum EnumValue {
    CANNM_PDU_BYTE_0,
    CANNM_PDU_BYTE_1,
    CANNM_PDU_OFF
}
