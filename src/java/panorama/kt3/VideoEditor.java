package panorama.kt3;

public final class VideoEditor extends Employee {
    public VideoEditor(int id, String name, String phone) {
        super(id, name, "специалист по обработке", phone);
    }
    @Override public String describeWork() { return "Сшивка, обработка и контроль качества видеоматериала"; }
}
