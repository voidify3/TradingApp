package ServerSide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Most code borrowed from week 8 address book exercise
 */
public class ServerGUI {
    public final static int FIVE_MINUTES = 300000;
    static JLabel tradeLabel = new JLabel("Trade reconciliation not yet performed                          ");

    public static void main(String[] args) {
        NetworkServer server = new NetworkServer(new JDBCDataSource());
        SwingUtilities.invokeLater(() -> createAndShowGUI(server));
        try {
            server.start();
            server.tradeReconciliation.schedule(new TimerTask() {
                @Override
                public void run() {
                    server.reconcileTrades();
                    LocalDateTime reconcileTime =  LocalDateTime.now();
                    SwingUtilities.invokeLater(() -> tradeLabel.setText("Trade reconciliation last done "
                            + reconcileTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))));
                }
            }, 0, FIVE_MINUTES);

        } catch (IOException e) {
            // In the case of an exception, show an error message and terminate
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null, e.getMessage(),
                        "Error starting server", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            });
        }
    }
    private static void createAndShowGUI(NetworkServer server) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Network server for Address Book");
        JButton shutdownButton = new JButton("Shut down server");
        // This button will simply close the dialog. CLosing the dialog
        // will shut down the server
        shutdownButton.addActionListener(e -> dialog.dispose());

        // When the dialog is closed, shut down the server
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                server.shutdown();
            }
        });

        // Create a label to show server info
        JLabel serverLabel = new JLabel("Server running on port " + NetworkServer.getPort());
        // Add the button and labels to the dialog
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(shutdownButton, BorderLayout.SOUTH);
        dialog.getContentPane().add(serverLabel, BorderLayout.NORTH);
        dialog.getContentPane().add(tradeLabel,BorderLayout.CENTER);
        dialog.pack();

        // Centre the dialog on the screen
        dialog.setLocationRelativeTo(null);

        // Show the dialog
        dialog.setVisible(true);
    }

}
