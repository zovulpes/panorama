package panorama.kt3;

import java.util.Objects;

public final class Client {
    private final int clientId;
    private final String clientName;
    private final String phone;
    private final String email;

    public Client(int clientId, String clientName, String phone, String email) {
        if (clientId <= 0 || Objects.requireNonNull(clientName, "clientName").isBlank()
                || Objects.requireNonNull(phone, "phone").isBlank())
            throw new IllegalArgumentException("Неверные данные клиента");
        this.clientId = clientId;
        this.clientName = clientName;
        this.phone = phone;
        this.email = email;
    }
    public int getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public boolean matches(String otherPhone, String otherEmail) {
        return phone.equals(otherPhone) || (otherEmail != null && !otherEmail.isBlank()
                && email != null && email.equalsIgnoreCase(otherEmail));
    }
}
