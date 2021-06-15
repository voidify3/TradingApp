package ClientSide;

// class imports

import common.*;
import common.Exceptions.*;

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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;

/***
 * @author Johnny Madigan & Scott Peachey (wrote createAndShowGui, menuBar, resizeImg, loginPanel, shellPanel);
 * Sophia Walsh Long (all other code and the fact that the members aren't static;
 * above methods have also been refactored)
 */
class NewTradingAppGUI {

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
    JLabel topLabel = new JLabel("ORG UNIT HERE", SwingConstants.CENTER);

    // Menu bar & widgets
    JMenuBar menuBar = new JMenuBar();
    JMenuItem logoutMenu = new JMenuItem("Logout");
    JMenuItem exitMenu = new JMenuItem("Exit");
    JMenuItem changePassword = new JMenuItem("Change password");
    JMenuItem masterUserKey = new JMenuItem("Master User Key");
    JMenuItem masterAdminKey = new JMenuItem("Master Admin Key");



    public NewTradingAppGUI(TradingAppData data) {
        this.data = data;
    }

    String[][] populateTable(ArrayList<InventoryRecord> a, boolean isHomepage) throws DoesNotExist {
        String[][] info = new String[0][4];
        for (InventoryRecord inventoryRecord : a) {
            //Get information on the asset of the inventory record
            int assetID = inventoryRecord.getAssetID();
            String[] infoNew;
            Asset asset = data.getAssetByKey(assetID);
            if (isHomepage){

                //Get information on the resolved BuyOrders of this asset (i.e. price history)
                ArrayList<BuyOrder> assetPriceHistory = data.getResolvedBuysByAsset(assetID);
                Optional<BuyOrder> mostRecentSale = assetPriceHistory.stream().max(BuyOrder::compareTo);
                AtomicInteger recentPrice = new AtomicInteger();
                mostRecentSale.ifPresent(buyOrder -> recentPrice.set(buyOrder.getPrice()));
                //recentPrice now contains the most recent price of the asset, or 0 if the asset has never been sold
                infoNew = new String[]{asset.getIdString(), asset.getDescription(),
                        String.valueOf(inventoryRecord.getQuantity()), "$" + recentPrice.get()};
            }
            else {
                infoNew = new String[]{inventoryRecord.getUnitName(), asset.getIdString(),
                        asset.getDescription(), String.valueOf(inventoryRecord.getQuantity())};
            }
            info = Arrays.copyOf(info, info.length + 1);
            info[info.length - 1] = infoNew;
        }
        return info;
    }
    String[][] populateOrderTable(ArrayList<Order> a) {
        String[][] info = new String[0][7];
        for (Order o : a) {
            String[] infoNew = new String[]{String.valueOf(o.getId()), o.getUnit(), String.valueOf(o.getAsset()),
                    String.valueOf(o.getQty()), String.valueOf(o.getPrice()), o.getDatePlaced().toString(),
                    (o.getDateResolved() == null? "N/A" : o.getDateResolved().toString())};
            info = Arrays.copyOf(info, info.length + 1);
            info[info.length - 1] = infoNew;
        }

        return info;
    }
    String[][] populateTable(ArrayList<Asset> a) throws DoesNotExist {
        String[][] info = new String[0][3];
        for (Asset x : a) {
            ArrayList<BuyOrder> assetPriceHistory = data.getResolvedBuysByAsset(x.getId());
            Optional<BuyOrder> mostRecentSale = assetPriceHistory.stream().max(BuyOrder::compareTo);
            AtomicInteger recentPrice = new AtomicInteger();
            mostRecentSale.ifPresent(buyOrder -> recentPrice.set(buyOrder.getPrice()));
            String[] infoNew = new String[]{x.getIdString(), x.getDescription(), "$" + recentPrice.get()};
            info = Arrays.copyOf(info, info.length + 1);
            info[info.length - 1] = infoNew;
        }

        return info;
    }
    //TODO: add polymorphism for other possibiltiies

