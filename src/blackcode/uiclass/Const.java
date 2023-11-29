/* DON-CODE */
package blackcode.uiclass;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * This class will provide all the constant values for the program
 *
 * @author DON-CODE
 */
public class Const {

    /**
     * This method will create a StickyNotes++ folder and return its location
     *
     * @return Location of the StickyNotes++ folder
     */
    public String getFILE_PATH() {
        return createFolderAndPropertiesFile("StickyNotes++");
    }

    /**
     * This method will create the database file and return its location
     *
     * @return Location of the database
     */
    public String getDATABASE_PATH() {
        return createFolderAndPropertiesFile("StickyNotes++") + File.separator + "StickyNotes.db";
    }

    /**
     * This method will generate a password HASh using a String keyword
     *
     * @param password password that use for generating a password HASH
     * @return HASH password of the input password
     */
    public String getHASH_PASSWORD(String password) {
        return generateFixedHash(password);
    }

    /**
     * This method will generate a key value using a seed word
     *
     * @param password seed word for generating a key
     * @return key value from the seed word
     */
    public String getKEY(String seed) {
        return generateKeyFromSeed(seed);
    }

    /**
     * This method will return the image path for unlock_15px image
     *
     * @return path for the unlock_15px.png
     */
    public String getUNLOCK_IMG() {
        return "/blackcode/img/unlock_15px.png";
    }

    /**
     * This method will return the image path for stickyNote++ image
     *
     * @return path for the stickyNote++.png
     */
    public String getSTICKYNOTE_IMG() {
        return "/blackcode/img/stickyNote++.png";
    }

    /**
     * This method will return the image path for stickyNote++_15 image
     *
     * @return path for the stickyNote++_15.png
     */
    public String getSTICKYNOTE_IMG15() {
        return "/blackcode/img/stickyNote++_15.png";
    }

    /**
     * This method will return the image path for plus_20px image
     *
     * @return path for the plus_20px.png
     */
    public String getPLUSMATH() {
        return "/blackcode/img/plus_20px.png";
    }

    /**
     * This method will return the image path for plus_math_20px_black image
     *
     * @return path for the plus_math_20px_black.png
     */
    public String getPLUSMATHBLACK() {
        return "/blackcode/img/plus_math_20px_black.png";
    }

    /**
     * This method will create a folder if not exists using a name that input to
     * this in the users Document folder
     *
     * @param folderName Name of the folder
     * @return path for the created folder in users Document folder
     */
    private static String createFolderAndPropertiesFile(String folderName) {
        String userHome = System.getProperty("user.home");
        String documentsFolderPath = userHome + File.separator + "Documents";
        File folder = new File(documentsFolderPath, folderName);
        // Create Folder in Document folder if not there
        folder.mkdir();
        return folder.getAbsolutePath();
    }

    /**
     * This method will generate a HASHed password that using a input password
     * to this, this use SHA-256 algorithm
     *
     * @param password String value of the password
     * @return HASHed password value
     */
    private static String generateFixedHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] passwordBytes = password.getBytes();
            byte[] hashBytes = md.digest(passwordBytes);
            StringBuilder hexString = new StringBuilder();

            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method will generate a random key value from a seed that input
     *
     * @param seed String value for the seed
     * @return Key value using the seed
     */
    public static String generateKeyFromSeed(String seed) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seed.getBytes());
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();

            // Encode the secretKey to a Base64 string
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            return "";
        }
    }
}
