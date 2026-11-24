package panorama.kt3;

import java.time.LocalDateTime;
import java.util.Objects;

public interface ProcessingRule {
    String getFormat();
    int minutesPerVideoMinute();

    default LocalDateTime calculateDeadline(LocalDateTime shootingEnd, int videoMinutes) {
        Objects.requireNonNull(shootingEnd, "shootingEnd");
        if (videoMinutes <= 0) throw new IllegalArgumentException("Длина видео должна быть положительной");
        return shootingEnd.plusMinutes(60L + (long) videoMinutes * minutesPerVideoMinute());
    }
}
