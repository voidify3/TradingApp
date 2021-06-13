package ClientSide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/***
 * This is a simple class for filtering text in a search box
 * Inspired by methods from Stack Overflow, these have been modified to suit our app.
 * @see <a href="https://stackoverflow.com/a/28555827"/>
 */
public class GuiSearch extends JComboBox<String> {

    private List<String> entries;
    public List<String> getEntries() {
        return entries;
    }

    public GuiSearch(List<String> entries) {
        super(entries.toArray(new String[0]));
        this.entries = entries;
        this.setEditable(true);
        this.setSelectedItem(null);

        final JTextField textfield = (JTextField) this.getEditor().getEditorComponent();

        // Modification
        textfield.setForeground(Color.GRAY);
        textfield.setText("Search");

        // Listen for key presses.
        textfield.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke) {
                SwingUtilities.invokeLater(() -> {
                    //On key press filter the list.
                    //If there is "text" entered in text field of this combo use that "text" for filtering.
                    comboFilter(textfield.getText());
                });
            }
        });
    }

     // Build a list of entries that match the given filter.
    public void comboFilter(String enteredText) {
        List<String> entriesFiltered = new ArrayList<>();

        for (String entry : getEntries()) {
            if (entry.toLowerCase().contains(enteredText.toLowerCase())) {
                entriesFiltered.add(entry);
            }
        }

        if (entriesFiltered.size() > 0) {
            this.setModel(new DefaultComboBoxModel(entriesFiltered.toArray()));
            this.setSelectedItem(enteredText);
            this.showPopup();
        } else {
            this.hidePopup();
        }
    }

}

