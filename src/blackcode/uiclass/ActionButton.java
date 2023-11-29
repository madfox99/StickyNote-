package blackcode.uiclass;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 * This class will create a action button
 *
 * <p>
 * DON-CODE
 * </p>
 *
 * @author madfox99
 */
public class ActionButton extends JButton {

    /**
     * This field represents the true or false value for when user press the
     * mouse
     */
    private boolean mousePress;

    /**
     * This constructor will create a action button
     */
    public ActionButton() {
        setContentAreaFilled(false);
        setHorizontalAlignment(JButton.CENTER); // Center-align the text
        setBorder(new EmptyBorder(3, 3, 3, 3));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                mousePress = true;
            }

            public void mouseReleased(MouseEvent me) {
                mousePress = false;
            }
        });
    }

    /**
     * This method will override the paint Component for the action button to
     * modify it
     *
     * @param g Graphics value for the button
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        if (mousePress) {
            g2.setColor(new Color(102, 102, 102));
        } else {
            g2.setColor(new Color(51, 51, 51));
        }
        g2.fill(new Ellipse2D.Double(x, y, size, size));
        g2.dispose();
        super.paintComponent(g);
    }

}
