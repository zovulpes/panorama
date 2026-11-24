package panorama.kt3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Main {
    private static BigDecimal decimal(String value) { return new BigDecimal(value); }
    public static void main(String[] args) {
        StudioService studio = new StudioService();
        CameraOperator operator = studio.addOperator("Иван Петров", "+7-900-000-00-01");
        VideoEditor editor = studio.addVideoEditor("Мария Иванова", "+7-900-000-00-02");
        Equipment camera = studio.addEquipment("Панорамная камера", "камера", "CAM-001", "исправно");
        Equipment tripod = studio.addEquipment("Штатив", "штатив", "TRI-001", "исправно");

        Order oldOrder = studio.registerRequest("Компания Сфера", "+7-900-100-00-01",
                "office@sfera.test", "Москва", "Обзор офиса",
                LocalDateTime.of(2026, 11, 5, 11, 0), "4K", 5, "Короткое видео для сайта");
        studio.planOrder(oldOrder.getOrderId(), List.of(operator.getEmployeeId()), List.of(camera.getEquipmentId()),
                LocalDateTime.of(2026, 11, 5, 11, 0), LocalDateTime.of(2026, 11, 5, 13, 0));

        Order order = studio.registerRequest("Компания Панорама", "+7-900-100-00-02",
                "video@panorama.test", "Москва, выставочный зал", "Виртуальная экскурсия",
                LocalDateTime.of(2026, 11, 5, 10, 0), "4K", 10,
                "Панорамное видео 360 градусов длительностью 10 минут");
        int id = order.getOrderId();
        System.out.println("Заказ №" + id + " зарегистрирован. Статус: " + order.getStatus());

        List<ResourceRequest> resources = List.of(
                new ResourceRequest("employee", operator.getEmployeeId()),
                new ResourceRequest("employee", editor.getEmployeeId()),
                new ResourceRequest("equipment", camera.getEquipmentId()),
                new ResourceRequest("equipment", tripod.getEquipmentId()));
        LocalDateTime firstStart = LocalDateTime.of(2026, 11, 5, 10, 0);
        LocalDateTime firstEnd = LocalDateTime.of(2026, 11, 5, 14, 0);
        System.out.println("05.11 10:00–14:00: ресурсы доступны? "
                + (studio.checkResourceAvailability(resources, id, firstStart, firstEnd) ? "да" : "нет"));
        LocalDateTime newStart = LocalDateTime.of(2026, 11, 5, 15, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 11, 5, 19, 0);
        System.out.println("05.11 15:00–19:00: ресурсы доступны? "
                + (studio.checkResourceAvailability(resources, id, newStart, newEnd) ? "да" : "нет"));

        List<CostItem> staffCosts = List.of(
                new CostItem(operator.getEmployeeId(), decimal("4"), decimal("1200")),
                new CostItem(editor.getEmployeeId(), decimal("3"), decimal("900")));
        List<CostItem> equipmentCosts = List.of(
                new CostItem(camera.getEquipmentId(), decimal("4"), decimal("500")),
                new CostItem(tripod.getEquipmentId(), decimal("4"), decimal("150")));
        Estimate estimate = studio.calculateOrderCost(order, staffCosts, equipmentCosts, decimal("250"), newEnd);
        System.out.println("Предварительная стоимость: " + estimate.estimatedPrice() + " руб.");
        System.out.println("Ориентировочный срок: "
                + estimate.deadline().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        studio.updateOrderStatus(id, "согласован", "Согласование условий", newStart.minusDays(1));
        studio.planOrder(id, List.of(operator.getEmployeeId(), editor.getEmployeeId()),
                List.of(camera.getEquipmentId(), tripod.getEquipmentId()), newStart, newEnd);
        System.out.println("После резервирования статус: " + order.getStatus());
        studio.addVideoMaterial(id, "исходный", "videos/order2/raw.mp4", newEnd, "не проверен");
        studio.addVideoMaterial(id, "готовый", "videos/order2/final.mp4",
                LocalDateTime.of(2026, 11, 5, 21, 0), "проверен");
        studio.updateOrderStatus(id, "завершён", "Передача результата",
                LocalDateTime.of(2026, 11, 5, 21, 10));
        System.out.println("Итоговый статус: " + order.getStatus());
        System.out.println("Назначений сотрудников: " + studio.getAssignmentCount()
                + "; резервирований оборудования: " + studio.getReservationCount());
        System.out.println("Видеоматериалов по заказу: " + studio.countVideoMaterials(id));
    }
}
