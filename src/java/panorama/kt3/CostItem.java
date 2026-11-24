package panorama.kt3;

import java.math.BigDecimal;
import java.util.Objects;

public record CostItem(int resourceId, BigDecimal hours, BigDecimal hourlyRate) {
    public CostItem {
        Objects.requireNonNull(hours, "hours");
        Objects.requireNonNull(hourlyRate, "hourlyRate");
        if (resourceId <= 0 || hours.signum() < 0 || hourlyRate.signum() < 0)
            throw new IllegalArgumentException("Некорректные параметры стоимости ресурса");
    }
}