    void createAndShowGUI() throws IllegalString, AlreadyExists, IOException, DoesNotExist, InvalidDate, InvalidAmount, OrderException {
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
        listeners.ordersListener();

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
    private void loginPanel() {
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
        GridBagConstraints cords = new GridBagConstraints(-1, 1, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
        // Position the interactive components (text fields, buttons etc)
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
        try { //entire thing needs to be this
            user = data.getUserByKey(user.getUsername());
            homeButton.setToolTipText("Go to the home screen");
            assetsButton.setToolTipText("Go to the search screen");
            adminButton.setToolTipText("Go to admin portal");
            ordersButton.setToolTipText("View my orders");
            String unitText, creditsText;
            if (user.getUnit() != null) {
                unitText = user.getUnit();
                try {
                    creditsText = String.format("Your organisational unit has %d credits and the below holdings:",
                            data.getUnitByKey(user.getUnit()).getCredits());
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                    displayError("Unexpected error: " + doesNotExist.getMessage(),
                            "Either something went wrong internally, or an admin deleted your organisational" +
                                    "unit while this page was loading. Use Help > Refresh User Data to fix the problem");
                    creditsText = "";
                }
            } else {
                unitText = "No organisational unit";
                creditsText = "";
            }
            welcomeLabel.setText(String.format("Welcome back %s! %s", user.getUsername(), creditsText));
            topLabel.setText(unitText.toUpperCase());

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
            rightButtons.add(ordersButton);
            row1.add(leftButtons, BorderLayout.WEST);
            row1.add(topLabel, BorderLayout.CENTER);
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
        } catch (DoesNotExist doesNotExist) {
            doesNotExist.printStackTrace();
            displayError("Unexpected error: " + doesNotExist.getMessage(),
                    "Either something went wrong internally, or an admin deleted your account while" +
                            "this page was loading. Logging you out...");
        }
    }
    private void shellPanel(JPanel content, boolean includeWelcomeLabel, String unitLabel) {
        shellPanel(content, includeWelcomeLabel);
        topLabel.setText(unitLabel);
    }

    final String wrapDialog ="<html><body><p style='width: 200px;'>%s</p></body></html>";
    void messageDialogFormatted(String title, String message, int type) {
        JOptionPane.showMessageDialog(this.mainFrame,
                String.format(wrapDialog, message),
                title, type);
    }
    void displayError(String title, String message) {
        messageDialogFormatted(title, message, JOptionPane.ERROR_MESSAGE);
    }
    int displayConfirm(String title, String message) {
        return JOptionPane.showConfirmDialog(NewTradingAppGUI.this.mainFrame, String.format(wrapDialog, message),
                title, JOptionPane.YES_NO_OPTION);
    }

    void displayFeedback(String title, String message) {
        messageDialogFormatted(title, message, JOptionPane.INFORMATION_MESSAGE);
        //JOptionPane.showMessageDialog(TradingAppGUI.this.mainFrame,message, title, JOptionPane.PLAIN_MESSAGE);
    }

    //Helper to throw NotAuthorised
    void failIfNotAdmin(String thingAttempted) throws NotAuthorised {
        if (!user.getAdminAccess()) throw new NotAuthorised("You do not have permission to: " + thingAttempted);
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

        private void doLogin() throws DoesNotExist {
            shellPanel(new HomePage(), true);
        }

        private void doLogout() throws AlreadyExists, IllegalString {
            user = null; // reset session user data
            loginPanel(); // creates & shows login portal
        }

        // Checks login credentials-----------------------------------------------------------------------------------------
        private void checkLogin() {
            // store the user object for the duration of the session (if login was successful)
            String username = usernameInput.getText();
            String password = new String(passwordInput.getPassword());
            try {
                user = data.login(username, password);
                shellPanel(new HomePage(), true);
            } catch (DoesNotExist | IllegalString ex) {
                ex.printStackTrace();
                passwordInput.setText("");  // clear password field
                invalidLabel.setForeground(Color.RED);
                invalidLabel.setText("Invalid Login Credentials");
            }
        }

        private void checkPassChange() {
            int i = displayConfirm("Confirm password change", "Click yes to confirm password change");
            if (i == JOptionPane.YES_OPTION){
                String username = usernameInput.getText();
                String password = new String(passwordInput.getPassword());
                String newPassword = new String(newPasswordInput.getPassword());
                String newPassword2 = new String(confirmNewPasswordInput.getPassword());
                try {
                    data.login(username, password);
                    if (!newPassword.equals(newPassword2)) {
                        invalidLabel.setForeground(Color.RED);
                        invalidLabel.setText("New passwords do not match");
                    } else {
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
                } catch (ConstraintException e) {
                    displayError("Unexpected error: ", e.getMessage());
                }
            }
        }

        // Panels with the content to be passed into the shell template-----------------------------------------------------


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
            GridBagConstraints cords = new GridBagConstraints(-1, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
            // Position the interactive components (text fields, buttons etc)
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
                    doLogout();
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
                    doLogin();
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
                    doLogin();
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
                if (NewTradingAppGUI.this.user != null) {
                    shellPanel(changePasswordPage(), false);
                }
            });
        }

        //Other listeners

        void ordersListener() {
            ordersButton.addActionListener(e->{
                shellPanel(new OrdersPage(true, true, false), false);
            });
        }
        void homeListener() {
            homeButton.addActionListener(e -> {
                topLabel.setText("ORG UNIT HERE");
                try {
                    shellPanel(new HomePage(), true);
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            });
        }

        void adminListener() {
            adminButton.addActionListener(e->{
                shellPanel(new AdminPortal(), false);
                topLabel.setText("ADMIN PORTAL");
            });
        }
    }
    class AssetInfoPage extends JPanel implements ActionListener {
        int asset;
        JButton daysButton = new JButton("Days");
        JButton weeksButton = new JButton("Weeks");
        JButton monthsButton = new JButton("Months");
        JButton yearsButton = new JButton("Years");
        JButton buyButton = new JButton("Buy Asset");
        JButton sellButton = new JButton("Sell Asset");
        public AssetInfoPage(int asset) {
            this.asset=asset;
            setPreferredSize(new Dimension(600,325));
            setBackground(Color.decode(DARKGREY));
            setLayout(new BorderLayout());

            // Position the interactive components (text fields, buttons etc)
            JPanel intervalButtons = new JPanel();
            intervalButtons.add(daysButton);
            intervalButtons.add(weeksButton);
            intervalButtons.add(monthsButton);
            intervalButtons.add(yearsButton);
            daysButton.addActionListener(this);
            weeksButton.addActionListener(this);
            monthsButton.addActionListener(this);
            yearsButton.addActionListener(this);
            buyButton.addActionListener(this);
            sellButton.addActionListener(this);
            add(intervalButtons, BorderLayout.NORTH);

            JPanel graphPanel = new JPanel();
            add(graphPanel, BorderLayout.CENTER);

            JPanel orderButtons = new JPanel();
            orderButtons.add(buyButton);
            orderButtons.add(sellButton);
            add(orderButtons, BorderLayout.SOUTH);
        }

        void tryGetHistoricalPrices(TradingAppData.Intervals intervals) {
            try {
                data.getHistoricalPrices(asset, intervals);
            } catch (InvalidDate | DoesNotExist invalidDate) {
                displayError("An error occurred while displaying prices", invalidDate.getMessage());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == daysButton) tryGetHistoricalPrices(TradingAppData.Intervals.DAYS);
            else if (e.getSource() == weeksButton) tryGetHistoricalPrices(TradingAppData.Intervals.WEEKS);
            else if (e.getSource() == monthsButton) tryGetHistoricalPrices(TradingAppData.Intervals.MONTHS);
            else if (e.getSource() == yearsButton) tryGetHistoricalPrices(TradingAppData.Intervals.YEARS);
            else if (e.getSource() == buyButton) {
                try {
                    shellPanel(new OrderFormPage(asset, true, true), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Unexpected error", doesNotExist.getMessage());
                }
            }
            else if (e.getSource() == sellButton) {
                try {
                    shellPanel(new OrderFormPage(asset, true, false), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Unexpected error", doesNotExist.getMessage());
                }
            }
        }
    }
    abstract class TablePage extends JPanel implements MouseListener {
        JTable table;
        JScrollPane scrollPane;
        TablePage(String[] columnNames, String[][] info, int[] widths, boolean[] isPreferred) {
            if (columnNames.length != widths.length || columnNames.length != isPreferred.length ||
                    (info.length > 0 && columnNames.length != info[0].length)) {
                throw new IllegalArgumentException("Cannot load table with mismatched arguments");
            }
            setPreferredSize(new Dimension(600,275));
            DefaultTableModel tableModel = new DefaultTableModel(info, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells false
                    return false;
                }
            };
            table = new JTable(info,columnNames);
            table.setModel(tableModel);
            scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600,270));
            this.setBackground(Color.decode(DARKGREY));
            this.add(scrollPane);
            table.addMouseListener(this);
        }

        abstract void view(String key, String col2);
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            String key = table.getModel().getValueAt(row, 0).toString();
            view(key, table.getModel().getValueAt(row, 1).toString());
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
    class HomePage extends TablePage {

        private HomePage(String[][] info) {
            super(new String[]{"Asset ID", "Description", "Qty", "$ Current"}, info,
                    new int[]{25, 400, 20, 30}, new boolean[]{false, true, false, false});
        }
        HomePage() throws DoesNotExist {
            this((user.getUnit() == null? new String[0][4] :
                    populateTable(data.getInventoriesByOrgUnit(user.getUnit()), true)));
        }

        @Override
        void view(String key, String col2) {
            shellPanel(new AssetInfoPage(parseInt(key)),
                    false);
            topLabel.setText(col2.toUpperCase());
        }
    }
    class OrdersPage extends TablePage {
        private OrdersPage(String[][] info) {
            super(new String[]{"ID", "Unit", "Asset", "Price", "Quantity", "Placed", "Resolved"}, info,
                    new int[]{50,50,50,50,50,50,50}, new boolean[7]);
        }
        OrdersPage(boolean justMine, boolean isBuy, boolean resolvedFlag) {
            this(populateOrderTable(data.getOrdersForTable(user.getUnit(), justMine,isBuy,resolvedFlag)));
            //do more things
        }
        @Override
        void view(String key, String col2) {

        }
    }
    class AssetListPage extends TablePage {

        AssetListPage( String[][] info) {
            super(new String[]{"Asset ID", "Description", "$ Current"}, info,
                    new int[]{25, 400, 50}, new boolean[]{false, true, false});
        }
        AssetListPage() throws DoesNotExist {
            this(populateTable(data.getAllAssets()));
        }

        @Override
        void view(String key, String col2) {
            shellPanel(new AssetInfoPage(parseInt(key)),
                    false);
            topLabel.setText(col2.toUpperCase());
        }
    }
    class InventoryListPage extends TablePage {

        InventoryListPage(String[][] info) {
            super(new String[]{"Organisational unit", "Asset ID", "Asset description", "Quantity"}, info,
                    new int[]{50, 25, 380, 20}, new boolean[]{false, true, false});
        }
        InventoryListPage() throws DoesNotExist {
            this(populateTable(data.getAllInventories(), false));
        }
        InventoryListPage(String unit) throws DoesNotExist {
            this(populateTable(data.getInventoriesByOrgUnit(unit), false));
        }
        InventoryListPage(int asset) throws DoesNotExist {
            this(populateTable(data.getInventoriesByAsset(asset), false));
        }

        @Override
        void view(String key, String col2) {
            //open an input dialog
        }
    }
    abstract class FormPage extends JPanel implements ActionListener {
        String deletePromptText;
        JButton saveEditButton = new JButton("Save");
        JButton saveCreateButton = new JButton("Create");
        JButton cancelEditButton = new JButton("Discard");
        JButton deleteButton = new JButton("Delete");
        JPanel buttonRow = new JPanel();
        FormPage(boolean isCreate) {
            this.setLayout(new GridLayout(0,1));
            for (JPanel j : generateRows()) this.add(j);
            cancelEditButton.addActionListener(this);
            buttonRow.add(cancelEditButton);
            if (isCreate) {
                saveCreateButton.addActionListener(this);
                buttonRow.add(saveCreateButton);}
            else {
                saveEditButton.addActionListener(this);
                buttonRow.add(saveEditButton);
                deleteButton.addActionListener(this);
                buttonRow.add(deleteButton);
            }
            this.add(buttonRow);
        }
        abstract JPanel[] generateRows();
        abstract void save();
        abstract void create();
        abstract void delete();
        void cancel() {
            if (displayConfirm("Return to portal?", "Changes will be discarded")
                    == JOptionPane.YES_OPTION) exitToPortal();
        }
        void exitToPortal(){shellPanel(new AdminPortal(),false);}
        void notAuthorisedDialog(String message) {
            displayError("Unexpected error: " + message,
                    "Another admin may have revoked your admin access");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveEditButton) save();
            else if (e.getSource()==saveCreateButton) create();
            else if (e.getSource()==deleteButton) {
                if (displayConfirm("Confirm deletion",  deletePromptText) == JOptionPane.YES_OPTION) delete();}
            else if (e.getSource()==cancelEditButton) {
                cancel();
            }
        }
    }
    class AssetFormPage extends FormPage {
        JLabel infoLabel;
        JLabel numberKeyLabel;
        JTextField stringField;
        Asset old;
        AssetFormPage(String seed) throws DoesNotExist {
            super((seed == null));
            if (seed != null) {
                int i = seed.indexOf('(');
                if (i<1) throw new NumberFormatException();
                int id = parseInt(seed.substring(0, i - 1));
                old = data.getAssetByKey(id);
                stringField.setText(old.getDescription());
                numberKeyLabel.setText(String.valueOf(id));
                deletePromptText = "Are you sure you want to delete asset "
                        + numberKeyLabel.getText() + "?<br/>If the deletion succeeds, it will permanently delete all " +
                        "information on holdings of the asset, and all buy and sell orders for the asset. " +
                        "<br/>Please note that the orders will be deleted, not cancelled; i.e. credits held in outstanding " +
                        "buy orders will NOT be returned to the appropriate organisational units by this operation. " +
                        "Manually cancel the orders before deleting this asset to return the credits. ";
            }
        }
        void initialiseFields() {
            infoLabel = new JLabel("Editing asset: ");
            numberKeyLabel = new JLabel("NEW ASSET");
            stringField = new JTextField(30);
        }
        @Override
        JPanel[] generateRows() {
            initialiseFields();
            JPanel[] output = {new JPanel(), new JPanel()};
            output[0].add(infoLabel);
            output[0].add(numberKeyLabel);
            output[1].add(new JLabel("Description:"));
            output[1].add(stringField);
            return output;
        }
        @Override
        void save() {
            String description = stringField.getText();
            try {
                failIfNotAdmin("Edit an asset's description");
                new Asset(description); //for the check
                Asset toSend = new Asset(parseInt(numberKeyLabel.getText()), description);
                data.updateAsset(toSend);
                displayFeedback("Asset successfully updated", "Click OK to return to admin portal");
                exitToPortal();
            } catch (IllegalString illegalString) {
                displayError("Invalid description", illegalString.getMessage());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error: " + doesNotExist.getMessage(),
                        "Someone else probably deleted the asset while you were editing it.<br/>" +
                                "Return to the admin portal and try again");
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            }
        }
        @Override
        void create() {
            String description = stringField.getText();
            try {
                failIfNotAdmin("Create a new asset");
                Asset toSend = new Asset(description);
                data.addAsset(toSend);
                displayFeedback("Asset successfully created", "Click OK to return to admin portal");
                exitToPortal();
            } catch (IllegalString illegalString) {
                displayError("Invalid description", illegalString.getMessage());
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (AlreadyExists alreadyExists) {
                displayError("Unexpected error: " + alreadyExists.getMessage(),
                        "This error should never happen; try again");
            }
        }
        @Override
        void delete() {

            try {
                failIfNotAdmin("Delete an asset");
                data.deleteAsset(Integer.parseInt(numberKeyLabel.getText()));
                displayFeedback("Asset successfully deleted", "Click OK to return to admin portal");
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error: " + doesNotExist.getMessage(),
                        "Another admin may have deleted the asset.");
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } finally {
                exitToPortal();
            }
        }
    }
    class UserFormPage extends FormPage {
        public static final String NO_UNIT = "[NO UNIT]";
        JLabel infoLabel;
        JLabel passwordLabel;
        JTextField usernameField;
        JTextField passwordChanger;
        JCheckBox adminAccessCheckbox;
        GuiSearch unitDropdown;
        User old;
        User edited;
        UserFormPage(String seed) throws DoesNotExist {
            super((seed == null));
            if (seed != null) {
                deletePromptText = "Are you sure you want to delete "
                        + seed + "?<br/>This operation cannot be undone.";
                old = data.getUserByKey(seed);
                infoLabel.setText("Editing user with username:");
                passwordLabel.setText("New password (leave blank to keep old password):");
                usernameField.setText(seed);
                usernameField.setEnabled(false);
                adminAccessCheckbox.setSelected(old.getAdminAccess());
                if (old.getUnit() == null) unitDropdown.setSelectedItem(NO_UNIT);
                else unitDropdown.setSelectedItem(old.getUnit());
                edited = new User(old.getUsername(), old.getPassword(),old.getSalt(),old.getUnit(),old.getAdminAccess());
            }
        }

