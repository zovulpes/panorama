package panorama.kt3;

import java.math.BigDecimal;
public record CostItem(int resourceId, BigDecimal hours, BigDecimal hourlyRate) { }
