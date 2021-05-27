package ClientSide;

// class imports

import common.*;
import common.Exceptions.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * @authors Johnny Madigan & Scott Peachey
 */
public class GuiListeners {

    // Checks login credentials-----------------------------------------------------------------------------------------
    private static void checkLogin() {
        // store the user object for the duration of the session (if login was successful)
        String username = TradingAppGUI.c.usernameInput.getText();
        String password = new String(TradingAppGUI.c.passwordInput.getPassword());
        try {
            TradingAppGUI.user = TradingAppGUI.i.login(username, password);

            if (TradingAppGUI.user.getAdminAccess()) {
                TradingAppGUI.shellPanel(adminHome(), true);
            } else {
                TradingAppGUI.shellPanel(userHome(), true);
            }
        } catch (DoesNotExist | IllegalString ex) {
            ex.printStackTrace();
            TradingAppGUI.c.passwordInput.setText("");  // clear password field
            TradingAppGUI.c.invalidLabel.setForeground(Color.RED);
            TradingAppGUI.c.invalidLabel.setText("Invalid Login Credentials");
        }
    }

    // Panels with the content to be passed into the shell template-----------------------------------------------------

    // Puts the user home content in the shell panel
    public static JPanel userHome() throws DoesNotExist {
        // Table Panel
        TradingAppGUI.c.userHome = new JPanel();
        TradingAppGUI.c.userHome.setPreferredSize(new Dimension(600,275));
        // Table Data
        String[] columnNames = { "Asset ID", "Description", "Qty", "$ Current"};
        String[][] data = new String[0][4];
        //For every inventory record of the logged-in user's org unit...
        ArrayList<InventoryRecord> yourInvs = TradingAppGUI.i.getInventoriesByOrgUnit(TradingAppGUI.user.getUnit());
        if (yourInvs != null) for (InventoryRecord inventoryRecord : yourInvs) {
            //Get information on the asset of the inventory record
            int assetID = inventoryRecord.getAssetID();
            Asset asset = TradingAppGUI.i.getAssetByKey(inventoryRecord.getAssetID());
            //Get information on the resolved BuyOrders of this asset (i.e. price history)
            ArrayList<BuyOrder> assetPriceHistory = TradingAppGUI.i.getResolvedBuysByAsset(assetID);
            Optional<BuyOrder> mostRecentSale = assetPriceHistory.stream().max(BuyOrder::compareTo);
            AtomicInteger recentPrice = new AtomicInteger();
            mostRecentSale.ifPresent(buyOrder -> recentPrice.set(buyOrder.price));
            //recentPrice now contains the most recent price of the asset, or 0 if the asset has never been sold
            String[] dataNew = new String[]{ asset.getIdString(), asset.getDescription(),
                    String.valueOf(inventoryRecord.getQuantity()), "$" + recentPrice.get()};
            data = Arrays.copyOf(data, data.length + 1);
            data[data.length - 1] = dataNew;
        }
 //Make cells not editable
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        TradingAppGUI.c.userHoldings = new JTable(data, columnNames);
        TradingAppGUI.c.userHoldings.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(TradingAppGUI.c.userHoldings);
        scrollPane.setPreferredSize(new Dimension(600,270));
        // Set column widths
        TradingAppGUI.c.userHoldings.getColumnModel().getColumn(0).setWidth(25);
        TradingAppGUI.c.userHoldings.getColumnModel().getColumn(1).setPreferredWidth(400);
        TradingAppGUI.c.userHoldings.getColumnModel().getColumn(2).setWidth(20);
        TradingAppGUI.c.userHoldings.getColumnModel().getColumn(2).setWidth(30);
        TradingAppGUI.c.userHome.setBackground(Color.decode(TradingAppGUI.DARKGREY));
        TradingAppGUI.c.userHome.add(scrollPane);

        assetTableListener();
        return TradingAppGUI.c.userHome;
    }

    // Puts the admin home content in the shell panel
    public static JPanel adminHome() {
        TradingAppGUI.c.adminHome = new JPanel();
        JButton button = new JButton("new user");
        JButton button2 = new JButton("delete user");
        JButton button3 = new JButton("modify user");
        JButton button4 = new JButton("other admin stuff");

        TradingAppGUI.c.adminHome.add(button);
        TradingAppGUI.c.adminHome.add(button2);
        TradingAppGUI.c.adminHome.add(button3);
        TradingAppGUI.c.adminHome.add(button4);

        ArrayList<String> allUsernames = TradingAppGUI.i.getAllUsernames();
        String[] allUserList = new String[allUsernames.size()];
        allUserList = allUsernames.toArray(allUserList);
        GuiSearch fcb = new GuiSearch(Arrays.asList(allUserList));

        TradingAppGUI.c.adminHome.add(fcb);
        return TradingAppGUI.c.adminHome;
    }