        void initialiseFields() {
            infoLabel = new JLabel("Creating new user with username:");
            passwordLabel = new JLabel("Initial password:");
            usernameField = new JTextField(30);
            passwordChanger = new JTextField(30);
            adminAccessCheckbox = new JCheckBox();
            ArrayList<String> units = data.getAllUnitNames();
            units.add(NO_UNIT);
            unitDropdown = new GuiSearch(units);
        }

        @Override
        JPanel[] generateRows() {
            initialiseFields();
            JPanel[] output = {new JPanel(), new JPanel(), new JPanel(), new JPanel()};
            output[0].add(infoLabel);
            output[0].add(usernameField);
            output[1].add(passwordLabel);
            output[1].add(passwordChanger);
            output[2].add(new JLabel("Enable admin access"));
            output[2].add(adminAccessCheckbox);
            output[3].add(new JLabel("Assign to organisational unit:"));
            output[3].add(unitDropdown);
            return output;
        }

        @Override
        void save() {
            try {
                failIfNotAdmin("Change another user's password, access level, or organisational unit");
                if (passwordChanger.getText() != null && !passwordChanger.getText().equals("")) {
                    edited.changePassword(passwordChanger.getText());
                }
                edited.setAdminAccess(adminAccessCheckbox.isSelected());
                String unit = (String)unitDropdown.getSelectedItem();
                if (Objects.equals(unit, NO_UNIT)) unit=null;
                edited.setUnit(unit);
                data.updateUser(edited);
                displayFeedback("User successfully updated", "Click OK to return to admin portal");
                exitToPortal();
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (IllegalString illegalString) {
                displayError("Invalid password", illegalString.getMessage());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error: " + doesNotExist.getMessage(),
                        "Another admin may have deleted the user.");
            } catch (ConstraintException e) {
                displayError("Update failed for technical reasons", e.getMessage());
            }
        }

