package ClientSide;

/***
 * @author Johnny, Alistair, Scott & Sophia
 */
public class Main {

    /***
     * Invokes the Client-Side GUI & holds the date/time
     * @param args args
     */
    public static void main(String[] args) {

        TradingAppDataSource t;
        if (args.length == 1 && args[0].equals("MOCK")) {
            t = new MockDataSource();
        } else {
            t = new NetworkDataSource();
        }

        // BOILERPLATE needed when running the GUI to make sure it's thread safe
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                NewTradingAppGUI gui = new NewTradingAppGUI(new TradingAppData(t));
                gui.createAndShowGUI();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });

    }
}