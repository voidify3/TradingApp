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
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;

/***
 * @author Sophia Walsh Long, with some code borrowed from vastly different implementation by Johnny and Scott
 */
class NewTradingAppGUI extends JFrame {
    // Logged in user for the session
    private User user = null;
    private TradingAppData data;
    // General variables
    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;
    private static final String DARKGREY = "#4D4D5D";
    private static final String WHITE = "#FCFCFC";
    private static final String WRAP_DIALOG ="<html><body><p style='width: 200px;'>%s</p></body></html>";
    // Top-level components
    JPanel loginPanel;
    JPanel shellPanel;
    JMenuBar menuBar;

    public NewTradingAppGUI(TradingAppData data) {
        setTitle("STONK MACHINE");
        this.data = data;
    }


    void createAndShowGUI() throws IllegalString, AlreadyExists, IOException, DoesNotExist, InvalidDate, InvalidAmount, OrderException {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        data.mockObjectsWithPrices();
        loginPanel(); // creates & shows login portal as the first screen
        menuBar(); // declare the menu-bar once here

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Menu Bar---------------------------------------------------------------------------------------------------------
    private void menuBar() {
        menuBar = new MyMenuBar(); // reset

        // Boilerplate
        setJMenuBar(menuBar);
        revalidate();
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
        if (preserveAlpha) g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        BufferedImage img;
        JLabel bannerLabel = new JLabel();
        try {
            img = ImageIO.read(new File("./Images/GUI images/banner.png"));
            BufferedImage newImg = resizeImg(img, 350,62, false);
            bannerLabel = new JLabel(new ImageIcon(newImg));
        } catch (IOException e) {
            bannerLabel.setText("STONK MACHINE");
        }
        InnerLoginPage login = new InnerLoginPage();
        login.formatButton();

        // Add panels together
        loginBox.add(bannerLabel);
        loginBox.add(login);
        loginPanel.add(loginBox);

        // Boilerplate
        setContentPane(loginPanel);
        revalidate();
    }

    // Shell template (content depends on the parameter)----------------------------------------------------------------
    private void shellPanel(JPanel content, boolean includeWelcomeLabel) {
        shellPanel(content, includeWelcomeLabel, null);
    }
    private void shellPanel(JPanel content, boolean includeWelcomeLabel, String unitLabel) {
        shellPanel = new JPanel(); // reset
        shellPanel.setBackground(Color.decode(WHITE));
        try { //entire thing needs to be this
            user = data.getUserByKey(user.getUsername());

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
            JLabel welcomeLabel = new JLabel(String.format("Welcome back %s! %s", user.getUsername(), creditsText),
                    SwingConstants.CENTER);

            shellPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
            shellPanel.setLayout(new BoxLayout(shellPanel, BoxLayout.PAGE_AXIS));
            ShellFirstRow row1 = new ShellFirstRow((unitLabel==null?unitText.toUpperCase():unitLabel));

            row1.setMaximumSize(new Dimension(WIDTH, 50));
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

            // Boilerplate
            setContentPane(shellPanel);
            revalidate();
        } catch (DoesNotExist doesNotExist) {
            doesNotExist.printStackTrace();
            displayError("Unexpected error: " + doesNotExist.getMessage(),
                    "Either something went wrong internally, or an admin deleted your account while" +
                            "this page was loading. Logging you out...");
        }
    }
    //helpers
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


    void messageDialogFormatted(String title, String message, int type) {
        JOptionPane.showMessageDialog(this,
                String.format(WRAP_DIALOG, message),
                title, type);
    }
    void displayError(String title, String message) {
        messageDialogFormatted(title, message, JOptionPane.ERROR_MESSAGE);
    }
    int displayConfirm(String title, String message) {
        return JOptionPane.showConfirmDialog(NewTradingAppGUI.this, String.format(WRAP_DIALOG, message),
                title, JOptionPane.YES_NO_OPTION);
    }

    void displayFeedback(String title, String message) {
        messageDialogFormatted(title, message, JOptionPane.INFORMATION_MESSAGE);
        //JOptionPane.showMessageDialog(TradingAppGUI.this,message, title, JOptionPane.PLAIN_MESSAGE);
    }

    //Helper to throw NotAuthorised
    void failIfNotAdmin(String thingAttempted) throws NotAuthorised {
        if (!user.getAdminAccess()) throw new NotAuthorised("You do not have permission to: " + thingAttempted);
    }

    /**
     * Parse the asset ID from a string generated by TradingAppData.getAssetStrings, or return -1 if the parse failed
     * @param seed A string which may or may not be a valid asset string generated from getAssetStrings
     * @return An int parsed from the substring of seed prior to the first opening parenthesis.
     * If seed contains no opening parenthesis, or the substring prior to the opening parenthesis could not be
     * parsed as an int, the method returns -1 to induce a DoesNotExist when the return value is used to query asset
     */
    int parseAssetString(String seed) {
        if (seed == null) return -1;
        int i = seed.indexOf('(');
        if (i<1) return -1;
        try {return parseInt(seed.substring(0, i - 1));}
        catch (NumberFormatException n) {
            return -1;
        }
    }
    private void doLogin() throws DoesNotExist {
        shellPanel(new HomePage(), true);
    }

    private void doLogout() {
        user = null; // reset session user data
        loginPanel(); // creates & shows login portal
    }
    class ShellPanel extends JPanel implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }
    class OuterLogin extends JPanel {

    }
    class MyMenuBar extends JMenuBar implements ActionListener {
        JMenuItem logoutMenu = new JMenuItem("Logout");
        JMenuItem exitMenu = new JMenuItem("Exit");
        JMenuItem changePassword = new JMenuItem("Change password");
        JMenuItem masterUserKey = new JMenuItem("Master User Key");
        JMenuItem masterAdminKey = new JMenuItem("Master Admin Key");
        MyMenuBar() {
            JMenu fileMenu = new JMenu("File");
            fileMenu.add("Preference");
            fileMenu.add(logoutMenu);
            fileMenu.add(exitMenu);
            logoutMenu.addActionListener(this);
            exitMenu.addActionListener(this);
            add(fileMenu);

            JMenu editMenu = new JMenu("Edit");
            editMenu.add("Cut");
            editMenu.add("Copy");
            editMenu.add("Paste");
            add(editMenu);

            JMenu viewMenu = new JMenu("View");
            add(viewMenu);

            JMenu helpMenu = new JMenu("Help");
            helpMenu.add(changePassword);
            changePassword.addActionListener(this);
            add(helpMenu);

            JMenu devMenu = new JMenu("Dev Tools");
            devMenu.add(masterUserKey);
            devMenu.add(masterAdminKey);
            masterUserKey.addActionListener(this);
            masterAdminKey.addActionListener(this);
            add(devMenu);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == exitMenu) System.exit(0);
            else if (e.getSource() == logoutMenu) doLogout();
            else if (e.getSource() == masterUserKey) {
                user = TradingAppData.userDev;
                try {
                    doLogin();
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            }
            else if (e.getSource() == masterAdminKey) {
                user = TradingAppData.adminDev;
                try {
                    doLogin();
                } catch (DoesNotExist doesNotExist) {
                    doesNotExist.printStackTrace();
                }
            }
            else if (e.getSource() == changePassword && NewTradingAppGUI.this.user != null) shellPanel(new ChangePasswordPage(), false);
        }
    }
    class ShellFirstRow extends JPanel implements ActionListener {
        JButton homeButton = new JButton("Home");
        JButton adminButton = new JButton("Admin portal");
        JButton assetsButton = new JButton("All assets");
        JButton ordersButton = new JButton("Orders");
        JLabel topLabel = new JLabel("ORG UNIT HERE", SwingConstants.CENTER);

