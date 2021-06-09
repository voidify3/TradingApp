package ServerSide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.TimerTask;


/**
 * Most code borrowed from week 8 address book exercise
 */
public class ServerGUI {
    public final static int FIVE_MINUTES = 300000;
    static JLabel tradeLabel = new JLabel("Trade reconciliation not yet performed");

    public static void main(String[] args) throws SQLException {
        NetworkServer server = new NetworkServer();
        SwingUtilities.invokeLater(() -> createAndShowGUI(server));
        if (args.length == 1 && args[0].equals("RESET")) {
            server.resetEverything();
            server.setupTables();
        }
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
        Container mainPanel = dialog.getContentPane();
        dialog.setTitle("Network server for Address Book");
        JButton shutdownButton = new JButton("Shut down server");
        JButton clearDBButton = new JButton("Drop all database tables");
        JButton setupDBButton = new JButton("Rerun table creation script");
        JButton reconcileButton = new JButton("Reconcile trades now");
        JPanel controlPanel = new JPanel();

        reconcileButton.addActionListener(e -> server.reconcileTrades());
        clearDBButton.addActionListener(e -> {
            try {
                server.resetEverything();
            } catch (SQLException throwables) {
                System.out.println("Tables could not be dropped");
            }
        });
        setupDBButton.addActionListener(e -> {
            try {
                server.setupTables();
            } catch (SQLException throwables) {
                System.out.println("An error occurred rerunning the table creation script");
            }
        });

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
        mainPanel.setLayout(new BorderLayout());
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(reconcileButton, BorderLayout.NORTH);
        controlPanel.add(clearDBButton, BorderLayout.CENTER);
        controlPanel.add(setupDBButton, BorderLayout.EAST);
        controlPanel.add(shutdownButton, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(serverLabel, BorderLayout.NORTH);
        mainPanel.add(tradeLabel,BorderLayout.CENTER);
        dialog.pack();

        // Centre the dialog on the screen
        dialog.setLocationRelativeTo(null);

        // Show the dialog
        dialog.setVisible(true);
    }

}
