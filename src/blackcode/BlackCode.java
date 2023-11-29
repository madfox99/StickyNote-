/* DON-CODE */
package blackcode;

import blackcode.ui.SplashScreen;
import blackcode.ui.UserLoginWindow;
import blackcode.uiclass.Const;

/**
 * This is the main class for this project
 *
 * <p>
 * DON-CODE
 * </p>
 *
 * @author madfox99
 */
public class BlackCode {

    /**
     * This field represents the constant value for this project
     */
    public static Const cons = new Const();

    /**
     * This main method create a Splash screen and after that it will show the
     * Login screen to the user
     *
     * @param args
     */
    public static void main(String[] args) {
        /**
         * This field represents the splash screen
         */
        SplashScreen splashScreen = new SplashScreen();
        try {
            Thread.sleep(50); // 50 mills waiting
            splashScreen.dispose();
            new UserLoginWindow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
