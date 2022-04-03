package pl.mmilewczyk.userservice.model.enums;

public enum LanguageLevel {
    A1(1),
    A2(2),
    B1(3),
    B2(4),
    C1(5),
    C2(6),
    NATIVE(7);

    private final Integer measure;

    /**
     * @param measure - The measure variable was introduced to reflect the sophistication level graphically.
     */
    LanguageLevel(Integer measure) {
        this.measure = measure;
    }

    public Integer getMeasure() {
        return measure;
    }
}