        @Override
        void create() {
            try {
                failIfNotAdmin("Create a new user");
                String unit = (String)unitDropdown.getSelectedItem();
                if (unit != null && unit.equals(NO_UNIT)) unit=null;
                edited = new User(usernameField.getText(),passwordChanger.getText(),adminAccessCheckbox.isSelected(), unit);
                data.addUser(edited);
                displayFeedback("User successfully created", "Click OK to return to admin portal");
                exitToPortal();
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (IllegalString illegalString) {
                displayError("Invalid password", illegalString.getMessage());
            } catch (DoesNotExist | AlreadyExists doesNotExist) {
                displayError("User could not be created", doesNotExist.getMessage());
            }
        }

        @Override
        void delete() {
            try {
                failIfNotAdmin("Delete a user");
                data.deleteUser(old.getUsername());
                displayFeedback("User successfully deleted", "Click OK to return to admin portal");
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error: " + doesNotExist.getMessage(),
                        "Another admin may have deleted the user.");
            }
            finally {
                exitToPortal();
            }
        }
    }
    class UnitFormPage extends FormPage {
        JLabel infoLabel;
        JTextField nameField;
        JSpinner creditsInput;
        OrgUnit old;
        UnitFormPage(String seed) throws DoesNotExist {
            super(seed==null);
            if (seed != null) {
                old = data.getUnitByKey(seed);
                infoLabel.setText("Editing unit with name:");
                nameField.setText(seed);
                deletePromptText = "Are you sure you want to delete organisational unit "
                        + seed + "?<br/>This cannot be undone, and will also permanently delete all " +
                        "information on the unit's holdings, buy and sell orders placed by the unit, " +
                        "and past resolved transactions involving the unit. " +
                        "Users will not be deleted; they will only be removed from the unit.";
                nameField.setEnabled(false);
                creditsInput.setValue(old.getCredits());}
        }

