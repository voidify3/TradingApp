package common;

public enum ProtocolKeywords {
    SELECT(),
    INSERT(),
    UPDATE(),
    DELETE(),
    SPECIAL();
    public static final String DROP_PASSWORD = "Clear";
    public static final String RECREATE_PASSWORD = "Recreate";
    public static final String TRADE_DELAY_PASSWORD = "GetTradeDelay";
    public final static int RECONCILIATION_INTERVAL = 5 * 60 * 1000; //milliseconds
}
