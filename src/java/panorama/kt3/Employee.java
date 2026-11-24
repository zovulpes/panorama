package panorama.kt3;

public abstract class Employee {
    private final int employeeId;
    private final String fullName, role, phone;
    protected Employee(int employeeId, String fullName, String role, String phone) {
        this.employeeId = employeeId; this.fullName = fullName; this.role = role; this.phone = phone;
    }
    public int getEmployeeId() { return employeeId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public abstract String describeWork();
}