        void initialiseFields() {
            infoLabel = new JLabel("Creating new unit with name:");
            nameField = new JTextField(30);
            creditsInput = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
        }

        @Override
        JPanel[] generateRows() {
            initialiseFields();
            JPanel[] output = {new JPanel(), new JPanel()};
            output[0].add(infoLabel);
            output[0].add(nameField);
            output[1].add(new JLabel("Credits:"));
            output[1].add(creditsInput);
            return output;
        }

        @Override
        void save() {
            try {
                failIfNotAdmin("Change an organisational unit's credit balance");
                data.updateUnit(new OrgUnit(old.getName(), (Integer) creditsInput.getValue()));
                displayFeedback("Unit successfully updated", "Click OK to return to admin portal");
                exitToPortal();
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (IllegalString illegalString) {
                displayError("Unexpected error", illegalString.getMessage());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error: " + doesNotExist.getMessage(),
                        "Another admin may have deleted the unit.");
            }
        }


        @Override
        void create() {
            try {
                failIfNotAdmin("Create an organisational unit");
                data.addUnit(new OrgUnit(old.getName(), (Integer) creditsInput.getValue()));
                displayFeedback("Unit successfully created", "Click OK to return to admin portal");
                exitToPortal();
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (IllegalString illegalString) {
                displayError("Invalid name", illegalString.getMessage());
            } catch (AlreadyExists doesNotExist) {
                displayError("Unit could not be created", doesNotExist.getMessage());
            }
        }

        @Override
        void delete() {
            try {
                failIfNotAdmin("Delete an organisational unit");
                data.deleteUnit(old.getName());
                displayFeedback("Unit successfully deleted", "Click OK to return to admin portal");
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error: " + doesNotExist.getMessage(),
                        "Another admin may have deleted the unit.");
            } finally {
                exitToPortal();
            }
        }
    }
    class OrderFormPage extends FormPage {
        int id; //asset id if creating, order id if viewing existing order
        Order o;
        boolean isBuy;
        JLabel infoLabel = new JLabel();
        JLabel extraInfoLabel = new JLabel();
        JLabel numberKeyLabel = new JLabel();
        JSpinner quantityInput = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
        JSpinner priceInput = new JSpinner(new SpinnerNumberModel(1,1,null,1));
        OrderFormPage(int id, boolean isCreate, boolean isBuy) throws DoesNotExist {
            super(isCreate);
            this.isBuy = isBuy;
            String typeText = isBuy?"buy":"sell";
            infoLabel.setText(String.format("Placing new %s order for asset: ", typeText));
            numberKeyLabel.setText(String.valueOf(id));
            deletePromptText = String.format("Are you sure you want to delete this %s order?", typeText) +
                    (isBuy?"":"If it has been involved in any transactions, the buy order(s) resolved in those transactions" +
                            "will also be deleted");
            if (isCreate){
            this.id = id;
            }
            else {
                o = data.getBuyByKey(id);
                infoLabel.setText(String.format("Info for %s order", typeText));
                extraInfoLabel.setText(String.format(wrapDialog, MessageFormat.format("Placed at {0} by a member of {1} for asset {2}; {3}",
                        o.getDatePlaced().toString(), o.getUnit(), o.getAsset(),
                        (o.getDateResolved() == null) ? "unresolved." : MessageFormat.format("resolved at {0}{1}.",
                                o.getDateResolved().toString(), (o instanceof BuyOrder ? " with sell order " + ((BuyOrder) o).getBoughtFrom().toString() : "")))));
                quantityInput.setValue(o.getQty());
                quantityInput.setEnabled(false);
                priceInput.setValue(o.getPrice());
                priceInput.setEnabled(false);
            }

        }
        void done(String title, int asset) {
            if (displayConfirm(title, "Go to order list rather than asset page?")
                == JOptionPane.YES_OPTION) shellPanel(new OrdersPage(true, isBuy, false), false);
            else shellPanel(new AssetInfoPage(asset), false);
        }

