package panorama.kt3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Проверки исправлений по замечаниям взаимного рецензирования КТ3. */
public final class ReviewRegressionTest {
    private static int checks = 0;

    private static void expectFailure(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            if (type.isInstance(ex)) { checks++; return; }
            throw new AssertionError("Неверный тип исключения: " + ex, ex);
        }
        throw new AssertionError("Исключение " + type.getSimpleName() + " не возникло");
    }

    public static void main(String[] args) {
        StudioService studio = new StudioService();
        CameraOperator operator = studio.addOperator("Оператор", "111");
        Equipment camera = studio.addEquipment("Камера", "камера", "CAM-01", "исправно");
        LocalDateTime start = LocalDateTime.of(2026, 11, 5, 15, 0);
        LocalDateTime end = start.plusHours(4);
        Order order = studio.registerRequest("Заказчик", "222", null, "Москва", "Экскурсия",
                start, "4K", 10, "Видео");
        expectFailure(IllegalStateException.class, () -> studio.updateOrderStatus(order.getOrderId(),
                "завершён", "Пропуск этапов", start));
        expectFailure(IllegalArgumentException.class, () -> new CostItem(0, BigDecimal.ONE, BigDecimal.ONE));
        expectFailure(IllegalArgumentException.class, () -> new CostItem(1, BigDecimal.ONE.negate(), BigDecimal.ONE));
        expectFailure(NullPointerException.class, () -> new CostItem(1, BigDecimal.ONE, null));
        expectFailure(NullPointerException.class, () -> new Client(1, "Имя", null, null));
        expectFailure(IllegalArgumentException.class, () -> studio.checkResourceAvailability(
                List.of(new ResourceRequest("other", 1)), order.getOrderId(), start, end));
        expectFailure(IllegalArgumentException.class, () -> studio.calculateOrderCost(order,
                List.of(new CostItem(99, BigDecimal.ONE, BigDecimal.ONE)), List.of(), BigDecimal.ONE, end));
        expectFailure(IllegalArgumentException.class, () -> studio.calculateOrderCost(order,
                List.of(), List.of(new CostItem(99, BigDecimal.ONE, BigDecimal.ONE)), BigDecimal.ONE, end));
        Estimate cost = studio.calculateOrderCost(order,
                List.of(new CostItem(operator.getEmployeeId(), BigDecimal.valueOf(4), BigDecimal.valueOf(1200))),
                List.of(new CostItem(camera.getEquipmentId(), BigDecimal.valueOf(4), BigDecimal.valueOf(500))),
                BigDecimal.valueOf(250), end);
        if (!cost.deadline().equals(end.plusMinutes(140)))
            throw new AssertionError("Неверный срок обработки");
        checks++;
        studio.planOrder(order.getOrderId(), List.of(operator.getEmployeeId()),
                List.of(camera.getEquipmentId()), start, end);
        expectFailure(IllegalStateException.class, () -> studio.planOrder(order.getOrderId(),
                List.of(operator.getEmployeeId()), List.of(camera.getEquipmentId()), start.plusHours(5), end.plusHours(5)));
        expectFailure(IllegalStateException.class, () -> studio.updateOrderStatus(order.getOrderId(),
                "новый", "Возврат", end));
        studio.updateOrderStatus(order.getOrderId(), "завершён", "Передано", end);
        expectFailure(IllegalStateException.class, () -> studio.updateOrderStatus(order.getOrderId(),
                "согласован", "Повторный запуск", end.plusDays(1)));
        Order foreign = new Order(15, new Client(15, "Клиент", "333", null), "Москва", "Задача",
                start, "4K", 5, "Тест");
        expectFailure(IllegalArgumentException.class, () -> order.addAssignment(
                new EmployeeAssignment(1, foreign, operator, start, end)));
        System.out.println("Проверки рецензирования пройдены: " + checks);
    }
}
