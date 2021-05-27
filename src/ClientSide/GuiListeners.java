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
        String username = Gui.c.usernameInput.getText();
        String password = new String(Gui.c.passwordInput.getPassword());
        try {
            Gui.user = Gui.i.login(username, password);

            if (Gui.user.getAdminAccess()) {
                Gui.shellPanel(adminHome(), true);
            } else {
                Gui.shellPanel(userHome(), true);
            }
        } catch (DoesNotExist | IllegalString ex) {
            ex.printStackTrace();
            Gui.c.passwordInput.setText("");  // clear password field
            Gui.c.invalidLabel.setForeground(Color.RED);
            Gui.c.invalidLabel.setText("Invalid Login Credentials");
        }
    }

    // Panels with the content to be passed into the shell template-----------------------------------------------------

    // Puts the user home content in the shell panel
    public static JPanel userHome() throws DoesNotExist {
        // Table Panel
        Gui.c.userHome = new JPanel();
        Gui.c.userHome.setPreferredSize(new Dimension(600,275));
        // Table Data
        String[] columnNames = { "Asset ID", "Description", "Qty", "$ Current"};
        String[][] data = new String[0][4];
        //For every inventory record of the logged-in user's org unit...
        ArrayList<InventoryRecord> yourInvs = Gui.i.getInventoriesByOrgUnit(Gui.user.getUnit());
        if (yourInvs != null) for (InventoryRecord inventoryRecord : yourInvs) {
            //Get information on the asset of the inventory record
            int assetID = inventoryRecord.getAssetID();
            Asset asset = Gui.i.getAssetByKey(inventoryRecord.getAssetID());
            //Get information on the resolved BuyOrders of this asset (i.e. price history)
            ArrayList<BuyOrder> assetPriceHistory = Gui.i.getResolvedBuysByAsset(assetID);
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
        Gui.c.userHoldings = new JTable(data, columnNames);
        Gui.c.userHoldings.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(Gui.c.userHoldings);
        scrollPane.setPreferredSize(new Dimension(600,270));
        // Set column widths
        Gui.c.userHoldings.getColumnModel().getColumn(0).setWidth(25);
        Gui.c.userHoldings.getColumnModel().getColumn(1).setPreferredWidth(400);
        Gui.c.userHoldings.getColumnModel().getColumn(2).setWidth(20);
        Gui.c.userHoldings.getColumnModel().getColumn(2).setWidth(30);
        Gui.c.userHome.setBackground(Color.decode(Gui.DARKGREY));
        Gui.c.userHome.add(scrollPane);

        assetTableListener();
        return Gui.c.userHome;
    }

    // Puts the admin home content in the shell panel
    public static JPanel adminHome() {
        Gui.c.adminHome = new JPanel();
        JButton button = new JButton("new user");
        JButton button2 = new JButton("delete user");
        JButton button3 = new JButton("modify user");
        JButton button4 = new JButton("other admin stuff");

        Gui.c.adminHome.add(button);
        Gui.c.adminHome.add(button2);
        Gui.c.adminHome.add(button3);
        Gui.c.adminHome.add(button4);

        ArrayList<String> allUsernames = Gui.i.getAllUsernames();
        String[] allUserList = new String[allUsernames.size()];
        allUserList = allUsernames.toArray(allUserList);
        GuiSearch fcb = new GuiSearch(Arrays.asList(allUserList));

        Gui.c.adminHome.add(fcb);
        return Gui.c.adminHome;
    }

    // Puts the asset page content in the shell panel
    public static JPanel assetPage(String asset) {
        Gui.c.assetPage = new JPanel(new GridBagLayout());
        Gui.c.assetPage.setPreferredSize(new Dimension(600,325));
        Gui.c.assetPage.setBackground(Color.decode(Gui.DARKGREY));
        Gui.c.assetPage.setLayout(new BorderLayout());

        // Position the interactive components (text fields, buttons etc)
        JPanel intervalButtons = new JPanel();
        intervalButtons.add(Gui.c.daysButton);
        intervalButtons.add(Gui.c.weeksButton);
        intervalButtons.add(Gui.c.monthsButton);
        intervalButtons.add(Gui.c.yearsButton);
        Gui.c.assetPage.add(intervalButtons, BorderLayout.NORTH);

        JPanel graphPanel = new JPanel();

        JPanel orderButtons = new JPanel();
        orderButtons.add(Gui.c.buyButton);
        orderButtons.add(Gui.c.sellButton);
        Gui.c.assetPage.add(orderButtons, BorderLayout.SOUTH);

        return Gui.c.assetPage;
    }

    // Login portal listeners-------------------------------------------------------------------------------------------

    // Triggers checkLogin when the 'Login' button is pressed
    public static void loginActionListener() {
        Gui.c.loginButton.addActionListener(e -> checkLogin());
    }

    // Triggers checkLogin when the 'Enter' key is pressed
    public static void loginKeyListener() {
        Gui.c.passwordInput.addKeyListener(new KeyListener() {
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
        Gui.c.passwordHide.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Gui.c.passwordInput.setEchoChar((char) 0);
            } else {
                Gui.c.passwordInput.setEchoChar('\u2022');
            }
        });
    }

    // Menu bar listeners-----------------------------------------------------------------------------------------------

    // Logs out the user when 'Logout' is clicked in the File menu
    public static void logoutListener() {
        Gui.c.logoutMenu.addActionListener(e -> {
            try {
                Gui.user = null; // reset session user data
                Gui.loginPanel(); // creates & shows login portal
            } catch (AlreadyExists | IllegalString ex) {
                ex.printStackTrace();
            }
        });
    }

    // Dev master key to bypass login for testing (goes straight to user home)
    public static void masterUserKeyListener() {
        Gui.c.masterUserKey.addActionListener(e -> {
            Gui.user = Gui.i.userDev;
            try {
                Gui.shellPanel(userHome(), true);
            } catch (DoesNotExist doesNotExist) {
                doesNotExist.printStackTrace();
            }
        });
    }

    // Dev master key to bypass login for testing (goes straight to admin home)
    public static void masterAdminKeyListener() {
        Gui.c.masterAdminKey.addActionListener(e -> {
            Gui.user = Gui.i.adminDev;
            Gui.shellPanel(adminHome(), true);
        });
    }

    // Closes mainFrame and stops Main when 'Exit' is clicked in the File menu
    public static void exitListener() {
        Gui.c.exitMenu.addActionListener(ev -> System.exit(0));
    }

    public static void homeListener() {
        Gui.c.homeButton.addActionListener(e -> {
            if (Gui.user.getAdminAccess()) {
                Gui.c.orgUnitLabel.setText("ORG UNIT HERE");
                Gui.shellPanel(adminHome(), true);
            } else {
                Gui.c.orgUnitLabel.setText("ORG UNIT HERE");
                try {
                    Gui.shellPanel(userHome(), true);
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            }
        });
    }

    public static void assetTableListener() {
        Gui.c.userHoldings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = Gui.c.userHoldings.rowAtPoint(e.getPoint());
                String asset = Gui.c.userHoldings.getValueAt(row, 1).toString();
                Gui.shellPanel(assetPage(asset), false);
                Gui.c.orgUnitLabel.setText(asset.toUpperCase());
                Gui.c.userHome.remove(Gui.c.welcomeLabel);
                System.out.println(asset);
            }
        });
    }

}
