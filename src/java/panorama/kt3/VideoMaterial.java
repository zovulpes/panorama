package panorama.kt3;

import java.time.LocalDateTime;
public final class VideoMaterial {
    private final int materialId;
    private final Order order;
    private final String materialType, storagePath, qualityStatus;
    private final LocalDateTime createdAt;
    public VideoMaterial(int id, Order order, String type, String path, LocalDateTime createdAt, String qualityStatus) {
        this.materialId = id; this.order = order; this.materialType = type;
        this.storagePath = path; this.createdAt = createdAt; this.qualityStatus = qualityStatus;
    }
    public int getMaterialId() { return materialId; }
    public Order getOrder() { return order; }
    public String getMaterialType() { return materialType; }
    public String getStoragePath() { return storagePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getQualityStatus() { return qualityStatus; }
}
