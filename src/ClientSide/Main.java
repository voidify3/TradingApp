package ClientSide;

// class imports
// java imports

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/***
 * @authors Johnny, Alistair, Scott & Sophia
 */
public class Main {

    /***
     * Invokes the Client-Side GUI & holds the date/time
     * @param args args
     */
    public static void main(String[] args) {

        //Date & time formatter
        DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        TradingAppDataSource t;
        if (args.length == 1 && args[0].equals("MOCK")) {
            t = new MockDataSource();
        } else {
            t = new NetworkDataSource();
        }

        // BOILERPLATE needed when running the GUI to make sure it's thread safe
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                TradingAppGUI gui = new TradingAppGUI(new TradingAppData(t));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });

    }
}