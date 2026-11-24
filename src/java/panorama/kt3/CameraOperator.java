package panorama.kt3;

public final class CameraOperator extends Employee {
    public CameraOperator(int id, String name, String phone) { super(id, name, "оператор", phone); }
    @Override public String describeWork() { return "Подготовка оборудования и панорамная видеосъёмка"; }
}
