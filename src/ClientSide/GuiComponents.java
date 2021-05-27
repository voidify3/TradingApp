package ClientSide;

// Java imports
import javax.swing.*;
import java.awt.*;

/***
 * @author Johnny Madigan & Scott Peachey
 */
public class GuiComponents {

    // Login panel & widgets
    public JPanel loginPanel = new JPanel(new GridBagLayout());
    public JCheckBox passwordHide = new JCheckBox();
    public JButton loginButton = new JButton("Login");
    public JLabel usernameLabel = new JLabel("Username");
    public JLabel passwordLabel = new JLabel("Password");
    public JLabel invalidLabel = new JLabel(" ");
    public JTextField usernameInput = new JTextField(10);
    public JPasswordField passwordInput = new JPasswordField(10);

    // Shell panel & widgets
    public JPanel shellPanel = new JPanel();
    public JButton homeButton = new JButton("Home");
    public JButton searchButton = new JButton("Search");
    public JLabel welcomeLabel = new JLabel("Welcome back (username)", SwingConstants.CENTER);

    // User home & widgets
    public JPanel userHome = new JPanel();
    public JButton ordersButton = new JButton("Orders");
    public JButton tradeHistoryButton = new JButton("Trade History");
    public JLabel orgUnitLabel = new JLabel("ORG UNIT HERE", SwingConstants.CENTER);
    public JTable userHoldings = new JTable();

    // Admin home & widgets
    public JPanel adminHome = new JPanel();

    // Asset page & widgets
    public JPanel assetPage = new JPanel();
    public JButton daysButton = new JButton("Days");
    public JButton weeksButton = new JButton("Weeks");
    public JButton monthsButton = new JButton("Months");
    public JButton yearsButton = new JButton("Years");
    public JButton buyButton = new JButton("<- Buy Asset");
    public JButton sellButton = new JButton("Sell Asset ->");

    // Menu bar & widgets
    public JMenuBar menuBar = new JMenuBar();
    public JMenuItem logoutMenu = new JMenuItem("Logout");
    public JMenuItem exitMenu = new JMenuItem("Exit");
    public JMenuItem masterUserKey = new JMenuItem("Master User Key");
    public JMenuItem masterAdminKey = new JMenuItem("Master Admin Key");


}