    // Puts the asset page content in the shell panel
    public static JPanel assetPage(String asset) {
        TradingAppGUI.c.assetPage = new JPanel(new GridBagLayout());
        TradingAppGUI.c.assetPage.setPreferredSize(new Dimension(600,325));
        TradingAppGUI.c.assetPage.setBackground(Color.decode(TradingAppGUI.DARKGREY));
        TradingAppGUI.c.assetPage.setLayout(new BorderLayout());

        // Position the interactive components (text fields, buttons etc)
        JPanel intervalButtons = new JPanel();
        intervalButtons.add(TradingAppGUI.c.daysButton);
        intervalButtons.add(TradingAppGUI.c.weeksButton);
        intervalButtons.add(TradingAppGUI.c.monthsButton);
        intervalButtons.add(TradingAppGUI.c.yearsButton);
        TradingAppGUI.c.assetPage.add(intervalButtons, BorderLayout.NORTH);

        JPanel graphPanel = new JPanel();

        JPanel orderButtons = new JPanel();
        orderButtons.add(TradingAppGUI.c.buyButton);
        orderButtons.add(TradingAppGUI.c.sellButton);
        TradingAppGUI.c.assetPage.add(orderButtons, BorderLayout.SOUTH);

        return TradingAppGUI.c.assetPage;
    }

    // Login portal listeners-------------------------------------------------------------------------------------------

    // Triggers checkLogin when the 'Login' button is pressed
    public static void loginActionListener() {
        TradingAppGUI.c.loginButton.addActionListener(e -> checkLogin());
    }

    // Triggers checkLogin when the 'Enter' key is pressed
    public static void loginKeyListener() {
        TradingAppGUI.c.passwordInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    checkLogin();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    // Shows/hides password when the radio button passwordHidden is toggled
    public static void passwordHiddenListener() {
        TradingAppGUI.c.passwordHide.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                TradingAppGUI.c.passwordInput.setEchoChar((char) 0);
            } else {
                TradingAppGUI.c.passwordInput.setEchoChar('\u2022');
            }
        });
    }

    // Menu bar listeners-----------------------------------------------------------------------------------------------

    // Logs out the user when 'Logout' is clicked in the File menu
    public static void logoutListener() {
        TradingAppGUI.c.logoutMenu.addActionListener(e -> {
            try {
                TradingAppGUI.user = null; // reset session user data
                TradingAppGUI.loginPanel(); // creates & shows login portal
            } catch (AlreadyExists | IllegalString ex) {
                ex.printStackTrace();
            }
        });
    }

    // Dev master key to bypass login for testing (goes straight to user home)
    public static void masterUserKeyListener() {
        TradingAppGUI.c.masterUserKey.addActionListener(e -> {
            TradingAppGUI.user = TradingAppGUI.i.userDev;
            try {
                TradingAppGUI.shellPanel(userHome(), true);
            } catch (DoesNotExist doesNotExist) {
                doesNotExist.printStackTrace();
            }
        });
    }

    // Dev master key to bypass login for testing (goes straight to admin home)
    public static void masterAdminKeyListener() {
        TradingAppGUI.c.masterAdminKey.addActionListener(e -> {
            TradingAppGUI.user = TradingAppGUI.i.adminDev;
            TradingAppGUI.shellPanel(adminHome(), true);
        });
    }

    // Closes mainFrame and stops Main when 'Exit' is clicked in the File menu
    public static void exitListener() {
        TradingAppGUI.c.exitMenu.addActionListener(ev -> System.exit(0));
    }

    public static void homeListener() {
        TradingAppGUI.c.homeButton.addActionListener(e -> {
            if (TradingAppGUI.user.getAdminAccess()) {
                TradingAppGUI.c.orgUnitLabel.setText("ORG UNIT HERE");
                TradingAppGUI.shellPanel(adminHome(), true);
            } else {
                TradingAppGUI.c.orgUnitLabel.setText("ORG UNIT HERE");
                try {
                    TradingAppGUI.shellPanel(userHome(), true);
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            }
        });
    }

    public static void assetTableListener() {
        TradingAppGUI.c.userHoldings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = TradingAppGUI.c.userHoldings.rowAtPoint(e.getPoint());
                String asset = TradingAppGUI.c.userHoldings.getValueAt(row, 1).toString();
                TradingAppGUI.shellPanel(assetPage(asset), false);
                TradingAppGUI.c.orgUnitLabel.setText(asset.toUpperCase());
                TradingAppGUI.c.userHome.remove(TradingAppGUI.c.welcomeLabel);
                System.out.println(asset);
            }
        });
    }

}
