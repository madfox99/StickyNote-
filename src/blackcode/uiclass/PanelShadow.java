/* DON-CODE */
package blackcode.uiclass;

import blackcode.uiclass.ShadowType;
import blackcode.uiclass.ShadowRenderer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * This class will create a JPanel with a background shadow
 *
 * <p>
 * DON-CODE
 * </p>
 *
 * @author madfox99
 */
public class PanelShadow extends JPanel {

    /**
     * This field represents the type of the shadow, default value as CENTER
     */
    private ShadowType shadowType = ShadowType.CENTER;

    /**
     * This field represents the size of the shadow, default value as 6
     */
    private int shadowSize = 6;

    /**
     * This field represents the opacity of the shadow, default value as 0.5
     */
    private float shadowOpacity = 0.5f;

    /**
     * This field represents the colour of the shadow, default value as BLACK
     */
    private Color shadowColor = Color.BLACK;

    /**
     * This method will return the shadow type of the JPanel
     *
     * @return Assigned shadow type
     */
    public ShadowType getShadowType() {
        return shadowType;
    }

    /**
     * This method will set the shadow type of the JPanel
     *
     * @param shadowType Type of the shadow
     */
    public void setShadowType(ShadowType shadowType) {
        this.shadowType = shadowType;
    }

    /**
     * This method will return the shadow size of the JPanel
     *
     * @return Assigned shadow size
     */
    public int getShadowSize() {
        return shadowSize;
    }

    /**
     * This method will set the shadow size of the JPanel
     *
     * @param shadowSize Size of the shadow
     */
    public void setShadowSize(int shadowSize) {
        this.shadowSize = shadowSize;
    }

    /**
     * This method will return the shadow opacity of the JPanel
     *
     * @return Assigned shadow opacity
     */
    public float getShadowOpacity() {
        return shadowOpacity;
    }

    /**
     * This method will set the shadow opacity of the JPanel
     *
     * @param shadowOpacity Opacity of the shadow
     */
    public void setShadowOpacity(float shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
    }

    /**
     * This method will return the shadow colour of the JPanel
     *
     * @return Assigned shadow colour
     */
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * This method will set the shadow colour of the JPanel
     *
     * @param shadowColor Colour of the shadow
     */
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    /**
     * This method will set the shadow opacity to false
     */
    public PanelShadow() {
        setOpaque(false);
    }

    @Override
    /**
     * This method will override the paint component
     */
    protected void paintComponent(Graphics grphcs) {
        createShadow(grphcs);
        super.paintComponent(grphcs);
    }

    /**
     * This method will create a custom shadow
     */
    private void createShadow(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        int size = shadowSize * 2;
        int x = 0;
        int y = 0;
        int width = getWidth() - size;
        int height = getHeight() - size;
        if (shadowType == ShadowType.TOP) {
            x = shadowSize;
            y = size;
        } else if (shadowType == ShadowType.BOT) {
            x = shadowSize;
            y = 0;
        } else if (shadowType == ShadowType.TOP_LEFT) {
            x = size;
            y = size;
        } else if (shadowType == ShadowType.TOP_RIGHT) {
            x = 0;
            y = size;
        } else if (shadowType == ShadowType.BOT_LEFT) {
            x = size;
            y = 0;
        } else if (shadowType == ShadowType.BOT_RIGHT) {
            x = 0;
            y = 0;
        } else {
            //  Center
            x = shadowSize;
            y = shadowSize;
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(getBackground());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, width, height, 0, 0);
//        g.fillOval(0, 0, width, width);
        //  Create Shadow
        ShadowRenderer render = new ShadowRenderer(shadowSize, shadowOpacity, shadowColor);
        g2.drawImage(render.createShadow(img), 0, 0, null);
        g2.drawImage(img, x, y, null);
    }
}
