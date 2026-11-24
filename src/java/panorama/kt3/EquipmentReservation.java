package panorama.kt3;

import java.time.LocalDateTime;
public final class EquipmentReservation {
    private final int reservationId;
    private final Order order;
    private final Equipment equipment;
    private final LocalDateTime reservedFrom, reservedTo;
    public EquipmentReservation(int id, Order order, Equipment equipment, LocalDateTime from, LocalDateTime to) {
        this.reservationId = id; this.order = order; this.equipment = equipment;
        this.reservedFrom = from; this.reservedTo = to;
    }
    public int getReservationId() { return reservationId; }
    public Order getOrder() { return order; }
    public Equipment getEquipment() { return equipment; }
    public LocalDateTime getReservedFrom() { return reservedFrom; }
    public LocalDateTime getReservedTo() { return reservedTo; }
}
