package panorama.kt3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class StudioService {
    private enum ResourceType { EMPLOYEE, EQUIPMENT }
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "новый", "согласован", "запланирован", "съёмка", "обработка",
            "на проверке", "завершён", "отменён");
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "новый", Set.of("согласован", "запланирован", "отменён"),
            "согласован", Set.of("запланирован", "отменён"),
            "запланирован", Set.of("съёмка", "завершён", "отменён"),
            "съёмка", Set.of("обработка", "отменён"),
            "обработка", Set.of("на проверке", "отменён"),
            "на проверке", Set.of("обработка", "завершён", "отменён"),
            "завершён", Set.of(),
            "отменён", Set.of());
    private static ResourceType parseResourceType(String type) {
        if ("employee".equals(type)) return ResourceType.EMPLOYEE;
        if ("equipment".equals(type)) return ResourceType.EQUIPMENT;
        throw new IllegalArgumentException("Неизвестный тип ресурса: " + type);
    }
    private final Map<Integer, Client> clients = new LinkedHashMap<>();
    private final Map<Integer, Order> orders = new LinkedHashMap<>();
    private final Map<Integer, Employee> employees = new LinkedHashMap<>();
    private final Map<Integer, Equipment> equipment = new LinkedHashMap<>();
    private final List<EmployeeAssignment> employeeAssignments = new ArrayList<>();
    private final List<EquipmentReservation> equipmentReservations = new ArrayList<>();
    private final List<OrderStage> stages = new ArrayList<>();
    private final List<VideoMaterial> videoMaterials = new ArrayList<>();
    private final Map<String, ProcessingRule> processingRules = new LinkedHashMap<>();

    public StudioService() {
        addProcessingRule(new FourKProcessingRule());
        addProcessingRule(new HighResolutionProcessingRule("6K"));
        addProcessingRule(new HighResolutionProcessingRule("8K"));
    }
    public void addProcessingRule(ProcessingRule rule) { processingRules.put(rule.getFormat(), rule); }
    public int getClientCount() { return clients.size(); }
    public int getOrderCount() { return orders.size(); }
    public int getAssignmentCount() { return employeeAssignments.size(); }
    public int getReservationCount() { return equipmentReservations.size(); }
    public long countVideoMaterials(int orderId) {
        return videoMaterials.stream().filter(m -> m.getOrder().getOrderId() == orderId).count();
    }
    public CameraOperator addOperator(String name, String phone) {
        CameraOperator employee = new CameraOperator(employees.size() + 1, name, phone);
        employees.put(employee.getEmployeeId(), employee); return employee;
    }
    public VideoEditor addVideoEditor(String name, String phone) {
        VideoEditor employee = new VideoEditor(employees.size() + 1, name, phone);
        employees.put(employee.getEmployeeId(), employee); return employee;
    }
    public Equipment addEquipment(String name, String type, String inventoryNumber, String condition) {
        Equipment item = new Equipment(equipment.size() + 1, name, type, inventoryNumber, condition);
        equipment.put(item.getEquipmentId(), item); return item;
    }
    public Order registerRequest(String clientName, String phone, String email,
                                 String location, String purpose, LocalDateTime requestedDate,
                                 String videoFormat, int durationMinutes, String requirements) {
        if (clientName == null || clientName.isBlank() || phone == null || phone.isBlank()
                || location == null || location.isBlank() || requestedDate == null
                || durationMinutes <= 0 || requirements == null || requirements.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля заявки");
        }
        Client client = null;
        for (Client existing : clients.values()) {
            if (existing.matches(phone, email)) { client = existing; break; }
        }
        if (client == null) {
            client = new Client(clients.size() + 1, clientName, phone, email);
            clients.put(client.getClientId(), client);
        }
        Order order = new Order(orders.size() + 1, client, location, purpose,
                requestedDate, videoFormat, durationMinutes, requirements);
        orders.put(order.getOrderId(), order);
        OrderStage stage = new OrderStage(stages.size() + 1, order, "Регистрация заявки",
                "в работе", LocalDateTime.now(), null);
        stages.add(stage); order.addStage(stage);
        return order;
    }
    private record Booking(ResourceType type, int id, int orderId, LocalDateTime start, LocalDateTime end) { }
    private List<Booking> collectBookings() {
        List<Booking> bookings = new ArrayList<>();
        for (EmployeeAssignment a : employeeAssignments) {
            bookings.add(new Booking(ResourceType.EMPLOYEE, a.getEmployee().getEmployeeId(), a.getOrder().getOrderId(),
                    a.getStartAt(), a.getEndAt()));
        }
        for (EquipmentReservation r : equipmentReservations) {
            bookings.add(new Booking(ResourceType.EQUIPMENT, r.getEquipment().getEquipmentId(), r.getOrder().getOrderId(),
                    r.getReservedFrom(), r.getReservedTo()));
        }
        return bookings;
    }
    public boolean checkResourceAvailability(List<ResourceRequest> resources, int currentOrderId,
                                             LocalDateTime start, LocalDateTime end) {
        validateInterval(start, end);
        List<Booking> bookings = collectBookings();
        // Первый алгоритм КТ2: два вложенных цикла, худший случай O(n*m).
        for (ResourceRequest resource : resources) {
            ResourceType type = parseResourceType(resource.resourceType());
            int id = resource.resourceId();
            if (id <= 0) throw new IllegalArgumentException("Некорректный идентификатор ресурса");
            for (Booking booking : bookings) {
                if (type != booking.type() || id != booking.id()
                        || currentOrderId == booking.orderId()) continue;
                if (start.isBefore(booking.end()) && booking.start().isBefore(end)) return false;
            }
        }
        return true;
    }
    public Estimate calculateOrderCost(Order order, List<CostItem> employeeCosts,
                                       List<CostItem> equipmentCosts,
                                       BigDecimal ratePerVideoMinute, LocalDateTime shootingEnd) {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(employeeCosts, "employeeCosts");
        Objects.requireNonNull(equipmentCosts, "equipmentCosts");
        if (orders.get(order.getOrderId()) != order)
            throw new IllegalArgumentException("Заказ не зарегистрирован в данной студии");
        int videoMinutes = order.getDurationMinutes();
        if (videoMinutes <= 0 || shootingEnd == null) throw new IllegalArgumentException("Неверные параметры заказа");
        validateNonNegative(ratePerVideoMinute, "Тариф обработки видео");
        ProcessingRule rule = processingRules.get(order.getVideoFormat());
        if (rule == null) throw new IllegalArgumentException("Неизвестный формат видео: " + order.getVideoFormat());
        BigDecimal cost = ratePerVideoMinute.multiply(BigDecimal.valueOf(videoMinutes));
        // Второй алгоритм КТ2: два последовательных цикла, худший случай O(n+m).
        for (CostItem employee : employeeCosts) {
            if (!employees.containsKey(employee.resourceId()))
                throw new IllegalArgumentException("Сотрудник не найден: " + employee.resourceId());
            validateNonNegative(employee.hours(), "Время работы сотрудника");
            validateNonNegative(employee.hourlyRate(), "Тариф сотрудника");
            cost = cost.add(employee.hours().multiply(employee.hourlyRate()));
        }
        for (CostItem item : equipmentCosts) {
            if (!equipment.containsKey(item.resourceId()))
                throw new IllegalArgumentException("Оборудование не найдено: " + item.resourceId());
            validateNonNegative(item.hours(), "Время использования оборудования");
            validateNonNegative(item.hourlyRate(), "Тариф оборудования");
            cost = cost.add(item.hours().multiply(item.hourlyRate()));
        }
        LocalDateTime deadline = rule.calculateDeadline(shootingEnd, videoMinutes);
        BigDecimal price = cost.setScale(2, RoundingMode.HALF_UP);
        order.setEstimate(price, deadline);
        return new Estimate(price, deadline);
    }
    private static void validateNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0)
            throw new IllegalArgumentException(field + " не может быть отрицательным");
    }
    private static void validateInterval(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end))
            throw new IllegalArgumentException("Начало интервала должно быть раньше конца");
    }
    public void updateOrderStatus(int orderId, String status, String stageName, LocalDateTime timestamp) {
        if (!ALLOWED_STATUSES.contains(status) || stageName == null || stageName.isBlank() || timestamp == null)
            throw new IllegalArgumentException("Некорректный статус, этап или время");
        Order order = getOrder(orderId);
        if (!ALLOWED_TRANSITIONS.get(order.getStatus()).contains(status))
            throw new IllegalStateException("Недопустимый переход: " + order.getStatus() + " -> " + status);
        for (OrderStage stage : stages) {
            if (stage.getOrder().getOrderId() == orderId && stage.getFinishedAt() == null) stage.finish(timestamp);
        }
        order.setStatus(status);
        boolean finished = status.equals("завершён") || status.equals("отменён");
        OrderStage newStage = new OrderStage(stages.size() + 1, order, stageName,
                finished ? "завершён" : "в работе", timestamp, finished ? timestamp : null);
        stages.add(newStage); order.addStage(newStage);
    }
    public Order getOrder(int orderId) {
        Order order = orders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Заказ не найден: " + orderId);
        return order;
    }
    public void planOrder(int orderId, List<Integer> employeeIds, List<Integer> equipmentIds,
                          LocalDateTime start, LocalDateTime end) {
        Order order = getOrder(orderId);
        validateInterval(start, end);
        Objects.requireNonNull(employeeIds, "employeeIds");
        Objects.requireNonNull(equipmentIds, "equipmentIds");
        if (!Set.of("новый", "согласован").contains(order.getStatus())
                || !order.getAssignments().isEmpty() || !order.getReservations().isEmpty())
            throw new IllegalStateException("Заказ уже запланирован либо его статус не допускает планирование");
        if (employeeIds.isEmpty() || equipmentIds.isEmpty()
                || Set.copyOf(employeeIds).size() != employeeIds.size()
                || Set.copyOf(equipmentIds).size() != equipmentIds.size()) {
            throw new IllegalArgumentException("Ресурсы должны быть указаны без повторов");
        }
        for (int id : employeeIds) {
            if (!employees.containsKey(id)) throw new IllegalArgumentException("Сотрудник не найден: " + id);
        }
        for (int id : equipmentIds) {
            if (!equipment.containsKey(id)) throw new IllegalArgumentException("Оборудование не найдено: " + id);
        }
        List<ResourceRequest> resources = new ArrayList<>();
        for (int id : employeeIds) resources.add(new ResourceRequest("employee", id));
        for (int id : equipmentIds) resources.add(new ResourceRequest("equipment", id));
        if (!checkResourceAvailability(resources, orderId, start, end)) {
            throw new IllegalStateException("Конфликт расписания: ресурсы уже заняты");
        }
        for (int id : employeeIds) {
            EmployeeAssignment a = new EmployeeAssignment(employeeAssignments.size() + 1, order,
                    employees.get(id), start, end);
            employeeAssignments.add(a); order.addAssignment(a);
        }
        for (int id : equipmentIds) {
            EquipmentReservation r = new EquipmentReservation(equipmentReservations.size() + 1, order,
                    equipment.get(id), start, end);
            equipmentReservations.add(r); order.addReservation(r);
        }
        order.plan(start, end);
        updateOrderStatus(orderId, "запланирован", "Планирование съёмки", start);
    }
    public void addVideoMaterial(int orderId, String type, String path,
                                 LocalDateTime createdAt, String qualityStatus) {
        Order order = getOrder(orderId);
        if (type == null || type.isBlank() || path == null || path.isBlank())
            throw new IllegalArgumentException("Укажите тип и путь видеоматериала");
        VideoMaterial material = new VideoMaterial(videoMaterials.size() + 1, order,
                type, path, createdAt, qualityStatus);
        videoMaterials.add(material); order.addMaterial(material);
    }
}