        ShellFirstRow(String toplabel) {
            topLabel.setText(toplabel);
            homeButton.setToolTipText("Go to the home screen");
            assetsButton.setToolTipText("Go to the search screen");
            adminButton.setToolTipText("Go to admin portal");
            ordersButton.setToolTipText("View my orders");
            JPanel leftButtons = new JPanel();
            JPanel rightButtons = new JPanel();
            // keeps row 1 fixed in height so no matter what the content is (a table, buttons, graphs)
            // it won't change sizes (sometimes takes up most of the screen!)
            leftButtons.setBackground(Color.decode(WHITE));
            rightButtons.setBackground(Color.decode(WHITE));
            setBackground(Color.decode(WHITE));
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            setLayout(new BorderLayout());
            leftButtons.add(homeButton);
            homeButton.addActionListener(this);
            if (user.getAdminAccess()) {
                leftButtons.add(adminButton);
                adminButton.addActionListener(this);
            }
            rightButtons.add(assetsButton);
            assetsButton.addActionListener(this);
            rightButtons.add(ordersButton);
            ordersButton.addActionListener(this);
            add(leftButtons, BorderLayout.WEST);
            add(topLabel, BorderLayout.CENTER);
            add(rightButtons, BorderLayout.EAST);

        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == homeButton) {
                try {
                    shellPanel(new HomePage(), true);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Could not load due to unexpected error", doesNotExist.getMessage());
                }
            }
            else if (e.getSource() == ordersButton) shellPanel(new OrdersTablePage(true, true, false), false);
            else if (e.getSource() == adminButton) shellPanel(new AdminPortal(), false, "ADMIN PORTAL");
            else if (e.getSource() == assetsButton) {
                try {
                    shellPanel(new AssetTablePage(), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("Could not load due to unexpected error", doesNotExist.getMessage());
                }
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
            userSearch = new GuiSearch(data.getUsernames(data.getAllUsers()));
            unitSearch = new GuiSearch(data.getUnitNames(data.getAllUnits()));
            assetSearch = new GuiSearch(data.getAssetStrings(data.getAllAssets()));
            newUserButton.addActionListener(this);
            newUnitButton.addActionListener(this);
            newAssetButton.addActionListener(this);
            editAssetButton.addActionListener(this);
            editUnitButton.addActionListener(this);
            editUserButton.addActionListener(this);
            userListButton.addActionListener(this);
            unitHoldingsButton.addActionListener(this);
            assetHoldingsButton.addActionListener(this);
            add(adminActionRow(new JButton[]{newUserButton, editUserButton}, userSearch));
            add(adminActionRow(new JButton[]{newUnitButton, editUnitButton, unitHoldingsButton}, unitSearch));
            add(adminActionRow(new JButton[]{newAssetButton, editAssetButton, assetHoldingsButton}, assetSearch));
        }
        JPanel adminActionRow(JButton[] buttons, GuiSearch search) {
            JPanel result = new JPanel();
            for (JButton b : buttons) result.add(b);
            result.add(search);
            return result;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() == editAssetButton)
                    shellPanel(new AssetFormPage((String) assetSearch.getSelectedItem()), false);
                else if (e.getSource() == assetHoldingsButton)
                    shellPanel(new InventoryTablePage(parseAssetString((String) assetSearch.getSelectedItem())), false);
                else if (e.getSource() == editUnitButton)
                    shellPanel(new UnitFormPage((String) unitSearch.getSelectedItem()), false);
                else if (e.getSource() == unitHoldingsButton)
                    shellPanel(new InventoryTablePage((String) unitSearch.getSelectedItem()), false);
                else if (e.getSource() == editUserButton)
                    shellPanel(new UserFormPage((String) userSearch.getSelectedItem()), false);
            } catch (DoesNotExist d) { displayError("Could not load page", d.getMessage());}
            try {
                if (e.getSource() == newAssetButton) shellPanel(new AssetFormPage(null), false);
                else if (e.getSource() == newUnitButton) shellPanel(new UnitFormPage(null), false);
                else if (e.getSource() == newUserButton) shellPanel(new UserFormPage(null), false);
            } catch (DoesNotExist d) {
                displayError("Could not load page due to unexpected error", d.getMessage());}
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
        TreeMap<LocalDate, Double> graphData = new TreeMap<>();
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
        void renderGraph() {

        }
        void tryGetHistoricalPrices(TradingAppData.Intervals intervals) {
            try {
                graphData = data.getHistoricalPrices(asset, intervals);
                renderGraph();
            }
            catch (InvalidDate | DoesNotExist invalidDate) { displayError("An error occurred while displaying prices", invalidDate.getMessage()); }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == daysButton) tryGetHistoricalPrices(TradingAppData.Intervals.DAYS);
            else if (e.getSource() == weeksButton) tryGetHistoricalPrices(TradingAppData.Intervals.WEEKS);
            else if (e.getSource() == monthsButton) tryGetHistoricalPrices(TradingAppData.Intervals.MONTHS);
            else if (e.getSource() == yearsButton) tryGetHistoricalPrices(TradingAppData.Intervals.YEARS);
            try {
                if (e.getSource() == buyButton) shellPanel(new OrderFormPage(asset, true, true), false);
                else if (e.getSource() == sellButton) shellPanel(new OrderFormPage(asset, true, false), false);
            } catch (DoesNotExist d) { displayError("Unexpected error", d.getMessage()); }
        }
    }
    class InnerLoginPage extends JPanel implements ActionListener, ItemListener, KeyListener {
        JCheckBox passwordHide = new JCheckBox();
        JButton loginButton = new JButton("Login");
        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");
        JLabel invalidLabel = new JLabel(" ");
        JTextField usernameInput = new JTextField(10);
        JPasswordField passwordInput = new JPasswordField(10);
        GridBagConstraints cords = new GridBagConstraints(-1, 1, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
        InnerLoginPage() {
            setLayout(new GridBagLayout());
            invalidLabel.setMinimumSize(new Dimension(100, 50));
            passwordHide.setToolTipText("Show & hide password...");

            // Reset fields & checkbox
            passwordInput.setText("");
            usernameInput.setText("");
            invalidLabel.setText(" "); // need space for the label to have a fixed height
            passwordHide.setSelected(false);

            // Position the interactive components (text fields, buttons etc)
            add(usernameLabel, cords);
            cords.gridx += 2;
            add(usernameInput, cords);

            cords.gridy++; // password row is always below username row
            cords.gridx = -1;
            add(passwordLabel, cords);
            cords.gridx += 2;
            add(passwordInput, cords);
            cords.gridx++;
            add(passwordHide,cords);

            cords.gridy++; // button is always below the last row
            cords.gridx = 0;
            cords.gridwidth = 3;
            add(loginButton, cords);

            cords.gridy++; // warning message is always below login button
            add(invalidLabel, cords);
            loginButton.addActionListener(this);
            passwordInput.addKeyListener(this);
            passwordHide.addItemListener(this);

        }
        void formatButton() {
            loginButton.setContentAreaFilled(false);
            loginButton.setOpaque(true);
        }
        private void checkLogin() {
            // store the user object for the duration of the session (if login was successful)
            try {
                user = data.login(usernameInput.getText(), new String(passwordInput.getPassword()));
                shellPanel(new HomePage(), true);
            } catch (DoesNotExist | IllegalString ex) {
                ex.printStackTrace();
                passwordInput.setText("");  // clear password field
                invalidLabel.setForeground(Color.RED);
                invalidLabel.setText("Invalid Login Credentials");
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) { if (e.getSource() == loginButton) checkLogin(); }
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == passwordHide){
                if (e.getStateChange() == ItemEvent.SELECTED) passwordInput.setEchoChar((char) 0);
                else passwordInput.setEchoChar('\u2022');
            }
        }

        @Override public void keyTyped(KeyEvent e) {}
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getSource() == passwordInput && e.getKeyCode() == KeyEvent.VK_ENTER) {
                checkLogin();
            }
        }
        @Override public void keyReleased(KeyEvent e) {}
    }
    class ChangePasswordPage extends InnerLoginPage {
        JLabel newPasswordLabel = new JLabel("New password");
        JLabel confirmNewPasswordLabel = new JLabel("Confirm new password");
        JPasswordField newPasswordInput = new JPasswordField(10);
        JPasswordField confirmNewPasswordInput = new JPasswordField(10);
        JButton changePassButton = new JButton("Change password");
        ChangePasswordPage() {
            super();
            usernameInput.setText(user.getUsername());
            usernameInput.setEnabled(false);
            remove(loginButton);
            remove(invalidLabel);
            cords.gridy--;
            cords.gridwidth = 1;
            cords.gridx = -1;
            add(newPasswordLabel, cords);
            cords.gridx += 2;
            add(newPasswordInput, cords);

            cords.gridy++; // confirm row is always below new password row
            cords.gridx = -1;
            add(confirmNewPasswordLabel, cords);
            cords.gridx += 2;
            add(confirmNewPasswordInput, cords);

            cords.gridy++; // button is always below the last row
            cords.gridx = 0;
            cords.gridwidth = 3;
            add(changePassButton, cords);

            cords.gridy++; // warning message is always below login button
            add(invalidLabel, cords);
            changePassButton.addActionListener(this);

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
                    invalidLabel.setText(ex.getMessage());
                } catch (ConstraintException e) {
                    displayError("Unexpected error: ", e.getMessage());
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getSource() == confirmNewPasswordInput && e.getKeyCode() == KeyEvent.VK_ENTER) {
                checkPassChange();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == changePassButton) checkPassChange();
        }
    }
    abstract class TablePage extends JPanel implements MouseListener {
        JTable table;
        JPanel beforePanel;
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
            beforePanel = new JPanel();
            table = new JTable(info,columnNames);
            table.setModel(tableModel);
            scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600,270));
            this.setBackground(Color.decode(DARKGREY));
            this.add(beforePanel);
            this.add(scrollPane);
            table.addMouseListener(this);
        }
        abstract void onRowClick(String col1, String col2);
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            String key = table.getModel().getValueAt(row, 0).toString();
            onRowClick(key, table.getModel().getValueAt(row, 1).toString());
        }
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
    class HomePage extends TablePage {
        private HomePage(String[][] info) {
            super(new String[]{"Asset ID", "Description", "Qty", "$ Current"}, info,
                    new int[]{25, 400, 20, 30}, new boolean[]{false, true, false, false});
            this.remove(beforePanel);
        }
        HomePage() throws DoesNotExist {
            this((user.getUnit() == null? new String[0][4] :
                    populateTable(data.getInventoriesByOrgUnit(user.getUnit()), true)));
        }
        @Override
        void onRowClick(String col1, String col2) {
            shellPanel(new AssetInfoPage(parseInt(col1)), false, col2.toUpperCase()); }
    }
    class OrdersTablePage extends TablePage implements ActionListener {
        boolean isBuy;
        boolean justOurs;
        boolean resolvedFlag;
        JLabel optionsLabel = new JLabel("Filter:");
        ButtonGroup isResolved = new ButtonGroup();
        JPanel isResolvedPanel = new JPanel(new BorderLayout());
        JRadioButton resolved = new JRadioButton("Resolved");
        JRadioButton unresolved = new JRadioButton("Unresolved");
        ButtonGroup orderType = new ButtonGroup();
        JPanel orderTypePanel = new JPanel(new BorderLayout());
        JRadioButton buys = new JRadioButton("Buy orders");
        JRadioButton sells = new JRadioButton("Sell orders");
        ButtonGroup whoseOrders = new ButtonGroup();
        JPanel whoseOrdersPanel = new JPanel(new BorderLayout());
        JRadioButton fromUs = new JRadioButton("From my organisational unit");
        JRadioButton fromAllUnits = new JRadioButton("From all organisational units");
        JButton applyButton = new JButton("Apply filters");
        private OrdersTablePage(String[][] info) {
            super(new String[]{"ID", "Unit", "Asset", "Price", "Quantity", "Placed", "Resolved"}, info,
                    new int[]{50,50,50,50,50,50,50}, new boolean[7]);
            isResolved.add(resolved);
            isResolved.add(unresolved);
            isResolvedPanel.add(resolved, BorderLayout.NORTH);
            isResolvedPanel.add(unresolved, BorderLayout.CENTER);
            orderType.add(buys);
            orderType.add(sells);
            orderTypePanel.add(buys, BorderLayout.NORTH);
            orderTypePanel.add(sells, BorderLayout.CENTER);
            whoseOrders.add(fromUs);
            whoseOrders.add(fromAllUnits);
            whoseOrdersPanel.add(fromUs, BorderLayout.NORTH);
            whoseOrdersPanel.add(fromAllUnits, BorderLayout.CENTER);
            beforePanel.add(optionsLabel);
            beforePanel.add(isResolvedPanel);
            beforePanel.add(orderTypePanel);
            beforePanel.add(whoseOrdersPanel);
            beforePanel.add(applyButton);
            applyButton.addActionListener(this);
        }
        OrdersTablePage(boolean justOurs, boolean isBuy, boolean resolvedFlag) {
            this(populateOrderTable(data.getOrdersForTable(user.getUnit(), justOurs,isBuy,resolvedFlag)));
            this.isBuy = isBuy;
            this.justOurs = justOurs;
            this.resolvedFlag = resolvedFlag;
            isResolved.clearSelection();
            if (resolvedFlag) resolved.setSelected(true); else unresolved.setSelected(true);
            orderType.clearSelection();
            if (isBuy) buys.setSelected(true); else sells.setSelected(true);
            whoseOrders.clearSelection();
            if (justOurs) fromUs.setSelected(true); else fromAllUnits.setSelected(true);
        }
        @Override
        void onRowClick(String col1, String col2) {
            if (user.getAdminAccess() || (col2.equals(user.getUnit()) && !resolvedFlag)){
                try {
                    shellPanel(new OrderFormPage(Integer.parseInt(col1), false, isBuy), false);
                } catch (DoesNotExist doesNotExist) {
                    displayError("An error occurred attempting to load page", doesNotExist.getMessage());
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == applyButton) {
                shellPanel(new OrdersTablePage(fromUs.isSelected(), buys.isSelected(), resolved.isSelected()), false);
            }
        }
    }
    class AssetTablePage extends TablePage {

        AssetTablePage(String[][] info) {
            super(new String[]{"Asset ID", "Description", "$ Current"}, info,
                    new int[]{25, 400, 50}, new boolean[]{false, true, false});
            this.remove(beforePanel);
        }
        AssetTablePage() throws DoesNotExist {
            this(populateTable(data.getAllAssets()));
        }

        @Override
        void onRowClick(String col1, String col2) {
            shellPanel(new AssetInfoPage(parseInt(col1)),
                    false, col2.toUpperCase());
        }
    }
    class InventoryTablePage extends TablePage implements ActionListener{
        String unitfilter = null;
        int assetfilter = 0;
        ButtonGroup filter = new ButtonGroup();
        JRadioButton showAll = new JRadioButton("Show all");
        JPanel unitPanel = new JPanel(new BorderLayout());
        JPanel assetPanel = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new BorderLayout());
        JRadioButton byUnit = new JRadioButton("Filter by unit");
        JRadioButton byAsset = new JRadioButton("Filter by asset");
        GuiSearch assetSearch = new GuiSearch(data.getAssetStrings(data.getAllAssets()));
        GuiSearch unitSearch = new GuiSearch(data.getUnitNames(data.getAllUnits()));
        JButton refreshButton = new JButton("Apply filter");
        JButton newRecordButton = new JButton("New record");
        JPanel editInvPanel = new JPanel(new GridLayout(2, 2));
        JSpinner quantityInput = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        String[] options = {"Save changes", "Delete record", "Cancel"};
        String[] options2 = {"Save", "Cancel"};
        GuiSearch dialogSearch;
        InventoryTablePage(String[][] info) {
            super(new String[]{"Organisational unit", "Asset ID", "Asset description", "Quantity"}, info,
                    new int[]{50, 25, 380, 20}, new boolean[]{false, false, true, false});
            filter.add(showAll);
            filter.add(byUnit);
            filter.add(byAsset);

            beforePanel.add(showAll);
            unitPanel.add(byUnit, BorderLayout.NORTH);
            unitPanel.add(unitSearch, BorderLayout.CENTER);
            beforePanel.add(unitPanel);
            assetPanel.add(byAsset, BorderLayout.NORTH);
            assetPanel.add(assetSearch, BorderLayout.CENTER);
            beforePanel.add(assetPanel);
            btnPanel.add(refreshButton, BorderLayout.NORTH);
            btnPanel.add(newRecordButton, BorderLayout.CENTER);
            beforePanel.add(btnPanel);
            byUnit.addActionListener(this);
            byAsset.addActionListener(this);
            showAll.addActionListener(this);
            refreshButton.addActionListener(this);
            newRecordButton.addActionListener(this);
            assetSearch.addActionListener(this);
            unitSearch.addActionListener(this);
            JFormattedTextField qtyTF = ((JSpinner.DefaultEditor) quantityInput.getEditor()).getTextField();
            qtyTF.setColumns(4);
        }

        /**
         * Constructor that shows ALL inventory records
         * @throws DoesNotExist Never, but the computer doesn't know that
         */
        InventoryTablePage() throws DoesNotExist {
            this(populateTable(data.getAllInventories(), false));
            unitfilter = null;
            assetfilter = 0;
            setEnableds(false,false,false);
            showAll.setSelected(true);
        }

        /**
         * Constructor that shows inventory records for one unit
         * @param unit Unit name
         * @throws DoesNotExist when the unit doesn't exist
         */
        InventoryTablePage(String unit) throws DoesNotExist {
            this(populateTable(data.getInventoriesByOrgUnit(unit), false));
            if (unit == null) throw new DoesNotExist("Please select an option from the list");
            data.getUnitByKey(unit); //exception if it doesn't exist
            unitfilter = unit;
            assetfilter = 0;
            setEnableds(false,true,false);
            unitSearch.setSelectedItem(unit);
            byUnit.setSelected(true);
        }

        /**
         * Constructor that shows inventory records for one asset
         * @param asset Asset name
         * @throws DoesNotExist when the asset doesn't exist
         */
        InventoryTablePage(int asset) throws DoesNotExist {
            this(populateTable(data.getInventoriesByAsset(asset), false));
            data.getAssetByKey(asset); //exception if it doesn't exist
            assetfilter = asset;
            unitfilter = null;
            setEnableds(true,false,true);
            assetSearch.setSelectedIndex(asset-1);
            byAsset.setSelected(true);
        }
        void notAuthorisedDialog(String message) {
            displayError("Unexpected error: " + message,
                    "Another admin may have revoked your admin access");
        }
        void refresh() throws DoesNotExist {
            if (unitfilter != null) shellPanel(new InventoryTablePage(unitfilter), false);
            else if (assetfilter != 0) shellPanel(new InventoryTablePage(assetfilter), false);
            else shellPanel(new InventoryTablePage(), false);
        }
        void loadSelection() {
            try {
                if (showAll.isSelected()) shellPanel(new InventoryTablePage(), false);
                else if (byAsset.isSelected())
                    shellPanel(new InventoryTablePage(parseAssetString((String) assetSearch.getSelectedItem())), false);
                else if (byUnit.isSelected())
                    shellPanel(new InventoryTablePage((String) unitSearch.getSelectedItem()), false);
            }catch (DoesNotExist doesNotExist) {
                displayError("Could not load", doesNotExist.getMessage()); }
        }
        void create() throws DoesNotExist, NotAuthorised {
            String titlestring = "";
            boolean isUnit;
            if (unitfilter != null && unitfilter.equals(unitSearch.getSelectedItem())){
                editInvPanel.add(new JLabel("Select asset:"));
                dialogSearch = new GuiSearch(data.getAssetStrings(data.getUnheldAssets(unitfilter)));
                titlestring = "unit " + unitfilter;
                isUnit = true;
            }
            else if (assetfilter != 0 && parseAssetString((String) assetSearch.getSelectedItem()) == assetfilter) {
                editInvPanel.add(new JLabel("Select unit:"));
                dialogSearch = new GuiSearch(data.getUnitNames(data.getUnholdingUnits(assetfilter)));
                titlestring = "asset " + assetfilter;
                isUnit = false;
            }
            else { //should never be possible since button would be disabled but just in case
                displayError("Action not valid", "You can only create a new record if the filter" +
                        "is set to a valid asset or unit, and the filter has been applied.");
                return;
            }
            editInvPanel.add(dialogSearch);
            editInvPanel.add(new JLabel("Enter quantity:"));
            editInvPanel.add(quantityInput);
            quantityInput.setValue(0);
            int result = JOptionPane.showOptionDialog(NewTradingAppGUI.this, editInvPanel, "Creating new holding for " + titlestring,
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options2, null);
            if (result == JOptionPane.YES_OPTION) {
                failIfNotAdmin("Create a new inventory record");
                data.setInventory(new InventoryRecord((isUnit? unitfilter :  (String)dialogSearch.getSelectedItem()),
                        (isUnit? parseAssetString((String) dialogSearch.getSelectedItem()) : assetfilter), (Integer) quantityInput.getValue()));
                refresh();
            }
        }

        /**
         * Set the enabled value of three elements whose enabled values are often set together
         * @param assetSrch new enabled value for assetSearch
         * @param unitSrch new enabled value for unitSearch
         * @param newRecBtn new enabled value for newRecordButton
         */
        void setEnableds(boolean assetSrch, boolean unitSrch, boolean newRecBtn) {
            assetSearch.setEnabled(assetSrch);
            unitSearch.setEnabled(unitSrch);
            newRecordButton.setEnabled(newRecBtn);
        }

        @Override
        void onRowClick(String col1, String col2) {
            editInvPanel.add(new JLabel("Enter quantity:"));
            editInvPanel.add(quantityInput);
            try {
                int asset = parseInt(col2);
                int qty = data.getInv(col1, asset).getQuantity();
                quantityInput.setValue(qty);
                int result = JOptionPane.showOptionDialog(NewTradingAppGUI.this, editInvPanel, "Editing quantity of asset " + col2 + " held by " + col1 ,
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
                if (result == JOptionPane.YES_OPTION) {
                    failIfNotAdmin("Change the quantity of an asset held by an organisational unit");
                    data.setInventory(new InventoryRecord(col1, asset, (Integer) quantityInput.getValue()));
                    refresh();
                }
                else if (result == JOptionPane.NO_OPTION) {
                    failIfNotAdmin("Delete inventory information");
                    if (displayConfirm("Confirm deletion",  "Are you sure you want to delete this record?")
                            == JOptionPane.YES_OPTION) data.deleteInventoryRecord(col1, asset);
                    refresh();
                }
            } catch (DoesNotExist doesNotExist) {
                displayError("Could not edit", doesNotExist.getMessage());
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == showAll) setEnableds(false, false, false);
            else if (e.getSource() == byAsset) setEnableds(true, false, false);
            else if (e.getSource() == byUnit) setEnableds(false, true, false);
            else if (e.getSource() == refreshButton) loadSelection();
            else if (e.getSource()==newRecordButton) {
                try {create();}
                catch (DoesNotExist doesNotExist) { displayError("Could not load", doesNotExist.getMessage());}
                catch (NotAuthorised notAuthorised) { notAuthorisedDialog(notAuthorised.getMessage());}
            }
            else if (e.getSource()==unitSearch && !unitfilter.equals(unitSearch.getSelectedItem()))
                    newRecordButton.setEnabled(false);
            else if (e.getSource()==assetSearch && assetfilter != parseAssetString((String) assetSearch.getSelectedItem()))
                    newRecordButton.setEnabled(false);
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
        void cancel() { if (displayConfirm("Return to portal?", "Changes will be discarded")
                    == JOptionPane.YES_OPTION) exitToPortal(); }
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
            else if (e.getSource()==cancelEditButton) cancel();
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
                int id = parseAssetString(seed);
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
                data.updateAsset(DataObjectFactory.newAssetValidated(parseInt(numberKeyLabel.getText()), description));
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
                data.addAsset(DataObjectFactory.newAssetValidated(description));
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
            ArrayList<String> units = data.getUnitNames(data.getAllUnits());
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
                data.setUnitBalance(old.getName(), (Integer) creditsInput.getValue());
                displayFeedback("Unit successfully updated", "Click OK to return to admin portal");
                exitToPortal();
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (DoesNotExist doesNotExist) {
                displayError("Unexpected error: " + doesNotExist.getMessage(),
                        "Another admin may have deleted the unit.");
            } catch (InvalidAmount invalidAmount) {
                displayError("Could not save", invalidAmount.getMessage());
            }
        }


        @Override
        void create() {
            try {
                failIfNotAdmin("Create an organisational unit");
                data.addUnit(DataObjectFactory.newOrgUnitValidated(old.getName(), (Integer) creditsInput.getValue()));
                displayFeedback("Unit successfully created", "Click OK to return to admin portal");
                exitToPortal();
            } catch (NotAuthorised notAuthorised) {
                notAuthorisedDialog(notAuthorised.getMessage());
            } catch (IllegalString illegalString) {
                displayError("Invalid name", illegalString.getMessage());
            } catch (AlreadyExists doesNotExist) {
                displayError("Unit could not be created", doesNotExist.getMessage());
            } catch (InvalidAmount invalidAmount) {
                displayError("Invalid amount", invalidAmount.getMessage());
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
        JLabel infoLabel;
        JLabel extraInfoLabel;
        JLabel numberKeyLabel;
        JSpinner quantityInput;
        JSpinner priceInput;
        OrderFormPage(int id, boolean isCreate, boolean isBuy) throws DoesNotExist {
            super(isCreate);
            buttonRow.remove(saveEditButton);
            this.isBuy = isBuy;
            String typeText = isBuy?"buy":"sell";
            infoLabel.setText(String.format("Placing new %s order for asset: ", typeText));
            numberKeyLabel.setText(String.valueOf(id));
            deletePromptText = String.format("Are you sure you want to delete this %s order?", typeText) +
                    (isBuy?"":"If it has been involved in any transactions, the buy order(s) resolved in those transactions" +
                            "will also be deleted");
            if (isCreate){
                this.id = id;
                extraInfoLabel.setText("Your organisational unit has " + (isBuy? (data.getUnitByKey(user.getUnit()).getCredits())
                        + " credits." : (data.getInv(user.getUnit(), id)).getQuantity() + " of this asset."));
            }
            else {
                o = data.getBuyByKey(id);
                infoLabel.setText(String.format("Info for %s order", typeText));
                extraInfoLabel.setText(String.format(WRAP_DIALOG, MessageFormat.format("Placed at {0} by a member of {1} for asset {2}; {3}",
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
                == JOptionPane.YES_OPTION) shellPanel(new OrdersTablePage(true, isBuy, false), false);
            else shellPanel(new AssetInfoPage(asset), false);
        }

        void initialiseFields() {
            infoLabel = new JLabel("Placing new %s order for asset: ");
            extraInfoLabel = new JLabel("");
            numberKeyLabel = new JLabel("");
            quantityInput = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
            priceInput = new JSpinner(new SpinnerNumberModel(1,1,null,1));
            //workaround to make the spinners more than 1 column wide, from https://stackoverflow.com/a/14256492/15922463
            JFormattedTextField qtyTF = ((JSpinner.DefaultEditor) quantityInput.getEditor()).getTextField();
            qtyTF.setColumns(4);
            JFormattedTextField priceTF = ((JSpinner.DefaultEditor) priceInput.getEditor()).getTextField();
            priceTF.setColumns(4);
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
        void save() { } //button was removed

        @Override
        void create() {
            try {
                data.placeOrder(DataObjectFactory.newOrderValidated(isBuy, user.getUnit(), id,
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
}