        void initialiseFields() {
            infoLabel = new JLabel();extraInfoLabel = new JLabel();numberKeyLabel = new JLabel();
            quantityInput = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
            priceInput = new JSpinner(new SpinnerNumberModel(1,1,null,1));
        }
        @Override
        JPanel[] generateRows() {
            initialiseFields();
            JPanel[] output = {new JPanel(), new JPanel(), new JPanel(), new JPanel()};
            output[0].add(infoLabel);
            output[0].add(numberKeyLabel);
            output[1].add(extraInfoLabel);
            output[2].add(new JLabel("Quantity:"));
            output[2].add(quantityInput);
            output[3].add(new JLabel("Price per asset:"));
            output[3].add(priceInput);
            return output;
        }

        @Override
        void cancel() {
            if (displayConfirm("Exit", "Changes will be discarded")
                    == JOptionPane.YES_OPTION) done("Exiting", (o==null?id:o.getAsset()));
        }

        @Override
        void save() {
            displayError("Cannot edit", "Orders can only be created and deleted, I meant to remove" +
                    "this button before the deadline, oops");
        }

        @Override
        void create() {
            try {
                if (isBuy) data.placeBuyOrder(new BuyOrder(user.getUnit(), id,
                        (Integer)quantityInput.getValue(), (Integer)priceInput.getValue()));
                else data.placeSellOrder(new SellOrder(user.getUnit(), id,
                        (Integer)quantityInput.getValue(), (Integer)priceInput.getValue()));
                done("Order successfully placed!", id);
            } catch (OrderException e) {
                displayError("Could not place order", e.getMessage());
            } catch (InvalidAmount invalidAmount) {
                displayError("Invalid value", invalidAmount.getMessage());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error", doesNotExist.getMessage());
            }
        }

