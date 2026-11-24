package panorama.kt3;

import java.time.LocalDateTime;
public final class EmployeeAssignment {
    private final int assignmentId;
    private final Order order;
    private final Employee employee;
    private final String taskRole;
    private final LocalDateTime startAt, endAt;
    public EmployeeAssignment(int id, Order order, Employee employee, LocalDateTime startAt, LocalDateTime endAt) {
        this.assignmentId = id; this.order = order; this.employee = employee;
        this.taskRole = employee.getRole(); this.startAt = startAt; this.endAt = endAt;
    }
    public int getAssignmentId() { return assignmentId; }
    public Order getOrder() { return order; }
    public Employee getEmployee() { return employee; }
    public String getTaskRole() { return taskRole; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
}
