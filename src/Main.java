import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        runDemo();
    }

    static Map<String, List<Map<String, Object>>> createDatabase() {
        Map<String, List<Map<String, Object>>> db = new LinkedHashMap<>();
        for (String name : List.of(
                "clients", "orders", "employees", "employee_assignments",
                "equipment", "equipment_reservations", "stages", "video_materials")) {
            db.put(name, new ArrayList<>());
        }
        return db;
    }

    static Map<String, Object> row(Object... entries) {
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("Должно быть чётное число аргументов");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((String) entries[i], entries[i + 1]);
        }
        return result;
    }

    static int nextId(List<Map<String, Object>> rows, String key) {
        int max = 0;
        for (Map<String, Object> item : rows) {
            max = Math.max(max, (Integer) item.get(key));
        }
        return max + 1;
    }

    static Map<String, Object> getById(
            List<Map<String, Object>> rows, String idKey, int id) {
        for (Map<String, Object> item : rows) {
            if (Objects.equals(item.get(idKey), id)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Запись " + idKey + "=" + id + " не найдена");
    }

    static int addEmployee(Map<String, List<Map<String, Object>>> db,
                           String fullName, String role, String phone) {
        int id = nextId(db.get("employees"), "employee_id");
        db.get("employees").add(row("employee_id", id, "full_name", fullName,
                "role", role, "phone", phone));
        return id;
    }

    static int addEquipment(Map<String, List<Map<String, Object>>> db,
                            String name, String type, String inventoryNumber, String condition) {
        int id = nextId(db.get("equipment"), "equipment_id");
        db.get("equipment").add(row("equipment_id", id, "equipment_name", name,
                "equipment_type", type, "inventory_number", inventoryNumber,
                "condition", condition));
        return id;
    }

    static Map<String, Object> registerRequest(
            Map<String, List<Map<String, Object>>> db,
            String clientName, String phone, String email,
            String location, String purpose, LocalDateTime requestedDate,
            String videoFormat, int durationMinutes, String requirements) {
        if (clientName == null || clientName.isBlank() || phone == null || phone.isBlank()
                || location == null || location.isBlank() || requestedDate == null
                || durationMinutes <= 0 || requirements == null || requirements.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля заявки");
        }

        int clientId = nextId(db.get("clients"), "client_id");
        db.get("clients").add(row("client_id", clientId,
                "client_name", clientName, "phone", phone, "email", email));

        int orderId = nextId(db.get("orders"), "order_id");
        Map<String, Object> order = row(
                "order_id", orderId, "client_id", clientId,
                "request_date", LocalDateTime.now(),
                "shooting_location", location, "shooting_purpose", purpose,
                "requested_date", requestedDate,
                "planned_start", null, "planned_end", null,
                "video_format", videoFormat, "duration_minutes", durationMinutes,
                "requirements", requirements, "estimated_price", null,
                "deadline", null, "status", "новый");
        db.get("orders").add(order);

        db.get("stages").add(row(
                "stage_id", nextId(db.get("stages"), "stage_id"),
                "order_id", orderId, "stage_name", "Регистрация заявки",
                "status", "в работе", "started_at", LocalDateTime.now(),
                "finished_at", null));
        return order;
    }

    static List<Map<String, Object>> collectBookings(Map<String, List<Map<String, Object>>> db) {
        List<Map<String, Object>> bookings = new ArrayList<>();
        for (Map<String, Object> assignment : db.get("employee_assignments")) {
            bookings.add(row("resource_type", "employee",
                    "resource_id", assignment.get("employee_id"),
                    "order_id", assignment.get("order_id"),
                    "start", assignment.get("start_at"), "end", assignment.get("end_at")));
        }
        for (Map<String, Object> reservation : db.get("equipment_reservations")) {
            bookings.add(row("resource_type", "equipment",
                    "resource_id", reservation.get("equipment_id"),
                    "order_id", reservation.get("order_id"),
                    "start", reservation.get("reserved_from"),
                    "end", reservation.get("reserved_to")));
        }
        return bookings;
    }

    static boolean checkResourceAvailability(
            Map<String, List<Map<String, Object>>> db,
            List<Map<String, Object>> resources, int currentOrderId,
            LocalDateTime start, LocalDateTime end) {
        validateInterval(start, end);
        List<Map<String, Object>> bookings = collectBookings(db);

        for (Map<String, Object> resource : resources) {
            String type = (String) resource.get("resource_type");
            int id = (Integer) resource.get("resource_id");
            if (!type.equals("employee") && !type.equals("equipment")) {
                throw new IllegalArgumentException("Неизвестный тип ресурса: " + type);
            }
            if (id <= 0) {
                throw new IllegalArgumentException("Некорректный идентификатор ресурса");
            }

            for (Map<String, Object> booking : bookings) {
                if (!Objects.equals(type, booking.get("resource_type"))
                        || !Objects.equals(id, booking.get("resource_id"))
                        || Objects.equals(currentOrderId, booking.get("order_id"))) {
                    continue;
                }
                LocalDateTime busyStart = (LocalDateTime) booking.get("start");
                LocalDateTime busyEnd = (LocalDateTime) booking.get("end");
                if (start.isBefore(busyEnd) && busyStart.isBefore(end)) {
                    return false;
                }
            }
        }
        return true;
    }

    static void validateInterval(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Начало интервала должно быть раньше конца");
        }
    }

    static Map<String, Object> calculateOrderCost(
            Map<String, Object> order,
            List<Map<String, Object>> employeeCosts,
            List<Map<String, Object>> equipmentCosts,
            BigDecimal ratePerVideoMinute, LocalDateTime shootingEnd) {
        int videoMinutes = (Integer) order.get("duration_minutes");
        String format = (String) order.get("video_format");
        if (videoMinutes <= 0 || shootingEnd == null) {
            throw new IllegalArgumentException("Неверные параметры заказа");
        }
        validateNonNegative(ratePerVideoMinute, "Тариф обработки видео");

        int processingMinutesPerVideoMinute = switch (format) {
            case "4K" -> 8;
            case "6K" -> 10;
            case "8K" -> 12;
            default -> throw new IllegalArgumentException("Неизвестный формат видео: " + format);
        };

        BigDecimal cost = ratePerVideoMinute.multiply(BigDecimal.valueOf(videoMinutes));
        for (Map<String, Object> employee : employeeCosts) {
            BigDecimal hours = (BigDecimal) employee.get("hours");
            BigDecimal rate = (BigDecimal) employee.get("hourly_rate");
            validateNonNegative(hours, "Время работы сотрудника");
            validateNonNegative(rate, "Тариф сотрудника");
            cost = cost.add(hours.multiply(rate));
        }
        for (Map<String, Object> equipment : equipmentCosts) {
            BigDecimal hours = (BigDecimal) equipment.get("hours");
            BigDecimal rate = (BigDecimal) equipment.get("hourly_rate");
            validateNonNegative(hours, "Время использования оборудования");
            validateNonNegative(rate, "Тариф оборудования");
            cost = cost.add(hours.multiply(rate));
        }

        LocalDateTime deadline = shootingEnd.plusMinutes(
                60L + (long) videoMinutes * processingMinutesPerVideoMinute);
        BigDecimal total = cost.setScale(2, RoundingMode.HALF_UP);
        order.put("estimated_price", total);
        order.put("deadline", deadline);
        return row("estimated_price", total, "deadline", deadline);
    }

    static void validateNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " не может быть отрицательным");
        }
    }

    static void updateOrderStatus(Map<String, List<Map<String, Object>>> db,
                                  int orderId, String newStatus,
                                  String stageName, LocalDateTime timestamp) {
        Set<String> allowed = Set.of("новый", "согласован", "запланирован",
                "съёмка", "обработка", "на проверке", "завершён", "отменён");
        if (!allowed.contains(newStatus) || stageName == null || stageName.isBlank()
                || timestamp == null) {
            throw new IllegalArgumentException("Некорректный статус, этап или время");
        }
        Map<String, Object> order = getById(db.get("orders"), "order_id", orderId);
        for (Map<String, Object> stage : db.get("stages")) {
            if (Objects.equals(stage.get("order_id"), orderId)
                    && stage.get("finished_at") == null) {
                stage.put("status", "завершён");
                stage.put("finished_at", timestamp);
            }
        }
        order.put("status", newStatus);
        boolean finished = newStatus.equals("завершён") || newStatus.equals("отменён");
        db.get("stages").add(row(
                "stage_id", nextId(db.get("stages"), "stage_id"),
                "order_id", orderId, "stage_name", stageName,
                "status", finished ? "завершён" : "в работе",
                "started_at", timestamp,
                "finished_at", finished ? timestamp : null));
    }

    static List<Map<String, Object>> buildResources(
            List<Integer> employeeIds, List<Integer> equipmentIds) {
        List<Map<String, Object>> resources = new ArrayList<>();
        for (int id : employeeIds) {
            resources.add(row("resource_type", "employee", "resource_id", id));
        }
        for (int id : equipmentIds) {
            resources.add(row("resource_type", "equipment", "resource_id", id));
        }
        return resources;
    }

    static void planOrder(Map<String, List<Map<String, Object>>> db, int orderId,
                          List<Integer> employeeIds, List<Integer> equipmentIds,
                          LocalDateTime start, LocalDateTime end) {
        Map<String, Object> order = getById(db.get("orders"), "order_id", orderId);
        validateInterval(start, end);
        if (employeeIds.isEmpty() || equipmentIds.isEmpty()
                || Set.copyOf(employeeIds).size() != employeeIds.size()
                || Set.copyOf(equipmentIds).size() != equipmentIds.size()) {
            throw new IllegalArgumentException("Ресурсы должны быть указаны без повторов");
        }
        for (int employeeId : employeeIds) {
            getById(db.get("employees"), "employee_id", employeeId);
        }
        for (int equipmentId : equipmentIds) {
            getById(db.get("equipment"), "equipment_id", equipmentId);
        }
        List<Map<String, Object>> requested = buildResources(employeeIds, equipmentIds);
        if (!checkResourceAvailability(db, requested, orderId, start, end)) {
            throw new IllegalStateException("Конфликт расписания: ресурсы уже заняты");
        }
        for (int employeeId : employeeIds) {
            Map<String, Object> employee = getById(
                    db.get("employees"), "employee_id", employeeId);
            db.get("employee_assignments").add(row(
                    "assignment_id", nextId(db.get("employee_assignments"), "assignment_id"),
                    "order_id", orderId, "employee_id", employeeId,
                    "task_role", employee.get("role"),
                    "start_at", start, "end_at", end));
        }
        for (int equipmentId : equipmentIds) {
            db.get("equipment_reservations").add(row(
                    "reservation_id", nextId(db.get("equipment_reservations"), "reservation_id"),
                    "order_id", orderId, "equipment_id", equipmentId,
                    "reserved_from", start, "reserved_to", end));
        }
        order.put("planned_start", start);
        order.put("planned_end", end);
        updateOrderStatus(db, orderId, "запланирован", "Планирование съёмки", start);
    }

    static void addVideoMaterial(Map<String, List<Map<String, Object>>> db,
                                 int orderId, String type, String path,
                                 LocalDateTime createdAt, String qualityStatus) {
        getById(db.get("orders"), "order_id", orderId);
        if (type == null || type.isBlank() || path == null || path.isBlank()) {
            throw new IllegalArgumentException("Укажите тип и путь видеоматериала");
        }
        db.get("video_materials").add(row(
                "material_id", nextId(db.get("video_materials"), "material_id"),
                "order_id", orderId, "material_type", type,
                "storage_path", path, "created_at", createdAt,
                "quality_status", qualityStatus));
    }

    static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    static void runDemo() {
        Map<String, List<Map<String, Object>>> db = createDatabase();
        int operator = addEmployee(db, "Иван Петров", "оператор", "+7-900-000-00-01");
        int editor = addEmployee(db, "Мария Иванова", "специалист по обработке", "+7-900-000-00-02");
        int camera = addEquipment(db, "Панорамная камера", "камера", "CAM-001", "исправно");
        int tripod = addEquipment(db, "Штатив", "штатив", "TRI-001", "исправно");

        Map<String, Object> oldOrder = registerRequest(db, "Компания Сфера", "+7-900-100-00-01",
                "office@sfera.test", "Москва", "Обзор офиса",
                LocalDateTime.of(2026, 11, 5, 11, 0), "4K", 5,
                "Короткое видео для сайта");
        planOrder(db, (Integer) oldOrder.get("order_id"),
                List.of(operator), List.of(camera),
                LocalDateTime.of(2026, 11, 5, 11, 0),
                LocalDateTime.of(2026, 11, 5, 13, 0));

        Map<String, Object> order = registerRequest(db, "Компания Панорама", "+7-900-100-00-02",
                "video@panorama.test", "Москва, выставочный зал", "Виртуальная экскурсия",
                LocalDateTime.of(2026, 11, 5, 10, 0), "4K", 10,
                "Панорамное видео 360 градусов длительностью 10 минут");
        int id = (Integer) order.get("order_id");
        System.out.println("Заказ №" + id + " зарегистрирован. Статус: " + order.get("status"));

        List<Map<String, Object>> resources = buildResources(
                List.of(operator, editor), List.of(camera, tripod));
        LocalDateTime firstStart = LocalDateTime.of(2026, 11, 5, 10, 0);
        LocalDateTime firstEnd = LocalDateTime.of(2026, 11, 5, 14, 0);
        System.out.println("05.11 10:00–14:00: ресурсы доступны? "
                + (checkResourceAvailability(db, resources, id, firstStart, firstEnd) ? "да" : "нет"));

        LocalDateTime newStart = LocalDateTime.of(2026, 11, 5, 15, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 11, 5, 19, 0);
        System.out.println("05.11 15:00–19:00: ресурсы доступны? "
                + (checkResourceAvailability(db, resources, id, newStart, newEnd) ? "да" : "нет"));

        List<Map<String, Object>> staffCosts = List.of(
                row("employee_id", operator, "hours", decimal("4"), "hourly_rate", decimal("1200")),
                row("employee_id", editor, "hours", decimal("3"), "hourly_rate", decimal("900")));
        List<Map<String, Object>> equipmentCosts = List.of(
                row("equipment_id", camera, "hours", decimal("4"), "hourly_rate", decimal("500")),
                row("equipment_id", tripod, "hours", decimal("4"), "hourly_rate", decimal("150")));
        Map<String, Object> estimate = calculateOrderCost(
                order, staffCosts, equipmentCosts, decimal("250"), newEnd);
        System.out.println("Предварительная стоимость: " + estimate.get("estimated_price") + " руб.");
        System.out.println("Ориентировочный срок: "
                + ((LocalDateTime) estimate.get("deadline")).format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        updateOrderStatus(db, id, "согласован", "Согласование условий", newStart.minusDays(1));
        planOrder(db, id, List.of(operator, editor), List.of(camera, tripod), newStart, newEnd);
        System.out.println("После резервирования статус: " + order.get("status"));
        addVideoMaterial(db, id, "исходный", "videos/order2/raw.mp4", newEnd, "не проверен");
        addVideoMaterial(db, id, "готовый", "videos/order2/final.mp4",
                LocalDateTime.of(2026, 11, 5, 21, 0), "проверен");
        updateOrderStatus(db, id, "завершён", "Передача результата",
                LocalDateTime.of(2026, 11, 5, 21, 10));
        System.out.println("Итоговый статус: " + order.get("status"));
        System.out.println("Назначений сотрудников: " + db.get("employee_assignments").size()
                + "; резервирований оборудования: " + db.get("equipment_reservations").size());
        System.out.println("Видеоматериалов по заказу: " + countByOrder(db.get("video_materials"), id));
    }

    static int countByOrder(List<Map<String, Object>> rows, int orderId) {
        int count = 0;
        for (Map<String, Object> item : rows) {
            if (Objects.equals(item.get("order_id"), orderId)) {
                count++;
            }
        }
        return count;
    }

}
