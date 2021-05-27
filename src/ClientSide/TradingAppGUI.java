package ClientSide;

// class imports

import common.Exceptions.*;
import common.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/***
 * @author Johnny Madigan & Scott Peachey (wrote createAndShowGui, menuBar, resizeImg, loginPanel, shellPanel)
 * Sophia Walsh Long (all other code is from after the fork; above methods have also been somewhat refactored)
 */
public class TradingAppGUI {

    // Logged in user for the session
    public static User user = null;

    // Static instances of components & interactions
    public static GuiComponents c = new GuiComponents();
    public static TradingAppData i = new TradingAppData();

    // General variables
    public static final int WIDTH = 800;
    public static final int HEIGHT = 500;
    public static final String DARKGREY = "#4D4D4D";
    public static final String WHITE = "#FCFCFC";

    // Declared here (instead of components class) as it is only used within this scope
    public static final JFrame mainFrame = new JFrame("STONK MACHINE");

    public static void createAndShowGUI() throws IllegalString, AlreadyExists, IOException, DoesNotExist, InvalidPrice, InvalidDate {
        mainFrame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        i.mockObjectsWithPrices();
        loginPanel(); // creates & shows login portal as the first screen
        menuBar(); // declare the menu-bar once here

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    // Menu Bar---------------------------------------------------------------------------------------------------------
    public static void menuBar() {
        c.menuBar = new JMenuBar(); // reset

        JMenu fileMenu = new JMenu("File");
        fileMenu.add("Preference");
        fileMenu.add(c.logoutMenu);
        fileMenu.add(c.exitMenu);
        c.menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add("Cut");
        editMenu.add("Copy");
        editMenu.add("Paste");
        c.menuBar.add(editMenu);

        JMenu viewMenu = new JMenu("View");
        c.menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu("Help");
        c.menuBar.add(helpMenu);

        JMenu devMenu = new JMenu("Dev Tools");
        devMenu.add(c.masterUserKey);
        devMenu.add(c.masterAdminKey);
        c.menuBar.add(devMenu);

        // Listeners
        GuiListeners.logoutListener();
        GuiListeners.exitListener();
        GuiListeners.masterUserKeyListener();
        GuiListeners.masterAdminKeyListener();
        GuiListeners.homeListener();

        // Boilerplate
        mainFrame.setJMenuBar(c.menuBar);
        TradingAppGUI.mainFrame.revalidate();
    }

    // Resizes images---------------------------------------------------------------------------------------------------
    /***
     *  from https://stackoverflow.com/questions/244164/how-can-i-resize-an-image-using-java
      */
    public static BufferedImage resizeImg(Image originalImg, int width, int height, boolean preserveAlpha) {
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
    public static void loginPanel() throws AlreadyExists, IllegalString {
        c.loginPanel = new JPanel(new GridBagLayout()); // reset

        JPanel loginBox = new JPanel();
        JPanel login = new JPanel(new GridBagLayout());

        c.loginPanel.setBackground(Color.decode(DARKGREY));

        loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.PAGE_AXIS));
        loginBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        c.invalidLabel.setMinimumSize(new Dimension(100, 50));
        c.loginButton.setContentAreaFilled(false);
        c.loginButton.setOpaque(true);
        c.passwordHide.setToolTipText("Show & hide password...");

        // Reset fields & checkbox
        c.passwordInput.setText("");
        c.usernameInput.setText("");
        c.invalidLabel.setText(" "); // need space for the label to have a fixed height
        c.passwordHide.setSelected(false);

        GridBagConstraints cords = new GridBagConstraints();

        BufferedImage img;
        JLabel bannerLabel = new JLabel();
        try {
            img = ImageIO.read(new File("./Images/GUI images/banner.png"));
            BufferedImage newImg = resizeImg(img, 350,62, false);
            bannerLabel = new JLabel(new ImageIcon(newImg));
        } catch (IOException e) {
            bannerLabel.setText("STONK MACHINE");
        }

        // Position the interactive components (text fields, buttons etc)
        cords.insets =new Insets(5,5,5,5);
        cords.gridy = 1;
        cords.gridx = -1;
        cords.gridwidth = 1;
        login.add(c.usernameLabel, cords);
        cords.gridx += 2;
        login.add(c.usernameInput, cords);

        cords.gridy++; // password row is always below username row
        cords.gridx = -1;
        login.add(c.passwordLabel, cords);
        cords.gridx += 2;
        login.add(c.passwordInput, cords);
        cords.gridx++;
        login.add(c.passwordHide,cords);

        cords.gridy++; // login button is always below password row
        cords.gridx = 0;
        cords.gridwidth = 3;
        login.add(c.loginButton, cords);

        cords.gridy++; // warning message is always below login button
        login.add(c.invalidLabel, cords);

        // Add panels together
        loginBox.add(bannerLabel);
        loginBox.add(login);
        c.loginPanel.add(loginBox);

        // Listeners
        GuiListeners.loginActionListener();
        GuiListeners.loginKeyListener();
        GuiListeners.passwordHiddenListener();

        // Boilerplate
        mainFrame.setContentPane(c.loginPanel);
        mainFrame.revalidate();
    }

    // Shell template (content depends on the parameter)----------------------------------------------------------------
    public static void shellPanel(JPanel content, boolean welcomeLabel) {
        c.shellPanel = new JPanel(); // reset
        c.shellPanel.setBackground(Color.decode(WHITE));

        c.homeButton.setToolTipText("Go to the home screen");
        c.searchButton.setToolTipText("Go to the search screen");
        c.welcomeLabel.setText(String.format("Welcome back %s", user.getUsername()));
        c.welcomeLabel.setText(String.format("Welcome back %s!", user.getUsername()));
        c.orgUnitLabel.setText(user.getUnit().toUpperCase());

        c.shellPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        c.shellPanel.setLayout(new BoxLayout(c.shellPanel, BoxLayout.PAGE_AXIS));

        JPanel row1 = new JPanel();
        // keeps row 1 fixed in height so no matter what the content is (a table, buttons, graphs)
        // it won't change sizes (sometimes takes up most of the screen!)
        row1.setBackground(Color.decode(WHITE));
        row1.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        row1.setMaximumSize(new Dimension(WIDTH, 50));
        row1.setLayout(new BorderLayout());
        row1.add(c.homeButton, BorderLayout.WEST);
        row1.add(c.orgUnitLabel, BorderLayout.CENTER);
        row1.add(c.searchButton, BorderLayout.EAST);

        JPanel row2 = new JPanel();
        row2.setBackground(Color.decode(WHITE));
        row2.setLayout(new FlowLayout());
        row2.add(c.welcomeLabel);

        // The parameter panel is added here, always a panel
        JPanel row3 = new JPanel();
        row3.setBackground(Color.decode(WHITE));
        row3.setPreferredSize(new Dimension(700,400));
        row3.setLayout(new FlowLayout());
        row3.add(content);

        c.shellPanel.add(row1);
        c.shellPanel.add(Box.createVerticalStrut(20));
        if (welcomeLabel) {
            c.shellPanel.add(row2);
            c.shellPanel.add(Box.createVerticalStrut(20));
        }
        c.shellPanel.add(row3);

        // Listeners

        // Boilerplate
        mainFrame.setContentPane(c.shellPanel);
        mainFrame.revalidate();
    }



}
