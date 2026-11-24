package panorama.kt3;

import java.time.LocalDateTime;
public final class OrderStage {
    private final int stageId;
    private final Order order;
    private final String stageName;
    private String status;
    private final LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    public OrderStage(int id, Order order, String stageName, String status, LocalDateTime startedAt,
                      LocalDateTime finishedAt) {
        this.stageId = id; this.order = order; this.stageName = stageName;
        this.status = status; this.startedAt = startedAt; this.finishedAt = finishedAt;
    }
    public int getStageId() { return stageId; }
    public Order getOrder() { return order; }
    public String getStageName() { return stageName; }
    public String getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void finish(LocalDateTime timestamp) { this.status = "завершён"; this.finishedAt = timestamp; }
}
