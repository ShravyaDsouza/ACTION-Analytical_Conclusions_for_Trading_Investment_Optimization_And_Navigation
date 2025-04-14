package components.dashboard.sections;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlaceholderTextField extends JTextField {

    private String placeholder;
    private boolean showingPlaceholder;

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
        this.showingPlaceholder = true;
        super.setText(placeholder);
        super.setForeground(Color.GRAY);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder) {
                    setText("");
                    setForeground(Color.BLACK); // Reset text color
                    showingPlaceholder = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(placeholder);
                    setForeground(Color.GRAY); // Set placeholder color
                    showingPlaceholder = true;
                }
            }
        });
    }

    @Override
    public String getText() {
        return showingPlaceholder ? "" : super.getText();
    }
}
