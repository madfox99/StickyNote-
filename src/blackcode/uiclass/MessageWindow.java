package blackcode.uiclass;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * This class will generate messages for the user
 *
 * <p>
 * DON-CODE
 * </p>
 *
 * @author madfox99
 */
public class MessageWindow {

    /**
     * This constructor will create messages according to the inputs
     */
    public MessageWindow(String about, String message) {
        JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        switch (about.toLowerCase()) {
            case "warning":
                JOptionPane.showMessageDialog(dialog, message, about, JOptionPane.WARNING_MESSAGE);
                break;
            case "error":
                JOptionPane.showMessageDialog(dialog, message, about, JOptionPane.ERROR_MESSAGE);
                break;
            case "information":
                JOptionPane.showMessageDialog(dialog, message, about, JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                throw new AssertionError();
        }
    }
}
