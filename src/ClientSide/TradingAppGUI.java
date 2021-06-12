package ClientSide;

// class imports

import common.Asset;
import common.BuyOrder;
import common.Exceptions.*;
import common.InventoryRecord;
import common.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * @author Johnny Madigan & Scott Peachey (wrote createAndShowGui, menuBar, resizeImg, loginPanel, shellPanel);
 * Sophia Walsh Long (all other code and the fact that the members aren't static;
 * above methods have also been refactored)
 */
public class TradingAppGUI {

    // Logged in user for the session
    private User user = null;

    // instances of components & interactions
    private GuiListeners listeners = new GuiListeners();
    private TradingAppData data;

    // General variables
    private final int WIDTH = 800;
    private final int HEIGHT = 500;
    private final String DARKGREY = "#4D4D4D";
    private final String WHITE = "#FCFCFC";

    private final JFrame mainFrame = new JFrame("STONK MACHINE");

    // Login panel & widgets, and change password widgets
    JPanel loginPanel = new JPanel(new GridBagLayout());
    JCheckBox passwordHide = new JCheckBox();
    JButton loginButton = new JButton("Login");
    JLabel usernameLabel = new JLabel("Username");
    JLabel passwordLabel = new JLabel("Password");
    JLabel invalidLabel = new JLabel(" ");
    JTextField usernameInput = new JTextField(10);
    JPasswordField passwordInput = new JPasswordField(10);
    JLabel newPasswordLabel = new JLabel("New password");
    JLabel confirmNewPasswordLabel = new JLabel("Confirm new password");
    JPasswordField newPasswordInput = new JPasswordField(10);
    JPasswordField confirmNewPasswordInput = new JPasswordField(10);
    JButton changePassButton = new JButton("Change password");
    JPanel changePassPanel = new JPanel();

    // Shell panel & widgets
    JPanel shellPanel = new JPanel();
    JButton homeButton = new JButton("Home");
    JButton adminButton = new JButton("Admin portal");
    JButton assetsButton = new JButton("All assets");
    JButton ordersButton = new JButton("Orders");
    JLabel welcomeLabel = new JLabel("Welcome back (username)", SwingConstants.CENTER);

    // Menu bar & widgets
    JMenuBar menuBar = new JMenuBar();
    JMenuItem logoutMenu = new JMenuItem("Logout");
    JMenuItem exitMenu = new JMenuItem("Exit");
    JMenuItem changePassword = new JMenuItem("Change password");
    JMenuItem masterUserKey = new JMenuItem("Master User Key");
    JMenuItem masterAdminKey = new JMenuItem("Master Admin Key");

    // User home & widgets
    JPanel userHome = new JPanel();
    JLabel orgUnitLabel = new JLabel("ORG UNIT HERE", SwingConstants.CENTER);
    JTable userHoldings = new JTable();

    // Admin portal & widgets
    JPanel adminHome = new JPanel();
    JButton newUserButton = new JButton("New user");
    JButton editUserButton = new JButton("Edit selected");
    JButton userListButton = new JButton("Expanded list");
    JButton newUnitButton = new JButton("New organisational unit");
    JButton editUnitButton = new JButton("Edit selected");
    JButton unitHoldingsButton = new JButton("Holdings");
    JButton newAssetButton = new JButton("New asset");
    JButton editAssetButton = new JButton("Edit selected");
    JButton assetHoldingsButton = new JButton("Holdings");

    // Asset page & widgets
    JPanel assetPage = new JPanel();
    JButton daysButton = new JButton("Days");
    JButton weeksButton = new JButton("Weeks");
    JButton monthsButton = new JButton("Months");
    JButton yearsButton = new JButton("Years");
    JButton buyButton = new JButton("Buy Asset");
    JButton sellButton = new JButton("Sell Asset");





    public TradingAppGUI(TradingAppData data) throws InvalidPrice, InvalidDate, IOException, DoesNotExist, AlreadyExists, IllegalString, InvalidAmount, OrderException {
        this.data = data;
    }