        @Override
        void delete() {
            try {
                if (isBuy) data.cancelBuyOrder(id);
                else data.cancelSellOrder(id);
                done("Buy order successfully deleted!", o.getAsset());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error", doesNotExist.getMessage());
            } catch (InvalidAmount invalidAmount) {
                displayError("Unexpected error when doing refund", invalidAmount.getMessage());
            }
        }
    }

    class AdminPortal extends JPanel implements ActionListener {
        JButton newUserButton = new JButton("New user");
        JButton editUserButton = new JButton("Edit selected");
        JButton userListButton = new JButton("Expanded list");
        JButton newUnitButton = new JButton("New organisational unit");
        JButton editUnitButton = new JButton("Edit selected");
        JButton unitHoldingsButton = new JButton("Holdings");
        JButton newAssetButton = new JButton("New asset");
        JButton editAssetButton = new JButton("Edit selected");
        JButton assetHoldingsButton = new JButton("Holdings");
        GuiSearch userSearch;
        GuiSearch unitSearch;
        GuiSearch assetSearch;

        AdminPortal() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            userSearch = new GuiSearch(data.getAllUsernames());
            unitSearch = new GuiSearch(data.getAllUnitNames());
            assetSearch = new GuiSearch(data.getAllAssetStrings());
            newUserButton.addActionListener(this);
            newUnitButton.addActionListener(this);
            newAssetButton.addActionListener(this);
            editAssetButton.addActionListener(this);
            editUnitButton.addActionListener(this);
            editUserButton.addActionListener(this);
            userListButton.addActionListener(this);
            unitHoldingsButton.addActionListener(this);
            assetHoldingsButton.addActionListener(this);
            add(adminActionRow(new JButton[]{newUserButton, editUserButton, userListButton}, userSearch));
            add(adminActionRow(new JButton[]{newUnitButton, editUnitButton, unitHoldingsButton}, unitSearch));
            add(adminActionRow(new JButton[]{newAssetButton, editAssetButton, assetHoldingsButton}, assetSearch));
        }

