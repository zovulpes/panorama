package panorama.kt3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public record Estimate(BigDecimal estimatedPrice, LocalDateTime deadline) { }
