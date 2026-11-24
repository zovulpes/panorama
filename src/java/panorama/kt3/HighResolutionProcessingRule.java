package panorama.kt3;

public final class HighResolutionProcessingRule implements ProcessingRule {
    private final String format;
    public HighResolutionProcessingRule(String format) {
        if (!format.equals("6K") && !format.equals("8K"))
            throw new IllegalArgumentException("Поддерживаются только 6K и 8K");
        this.format = format;
    }
    @Override public String getFormat() { return format; }
    @Override public int minutesPerVideoMinute() { return format.equals("6K") ? 10 : 12; }
}
