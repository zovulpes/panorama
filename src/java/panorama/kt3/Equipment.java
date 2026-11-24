package panorama.kt3;

public final class Equipment {
    private final int equipmentId;
    private final String equipmentName, equipmentType, inventoryNumber, condition;
    public Equipment(int id, String name, String type, String inventoryNumber, String condition) {
        this.equipmentId = id; this.equipmentName = name; this.equipmentType = type;
        this.inventoryNumber = inventoryNumber; this.condition = condition;
    }
    public int getEquipmentId() { return equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public String getEquipmentType() { return equipmentType; }
    public String getInventoryNumber() { return inventoryNumber; }
    public String getCondition() { return condition; }
}