    void createAndShowGUI() throws IllegalString, AlreadyExists, IOException, DoesNotExist, InvalidPrice, InvalidDate, InvalidAmount, OrderException {
        mainFrame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        data.mockObjectsWithPrices();
        loginPanel(); // creates & shows login portal as the first screen
        menuBar(); // declare the menu-bar once here

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    // Menu Bar---------------------------------------------------------------------------------------------------------
    private void menuBar() {
        menuBar = new JMenuBar(); // reset

        JMenu fileMenu = new JMenu("File");
        fileMenu.add("Preference");
        fileMenu.add(logoutMenu);
        fileMenu.add(exitMenu);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add("Cut");
        editMenu.add("Copy");
        editMenu.add("Paste");
        menuBar.add(editMenu);

        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(changePassword);
        menuBar.add(helpMenu);

        JMenu devMenu = new JMenu("Dev Tools");
        devMenu.add(masterUserKey);
        devMenu.add(masterAdminKey);
        menuBar.add(devMenu);

        // Listeners
        listeners.logoutListener();
        listeners.exitListener();
        listeners.masterUserKeyListener();
        listeners.masterAdminKeyListener();
        listeners.homeListener();
        listeners.adminListener();
        listeners.changePassMenuListener();

        // Boilerplate
        mainFrame.setJMenuBar(menuBar);
        mainFrame.revalidate();
    }

    // Resizes images---------------------------------------------------------------------------------------------------
    /***
     *  from https://stackoverflow.com/questions/244164/how-can-i-resize-an-image-using-java
      */
    private BufferedImage resizeImg(Image originalImg, int width, int height, boolean preserveAlpha) {
        //System.out.println("resizing...");
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(width, height, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImg, 0, 0, width, height, null);
        g.dispose();
        return scaledBI;
    }

    // Login screen-----------------------------------------------------------------------------------------------------
    private void loginPanel() throws AlreadyExists, IllegalString {
        loginPanel = new JPanel(new GridBagLayout()); // reset

        JPanel loginBox = new JPanel();

        loginPanel.setBackground(Color.decode(DARKGREY));

        loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.PAGE_AXIS));
        loginBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        invalidLabel.setMinimumSize(new Dimension(100, 50));
        loginButton.setContentAreaFilled(false);
        loginButton.setOpaque(true);
        passwordHide.setToolTipText("Show & hide password...");

        // Reset fields & checkbox
        passwordInput.setText("");
        usernameInput.setText("");
        invalidLabel.setText(" "); // need space for the label to have a fixed height
        passwordHide.setSelected(false);



        BufferedImage img;
        JLabel bannerLabel = new JLabel();
        try {
            img = ImageIO.read(new File("./Images/GUI images/banner.png"));
            BufferedImage newImg = resizeImg(img, 350,62, false);
            bannerLabel = new JLabel(new ImageIcon(newImg));
        } catch (IOException e) {
            bannerLabel.setText("STONK MACHINE");
        }
        JPanel login = new JPanel(new GridBagLayout());
        GridBagConstraints cords = new GridBagConstraints();
        // Position the interactive components (text fields, buttons etc)
        cords.insets =new Insets(5,5,5,5);
        cords.gridy = 1;
        cords.gridx = -1;
        cords.gridwidth = 1;
        login.add(usernameLabel, cords);
        cords.gridx += 2;
        login.add(usernameInput, cords);

        cords.gridy++; // password row is always below username row
        cords.gridx = -1;
        login.add(passwordLabel, cords);
        cords.gridx += 2;
        login.add(passwordInput, cords);
        cords.gridx++;
        login.add(passwordHide,cords);

        cords.gridy++; // login button is always below password row
        cords.gridx = 0;
        cords.gridwidth = 3;
        login.add(loginButton, cords);

        cords.gridy++; // warning message is always below login button
        login.add(invalidLabel, cords);

        // Add panels together
        loginBox.add(bannerLabel);
        loginBox.add(login);
        loginPanel.add(loginBox);

        // Listeners
        listeners.loginActionListener();
        listeners.loginKeyListener();
        listeners.passwordHiddenListener();

