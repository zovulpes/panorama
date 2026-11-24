package panorama.kt3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {
    private final int orderId;
    private final Client client;
    private final LocalDateTime requestDate;
    private final String shootingLocation, shootingPurpose, videoFormat, requirements;
    private final LocalDateTime requestedDate;
    private final int durationMinutes;
    private LocalDateTime plannedStart, plannedEnd, deadline;
    private BigDecimal estimatedPrice;
    private String status = "новый";
    private final List<EmployeeAssignment> assignments = new ArrayList<>();
    private final List<EquipmentReservation> reservations = new ArrayList<>();
    private final List<OrderStage> stages = new ArrayList<>();
    private final List<VideoMaterial> materials = new ArrayList<>();

    public Order(int orderId, Client client, String location, String purpose,
                 LocalDateTime requestedDate, String videoFormat, int durationMinutes, String requirements) {
        this.orderId = orderId; this.client = client;
        this.requestDate = LocalDateTime.now();
        this.shootingLocation = location; this.shootingPurpose = purpose;
        this.requestedDate = requestedDate; this.videoFormat = videoFormat;
        this.durationMinutes = durationMinutes; this.requirements = requirements;
    }
    public int getOrderId() { return orderId; }
    public Client getClient() { return client; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public String getShootingLocation() { return shootingLocation; }
    public String getShootingPurpose() { return shootingPurpose; }
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public String getVideoFormat() { return videoFormat; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getRequirements() { return requirements; }
    public LocalDateTime getPlannedStart() { return plannedStart; }
    public LocalDateTime getPlannedEnd() { return plannedEnd; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public LocalDateTime getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public List<EmployeeAssignment> getAssignments() { return Collections.unmodifiableList(assignments); }
    public List<EquipmentReservation> getReservations() { return Collections.unmodifiableList(reservations); }
    public List<OrderStage> getStages() { return Collections.unmodifiableList(stages); }
    public List<VideoMaterial> getMaterials() { return Collections.unmodifiableList(materials); }
    public void setEstimate(BigDecimal price, LocalDateTime deadline) { this.estimatedPrice = price; this.deadline = deadline; }
    public void plan(LocalDateTime start, LocalDateTime end) { this.plannedStart = start; this.plannedEnd = end; }
    public void setStatus(String status) { this.status = status; }
    public void addAssignment(EmployeeAssignment item) { assignments.add(item); }
    public void addReservation(EquipmentReservation item) { reservations.add(item); }
    public void addStage(OrderStage item) { stages.add(item); }
    public void addMaterial(VideoMaterial item) { materials.add(item); }
}
