package common;

public enum ProtocolKeywords {
    SELECT(),
    INSERT(),
    UPDATE(),
    DELETE(),
    SPECIAL();
    public static final String DROP_PASSWORD = "CLEAR";
    public static final String RECREATE_PASSWORD = "RECREATE";
}