        // Boilerplate
        mainFrame.setContentPane(loginPanel);
        mainFrame.revalidate();
    }

    // Shell template (content depends on the parameter)----------------------------------------------------------------
    private void shellPanel(JPanel content, boolean includeWelcomeLabel) {
        shellPanel = new JPanel(); // reset
        shellPanel.setBackground(Color.decode(WHITE));

        homeButton.setToolTipText("Go to the home screen");
        assetsButton.setToolTipText("Go to the search screen");
        adminButton.setToolTipText("Go to admin portal");
        String unitText, creditsText;
        if (user.getUnit() != null) {
            unitText = user.getUnit();
            try {
                creditsText = String.format("Your organisational unit has %d credits and the below holdings:",
                        data.getUnitByKey(user.getUnit()).getCredits());
            } catch (DoesNotExist doesNotExist) {
                doesNotExist.printStackTrace();
                creditsText = "";
            }
        } else {
            unitText = "No organisational unit";
            creditsText = "";
        }
        welcomeLabel.setText(String.format("Welcome back %s! %s", user.getUsername(), creditsText));
        orgUnitLabel.setText(unitText.toUpperCase());

        shellPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        shellPanel.setLayout(new BoxLayout(shellPanel, BoxLayout.PAGE_AXIS));

        JPanel row1 = new JPanel();
        JPanel leftButtons = new JPanel();
        JPanel rightButtons = new JPanel();
        // keeps row 1 fixed in height so no matter what the content is (a table, buttons, graphs)
        // it won't change sizes (sometimes takes up most of the screen!)
        row1.setBackground(Color.decode(WHITE));
        row1.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        row1.setMaximumSize(new Dimension(WIDTH, 50));
        row1.setLayout(new BorderLayout());
        leftButtons.add(homeButton);
        if (user.getAdminAccess()) leftButtons.add(adminButton);
        rightButtons.add(assetsButton);
        row1.add(leftButtons, BorderLayout.WEST);
        row1.add(orgUnitLabel, BorderLayout.CENTER);
        row1.add(rightButtons, BorderLayout.EAST);

        JPanel row2 = new JPanel();
        row2.setBackground(Color.decode(WHITE));
        row2.setLayout(new FlowLayout());
        row2.add(welcomeLabel);

        // The parameter panel is added here, always a panel
        JPanel row3 = new JPanel();
        row3.setBackground(Color.decode(WHITE));
        row3.setPreferredSize(new Dimension(700,400));
        row3.setLayout(new FlowLayout());
        row3.add(content);

        shellPanel.add(row1);
        shellPanel.add(Box.createVerticalStrut(20));
        if (includeWelcomeLabel) {
            shellPanel.add(row2);
            shellPanel.add(Box.createVerticalStrut(20));
        }
        shellPanel.add(row3);

        // Listeners

        // Boilerplate
        mainFrame.setContentPane(shellPanel);
        mainFrame.revalidate();
    }


    /***
     * @author Johnny Madigan & Scott Peachey (wrote checkLogin, home, adminHome, assetPage, loginActionListener,
     * loginKeyListener, passwordHiddenListener, logoutListener, masterUserKeyListener, masterAdminKeyListener,
     * exitListener, homeListener, assetTableListener);
     *
     * Sophia Walsh Long (rewrote home extensively pre-fork and did some post-fork rewrites, wrote all methods not mentioned above,
     * refactored to make this an inner non-static class instead of a static class in its own file)
     */
    private class GuiListeners {

        // Checks login credentials-----------------------------------------------------------------------------------------
        private void checkLogin() {
            // store the user object for the duration of the session (if login was successful)
            String username = usernameInput.getText();
            String password = new String(passwordInput.getPassword());
            try {
                user = data.login(username, password);
                shellPanel(home(), true);
            } catch (DoesNotExist | IllegalString ex) {
                ex.printStackTrace();
                passwordInput.setText("");  // clear password field
                invalidLabel.setForeground(Color.RED);
                invalidLabel.setText("Invalid Login Credentials");
            }
        }

        private void checkPassChange() {
            String username = usernameInput.getText();
            String password = new String(passwordInput.getPassword());
            String newPassword = new String(newPasswordInput.getPassword());
            String newPassword2 = new String(confirmNewPasswordInput.getPassword());
            try {
                data.login(username, password);
                if (!newPassword.equals(newPassword2)) {
                    invalidLabel.setForeground(Color.RED);
                    invalidLabel.setText("New passwords do not match");
                }
                else {
                    user.changePassword(newPassword);
                    data.updateUser(user);
                    passwordInput.setText("");
                    newPasswordInput.setText("");
                    confirmNewPasswordInput.setText("");
                    invalidLabel.setForeground(Color.GREEN);
                    invalidLabel.setText("Password successfully changed!");
                }
            } catch (DoesNotExist ex) {
                ex.printStackTrace();
                invalidLabel.setForeground(Color.RED);
                invalidLabel.setText("Incorrect current password");
            } catch (IllegalString ex) {
                ex.printStackTrace();
                invalidLabel.setForeground(Color.RED);
                invalidLabel.setText("Password may not contain whitespace");
            }
        }

        // Panels with the content to be passed into the shell template-----------------------------------------------------

        // Puts the user home content in the shell panel
        JPanel home() throws DoesNotExist {
            // Table Panel
            userHome = new JPanel();
            userHome.setPreferredSize(new Dimension(600,275));
            // Table Data
            String[] columnNames = { "Asset ID", "Description", "Qty", "$ Current"};
            String[][] data = new String[0][4];
            //For every inventory record of the logged-in user's org unit...
            if (user.getUnit() != null) data = populateTable(TradingAppGUI.this.data.getInventoriesByOrgUnit(user.getUnit()));
            //Make cells not editable
            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells false
                    return false;
                }
            };
            userHoldings = new JTable(data, columnNames);
            userHoldings.setModel(tableModel);
            JScrollPane scrollPane = new JScrollPane(userHoldings);
            scrollPane.setPreferredSize(new Dimension(600,270));
            // Set column widths
            userHoldings.getColumnModel().getColumn(0).setWidth(25);
            userHoldings.getColumnModel().getColumn(1).setPreferredWidth(400);
            userHoldings.getColumnModel().getColumn(2).setWidth(20);
            userHoldings.getColumnModel().getColumn(2).setWidth(30);
            userHome.setBackground(Color.decode(DARKGREY));
            userHome.add(scrollPane);

            assetTableListener();
            return userHome;
        }

        // Puts the admin home content in the shell panel
        JPanel adminHome() {
            adminHome = new JPanel();
            adminHome.setLayout(new BoxLayout(adminHome, BoxLayout.PAGE_AXIS));
            GuiSearch userSearch = new GuiSearch(data.getAllUsernames());
            GuiSearch unitSearch = new GuiSearch(data.getAllUnitNames());
            GuiSearch assetSearch = new GuiSearch(data.getAllAssetStrings());
            adminHome.add(adminActionRow(new JButton[]{newUserButton, editUserButton, userListButton}, userSearch));
            adminHome.add(adminActionRow(new JButton[]{newUnitButton, editUnitButton, unitHoldingsButton}, unitSearch));
            adminHome.add(adminActionRow(new JButton[]{newAssetButton,editAssetButton,assetHoldingsButton}, assetSearch));
            return adminHome;
        }

        // Puts the asset page content in the shell panel
        JPanel assetPage(int asset) {
            assetPage = new JPanel(new GridBagLayout());
            assetPage.setPreferredSize(new Dimension(600,325));
            assetPage.setBackground(Color.decode(DARKGREY));
            assetPage.setLayout(new BorderLayout());

            // Position the interactive components (text fields, buttons etc)
            JPanel intervalButtons = new JPanel();
            intervalButtons.add(daysButton);
            intervalButtons.add(weeksButton);
            intervalButtons.add(monthsButton);
            intervalButtons.add(yearsButton);
            intervalButtonListeners(asset);
            assetPage.add(intervalButtons, BorderLayout.NORTH);

            JPanel graphPanel = new JPanel();

            JPanel orderButtons = new JPanel();
            orderButtons.add(buyButton);
            orderButtons.add(sellButton);
            assetPage.add(orderButtons, BorderLayout.SOUTH);

            return assetPage;
        }

        JPanel changePasswordPage() {
            changePassPanel = new JPanel();
            invalidLabel.setMinimumSize(new Dimension(100, 50));
            passwordHide.setToolTipText("Show & hide password...");

            // Reset fields & checkbox
            passwordInput.setText("");
            newPasswordInput.setText("");
            confirmNewPasswordInput.setText("");
            usernameInput.setText(user.getUsername());
            usernameInput.setEnabled(false);
            invalidLabel.setText(" "); // need space for the label to have a fixed height
            passwordHide.setSelected(false);
            JPanel login = new JPanel(new GridBagLayout());
            GridBagConstraints cords = new GridBagConstraints();
            // Position the interactive components (text fields, buttons etc)
            cords.insets =new Insets(5,5,5,5);
            cords.gridy = 1;
            cords.gridx = -1;
            cords.gridwidth = 1;
            login.add(usernameLabel, cords);
            cords.gridx += 2;
            login.add(usernameInput, cords);

            cords.gridy++; // password row is always below username row
            cords.gridx = -1;
            login.add(passwordLabel, cords);
            cords.gridx += 2;
            login.add(passwordInput, cords);
            cords.gridx++;
            login.add(passwordHide,cords);

            cords.gridy++; // new password row is always below password row
            cords.gridx = -1;
            login.add(newPasswordLabel, cords);
            cords.gridx += 2;
            login.add(newPasswordInput, cords);

            cords.gridy++; // confirm row is always below new password row
            cords.gridx = -1;
            login.add(confirmNewPasswordLabel, cords);
            cords.gridx += 2;
            login.add(confirmNewPasswordInput, cords);

            cords.gridy++; // button is always below the last row
            cords.gridx = 0;
            cords.gridwidth = 3;
            login.add(changePassButton, cords);

            cords.gridy++; // warning message is always below login button
            login.add(invalidLabel, cords);

            changePassPanel.add(login);

            changePassActionListener();
            changePassKeyListener();
            passwordHiddenListener();

            return changePassPanel;
        }

        JPanel userListPage() {
            return new JPanel();
        }

        JPanel assetListPage() {
            return new JPanel();
        }

        JPanel inventoryListPage() {
            return new JPanel();
        }

        JPanel orderListPage() {
            return new JPanel();
        }

        JPanel editUserPage(boolean isCreate) {
            return new JPanel();
        }

        JPanel editAssetPage(boolean isCreate) {
            return new JPanel();
        }

        JPanel editUnitPage(boolean isCreate) {
            return new JPanel();
        }

        JPanel editInventoryPage() {
            return new JPanel();
        }

        JPanel editOrderPage(boolean isCreate, boolean isBuy) {
            return new JPanel();
        }



        // Helpers------------------------------------------
        String[][] populateTable(ArrayList<InventoryRecord> a) throws DoesNotExist {
            String[][] data = new String[0][4];
            for (InventoryRecord inventoryRecord : a) {
                //Get information on the asset of the inventory record
                int assetID = inventoryRecord.getAssetID();
                Asset asset = TradingAppGUI.this.data.getAssetByKey(assetID);
                //Get information on the resolved BuyOrders of this asset (i.e. price history)
                ArrayList<BuyOrder> assetPriceHistory = TradingAppGUI.this.data.getResolvedBuysByAsset(assetID);
                Optional<BuyOrder> mostRecentSale = assetPriceHistory.stream().max(BuyOrder::compareTo);
                AtomicInteger recentPrice = new AtomicInteger();
                mostRecentSale.ifPresent(buyOrder -> recentPrice.set(buyOrder.getPrice()));
                //recentPrice now contains the most recent price of the asset, or 0 if the asset has never been sold
                String[] dataNew = new String[]{ asset.getIdString(), asset.getDescription(),
                        String.valueOf(inventoryRecord.getQuantity()), "$" + recentPrice.get()};
                data = Arrays.copyOf(data, data.length + 1);
                data[data.length - 1] = dataNew;
            }
            return data;
        }

        JPanel adminActionRow(JButton[] buttons, GuiSearch search) {
            JPanel result = new JPanel();
            for (JButton b : buttons) {
                result.add(b);
            }
            result.add(search);
            return result;
        }

        // Login portal/password change listeners-------------------------------------------------------------------------------------------

        // Triggers checkLogin when the 'Login' button is pressed
        void loginActionListener() {
            loginButton.addActionListener(e -> checkLogin());
        }

        // Triggers checkLogin when the 'Enter' key is pressed
        void loginKeyListener() {
            passwordInput.addKeyListener(new KeyListener() {
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

        void changePassActionListener() {
            changePassButton.addActionListener(e->checkPassChange());
        }

        void changePassKeyListener() {
            confirmNewPasswordInput.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {}

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        checkPassChange();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {}
            });
        }

        // Shows/hides password when the radio button passwordHidden is toggled
        void passwordHiddenListener() {
            passwordHide.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    passwordInput.setEchoChar((char) 0);
                } else {
                    passwordInput.setEchoChar('\u2022');
                }
            });
        }

        // Menu bar listeners-----------------------------------------------------------------------------------------------

        // Logs out the user when 'Logout' is clicked in the File menu
        void logoutListener() {
            logoutMenu.addActionListener(e -> {
                try {
                    user = null; // reset session user data
                    loginPanel(); // creates & shows login portal
                } catch (AlreadyExists | IllegalString ex) {
                    ex.printStackTrace();
                }
            });
        }

        // Dev master key to bypass login for testing (goes straight to user home)
        void masterUserKeyListener() {
            masterUserKey.addActionListener(e -> {
                user = TradingAppData.userDev;
                try {
                    shellPanel(home(), true);
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            });
        }

        // Dev master key to bypass login for testing (goes straight to admin home)
        void masterAdminKeyListener() {
            masterAdminKey.addActionListener(e -> {
                user = TradingAppData.adminDev;
                try {
                    shellPanel(home(), true);
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            });
        }

        // Closes mainFrame and stops Main when 'Exit' is clicked in the File menu
        public void exitListener() {
            exitMenu.addActionListener(ev -> System.exit(0));
        }

        public void changePassMenuListener() {
            changePassword.addActionListener(e->{
                if (TradingAppGUI.this.user != null) {
                    shellPanel(changePasswordPage(), false);
                }
            });
        }

        //Other listeners

        void homeListener() {
            homeButton.addActionListener(e -> {
                orgUnitLabel.setText("ORG UNIT HERE");
                try {
                    shellPanel(home(), true);
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            });
        }

        void adminListener() {
            adminButton.addActionListener(e->{
                shellPanel(adminHome(), false);
                orgUnitLabel.setText("ADMIN PORTAL");
            });
        }

        void assetTableListener() {
            userHoldings.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = userHoldings.rowAtPoint(e.getPoint());
                    String asset = userHoldings.getValueAt(row, 1).toString();
                    shellPanel(assetPage(Integer.parseInt(userHoldings.getValueAt(row, 0).toString())),
                            false);
                    orgUnitLabel.setText(asset.toUpperCase());
                    userHome.remove(welcomeLabel);
                    System.out.println(asset);
                }
            });
        }

        void intervalButtonListeners(int asset) {
            daysButton.addActionListener(e->{
                try {
                    data.getHistoricalPrices(asset, TradingAppData.Intervals.DAYS);
                } catch (InvalidDate | DoesNotExist invalidDate) {
                    invalidDate.printStackTrace();
                }
            });
            weeksButton.addActionListener(e->{
                try {
                    data.getHistoricalPrices(asset, TradingAppData.Intervals.WEEKS);
                } catch (InvalidDate | DoesNotExist invalidDate) {
                    invalidDate.printStackTrace();
                }
            });
            monthsButton.addActionListener(e->{
                try {
                    data.getHistoricalPrices(asset, TradingAppData.Intervals.MONTHS);
                } catch (InvalidDate | DoesNotExist invalidDate) {
                    invalidDate.printStackTrace();
                }
            });
            yearsButton.addActionListener(e->{
                try {
                    data.getHistoricalPrices(asset, TradingAppData.Intervals.YEARS);
                } catch (InvalidDate | DoesNotExist invalidDate) {
                    invalidDate.printStackTrace();
                }
            });
        }

        void adminCreateButtonListeners() {
            newUserButton.addActionListener(e->{
                shellPanel(editUserPage(true), false);
            });
            newUnitButton.addActionListener(e->{
                shellPanel(editUnitPage(true), false);
            });
            newAssetButton.addActionListener(e->{
                shellPanel(editAssetPage(true), false);
            });
        }
    }
}