        JPanel adminActionRow(JButton[] buttons, GuiSearch search) {
            JPanel result = new JPanel();
            for (JButton b : buttons) {
                result.add(b);
            }
            result.add(search);
            return result;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == editAssetButton) {
                try {shellPanel(new AssetFormPage((String) assetSearch.getSelectedItem()), false);}
                catch (NumberFormatException n) {
                    displayError("Could not load page", "Please select a valid asset option");
                } catch (DoesNotExist d) {
                    displayError("Could not load page", d.getMessage());
                }
            }
            else if (e.getSource() == newAssetButton) {
                try {
                    shellPanel(new AssetFormPage(null), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Could not load page due to unexpected error", doesNotExist.getMessage());
                }
            }
            else if (e.getSource() == editUnitButton) {
                try {
                    shellPanel(new UnitFormPage((String) unitSearch.getSelectedItem()), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Could not load page", doesNotExist.getMessage());
                }
            }
            else if (e.getSource() == newUnitButton) {
                try {
                    shellPanel(new UnitFormPage(null), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Could not load page due to unexpected error", doesNotExist.getMessage());
                }
            }
            else if (e.getSource() == editUserButton) {
                try {
                    shellPanel(new UserFormPage((String) userSearch.getSelectedItem()), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Could not load page", doesNotExist.getMessage());
                }
            }
            else if (e.getSource() == newUserButton) {
                try {
                    shellPanel(new UserFormPage(null), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Could not load page due to unexpected error", doesNotExist.getMessage());
                }
            }
        }
    }
}
