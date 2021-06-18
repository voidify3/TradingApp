package ClientSide;

/***
 * @author Johnny, Alistair, Scott & Sophia
 */
class Main {

    /***
     * Invokes the Client-Side GUI & holds the date/time
     * @param args CLI arguments. If the first argument is "MOCK", use the mock data source instead of the network.
     *             If the last argument is "TESTDATA", populate test data on startup.
     */
    public static void main(String[] args) {

        TradingAppDataSource t;
        if (args.length >= 1 && args[0].equals("MOCK")) {
            t = new MockDataSource();
            System.out.println("Using mock database rather than network");
        } else {
            t = new NetworkDataSource();
        }

        // BOILERPLATE needed when running the GUI to make sure it's thread safe
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                NewTradingAppGUI gui = new NewTradingAppGUI(new TradingAppData(t));
                if (args.length >= 1 && args[args.length-1].equals("TESTDATA")) gui.populateTestData();
                gui.createAndShowGUI();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });

    }
}