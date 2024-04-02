abstract class ParameterItem {
    protected String name;
    protected String UUID;
    protected String parentContainer;
    protected String description;
    protected int lowerMultiplicity;
    protected int upperMultiplicity;
    protected boolean hasDefaultValue;
    protected ContainerItem container;
    public ParameterItem (String name, String UUID, String parentContainer, String description, int lowerMultiplicity, int upperMultiplicity) {
        this.name = name;
        this.UUID = UUID;
        this.parentContainer = parentContainer;
        this.description = description;
        this.lowerMultiplicity = lowerMultiplicity;
        this.upperMultiplicity = upperMultiplicity;
        this.container = container;
    }

    public String getName() {
        return name;
    }
    
    public String getDesc(){
        return description;
    }

}

class BooleanParameter extends ParameterItem {
    private boolean value;
    private boolean defaultValue;

    public BooleanParameter(String name, String UUID, String parentContainer, String description,
                            int lowerMultiplicity, int upperMultiplicity, boolean hasDefaultValue, boolean defaultValue) {
        
        super(name, UUID, parentContainer, description, lowerMultiplicity, upperMultiplicity);
        this.hasDefaultValue = hasDefaultValue;
        this.defaultValue = defaultValue;
    }

    // Getter for value
    public boolean getValue() {
        return value;
    }

    // Setter for value
    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean hasDefaultValue () {
        return hasDefaultValue;
    }
    
    public boolean getDefaultValue () {
        return defaultValue;
    }
}

class FloatParameter extends ParameterItem {
    private float value;
    private float defaultValue;
    private Range range;

    public FloatParameter(String name, String UUID, String parentContainer, String description, int lowerMultiplicity, int upperMultiplicity, boolean hasDefaultValue, float defaultValue, Range range) 
{
        super(name, UUID, parentContainer, description, lowerMultiplicity, upperMultiplicity);
        this.hasDefaultValue = hasDefaultValue;
        this.defaultValue = defaultValue;
        this.range = range;
    }

    // Getter for value
    public float getValue() {
        return value;
    }

    // Setter for value
    public void setValue(float value) {
        this.value = value;
    }

    public boolean hasDefaultValue () {
        return hasDefaultValue;
    }
    
    public float getDefaultValue () {
        return defaultValue;
    }
    
    public Range getRange(){
        return range;
    }
}

class IntegerParameter extends ParameterItem {
    private int value;
    private int defaultValue;
    private Range range;

    public IntegerParameter(String name, String UUID, String parentContainer, String description, int lowerMultiplicity, int upperMultiplicity, boolean hasDefaultValue, int defaultValue, Range range) {
        super(name, UUID, parentContainer, description, lowerMultiplicity, upperMultiplicity);
        this.hasDefaultValue = hasDefaultValue;
        this.defaultValue = defaultValue;
        this.range = range;
    }

    // Getter for value
    public int getValue() {
        return value;
    }

    // Setter for value
    public void setValue(int value) {
        this.value = value;
    }

    public boolean hasDefaultValue () {
        return hasDefaultValue;
    }

    public int getDefaultValue () {
        return defaultValue;
    }
    
    public Range getRange(){
        return range;
    }
}

class EnumParameter extends ParameterItem {
    private EnumValue value;

    public EnumParameter(String name, String UUID, String parentContainer, String description, int lowerMultiplicity, int upperMultiplicity, EnumValue value) {
        super(name, UUID, parentContainer, description, lowerMultiplicity, upperMultiplicity);
        this.value= value;
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
