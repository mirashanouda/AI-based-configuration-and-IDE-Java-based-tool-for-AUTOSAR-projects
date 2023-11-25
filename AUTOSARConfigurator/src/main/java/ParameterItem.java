public class ParameterItem {
    String defName;
    String value;
    String dataType;
    String declName;
    int isDefault;
    String startRange;
    String endRange;
    String lowerMult;
    String upperMult;

    public ParameterItem(String defName, String value, String dataType, String declName, int isDefault,
                         String startRange, String endRange, String lowerMult, String upperMult) {
        this.defName = defName;
        this.value = value;
        this.dataType = dataType;
        this.declName = declName;
        this.isDefault = isDefault;
        this.startRange = startRange;
        this.endRange = endRange;
        this.lowerMult = lowerMult;
        this.upperMult = upperMult;
    }
}