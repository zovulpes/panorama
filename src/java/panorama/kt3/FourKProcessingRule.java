package panorama.kt3;

public final class FourKProcessingRule implements ProcessingRule {
    @Override public String getFormat() { return "4K"; }
    @Override public int minutesPerVideoMinute() { return 8; }
}
