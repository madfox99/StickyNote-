/* DON-CODE */
package blackcode.uiclass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is use to do all database related activities
 *
 * @author DON-CODE
 */
public class Database {

    /**
     * This field represents the database connection
     */
    static Connection connection;

    /**
     * This field represents fixed byte values for the SALT
     */
    private static final byte[] SALT = new byte[]{
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
    };

    /**
     * This method will create the database connection if the connection is null
     * or close
     *
     * @return Database connection
     */
    public static Connection dbConnect() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + new Const().getDATABASE_PATH());
                createTablesIfNotExist(connection);
            }
            return connection;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This method will insert the note value in the Notes table in the database
     *
     * @param userId Id value of the current user
     * @param title title value of the note
     * @param noteObject Note value as JTextPane object
     * @param plainText Note as a plain text
     * @return Id value of the created note from this method in the database
     */
    public static int saveNote(int userId, String title, ObjectOutputStream noteObject, String plainText) {
        int noteId = -1; // Initialize noteId to -1 as a default value indicating an error

        try {
            Connection conn = dbConnect();
            if (conn == null) {
                return noteId;
            }

            // Serialize the noteObject into a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(noteObject);
            byte[] serializedNoteObject = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();

            String insertNoteQuery = "INSERT INTO Notes (user_id, title, note_blob, plain_text, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertNoteQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, title);
            preparedStatement.setBytes(3, serializedNoteObject);
            preparedStatement.setString(4, plainText);
            Timestamp currentTime = new Timestamp(new Date().getTime());
            preparedStatement.setTimestamp(5, currentTime);
            preparedStatement.setTimestamp(6, currentTime);

            // Execute the insert statement and get the generated keys
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    noteId = generatedKeys.getInt(1); // Get the generated note_id
                }
            }

            preparedStatement.close();
        } catch (SQLException | IOException e) {
            new MessageWindow("Error", "Database connection error");
        }

        return noteId;
    }

    /**
     * This method will get all notes that belong to the current user as plain
     * text
     *
     * @param userId Id value of the current user
     * @return List of Note Ids & the notes as plain text from the database
     */
    public static List<Pair<Integer, String>> getAllPlainTextNotes(int userId) {
        List<Pair<Integer, String>> noteAndPlainTextList = new ArrayList<>();

        Connection conn = dbConnect();
        if (conn == null) {
            return noteAndPlainTextList; // Return an empty list to indicate an error
        }

        try {
            String selectNoteQuery = "SELECT note_id, plain_text FROM Notes WHERE user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(selectNoteQuery);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int noteId = resultSet.getInt("note_id");
                String plainText = resultSet.getString("plain_text");
                noteAndPlainTextList.add(Pair.of(noteId, plainText));
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            new MessageWindow("Error", "Database error");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                new MessageWindow("Error", "Database connection error");
            }
        }

        return noteAndPlainTextList;
    }

    /**
     * This method will get all notes that has specific character set in the
     * plain text
     *
     * @param userId Id value of the current user
     * @param keyword keyword that user want to search
     * @return Note Id & the note as plain text from the database
     */
    public static List<Pair<Integer, String>> getPlainTextNotesByKeyword(int userId, String keyword) {
        List<Pair<Integer, String>> filteredNoteAndPlainTextList = new ArrayList<>();

        Connection conn = dbConnect();
        if (conn == null) {
            return filteredNoteAndPlainTextList; // Return an empty list to indicate an error
        }

        try {
            // Modify the SQL query to include a WHERE clause for searching by keyword
            String selectNoteQuery = "SELECT note_id, plain_text FROM Notes WHERE user_id = ? AND plain_text LIKE ?";
            PreparedStatement preparedStatement = conn.prepareStatement(selectNoteQuery);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, "%" + keyword + "%"); // Use "%" to search for keyword within the text

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int noteId = resultSet.getInt("note_id");
                String plainText = resultSet.getString("plain_text");
                filteredNoteAndPlainTextList.add(Pair.of(noteId, plainText));
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            new MessageWindow("Error", "Database error");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                new MessageWindow("Error", "Database connection error");
            }
        }

        return filteredNoteAndPlainTextList;
    }

    /**
     * This method will get all notes that belong to the current user as the
     * full note and the plain text
     *
     * @param noteId Id value of the note
     * @return List of Note BLOB values & the plain note values from the
     * database
     */
    public static Map<String, Object> getFullNote(int noteId) {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dbConnect(); PreparedStatement preparedStatement = conn.prepareStatement("SELECT note_blob, plain_text FROM Notes WHERE note_id = ?")) {
            preparedStatement.setInt(1, noteId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Read the binary data from the database
                    byte[] noteBlob = resultSet.getBytes("note_blob");
                    result.put("note_blob", noteBlob);
                    result.put("plainText", resultSet.getString("plain_text"));
                    return result;
                } else {
                    return null; // Note not found
                }
            }
        } catch (SQLException e) {
            new MessageWindow("Error", "Database connection error");
            return null; // Error occurred
        }
    }

    /**
     * This method will update the note that belong to the current user
     *
     * @param noteId Id value of the note
     * @param title Title value of the note
     * @param styledTextPane JTextPane that use for the Note
     * @return True or False value based on the operation success or not
     */
    public static boolean updateNote(int noteId, String title, JTextPane styledTextPane) {
        try {
            Connection conn = dbConnect();
            if (conn == null) {
                return false;
            }

            // Serialize the JSON data
            StyledDocument styledDocument = styledTextPane.getStyledDocument();
            int length = styledDocument.getLength();
            JSONArray characterDataArray = new JSONArray();

            for (int i = 0; i < length; i++) {
                AttributeSet attributes = styledDocument.getCharacterElement(i).getAttributes();
                String text = styledTextPane.getText(i, 1);

                JSONObject characterData = new JSONObject();
                characterData.put("text", text);
                characterData.put("bold", StyleConstants.isBold(attributes));
                characterData.put("italic", StyleConstants.isItalic(attributes));
                characterData.put("underline", StyleConstants.isUnderline(attributes));
                characterData.put("strikethrough", StyleConstants.isStrikeThrough(attributes));
                characterData.put("fontSize", StyleConstants.getFontSize(attributes));
                // Add more attributes as needed.

                characterDataArray.put(characterData);
            }

            String jsonString = characterDataArray.toString();

            // Create a byte array from the JSON string
            byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);

            String updateNoteQuery = "UPDATE Notes SET title = ?, note_blob = ?, plain_text = ?, updated_at = ? WHERE note_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(updateNoteQuery);
            preparedStatement.setString(1, title);

            // Set the JSON data as a blob
            ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonBytes);
            preparedStatement.setBinaryStream(2, inputStream, jsonBytes.length);

            String plainText = styledTextPane.getText(); // Get plain text
            Timestamp currentTime = new Timestamp(new Date().getTime());

            preparedStatement.setString(3, plainText); // Save plain text
            preparedStatement.setTimestamp(4, currentTime);
            preparedStatement.setInt(5, noteId);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            return true;
        } catch (SQLException | BadLocationException e) {
            new MessageWindow("Error", "Database connection error");
            return false;
        }
    }

    /**
     * This method will delete the note that belong to the current user
     *
     * @param noteId Id value of the note
     * @return True or False value based on the operation success or not
     */
    public static boolean deleteNote(int noteId) {
        try {
            Connection conn = dbConnect();
            if (conn == null) {
                return false;
            }

            String deleteNoteQuery = "DELETE FROM Notes WHERE note_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(deleteNoteQuery);
            preparedStatement.setInt(1, noteId);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            return true;
        } catch (SQLException e) {
            new MessageWindow("Error", "Database connection error");
            return false;
        }
    }

    /**
     * This method will count total notes that belong to the current user
     *
     * @param userId Id value of the user
     * @return Number of notes that the current user have
     */
    public static int countUserNotes(int userId) {
        Connection conn = dbConnect();
        if (conn == null) {
            return -1; // Return -1 to indicate an error
        }

        try {
            String countNotesQuery = "SELECT COUNT(*) FROM Notes WHERE user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(countNotesQuery);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int noteCount = resultSet.getInt(1);
                return noteCount; // Return the count of notes for the user
            }
        } catch (SQLException e) {
            new MessageWindow("Error", "Database error");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                new MessageWindow("Error", "Database connection error");
            }
        }

        return 0; // If there are no notes for the user, return 0
    }

    /**
     * This method will check if the note exists on the database or not
     *
     * @param noteId Id value of the note
     * @return True or False by the present of the note
     */
    public static boolean doesNoteExist(int noteId) {
        Connection conn = dbConnect();
        if (conn == null) {
            return false; // Database connection error
        }

        try {
            String checkNoteQuery = "SELECT COUNT(*) FROM Notes WHERE note_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(checkNoteQuery);
            preparedStatement.setInt(1, noteId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0; // Return true if a note with the given noteId exists
            }
        } catch (SQLException e) {
            new MessageWindow("Error", "Databse error");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                new MessageWindow("Error", "Database connection error");
            }
        }

        return false; // Note not found in the database
    }

    /**
     * This method will save the user details on the User table in the database
     *
     * @param username Name of the user
     * @param passwordHash password HASH value of the entered password
     * @return True or False value based on the operation success or not
     */
    public static boolean saveUser(String username, String passwordHash) {
        try {
            Connection conn = dbConnect();
            if (conn == null) {
                return false;
            }

            passwordHash = new Const().getHASH_PASSWORD(passwordHash);
            String encryptionKey = new Const().getKEY(passwordHash);

            if (isUsernameExists(username, conn)) {
                new MessageWindow("Error", "Username already exists. Please choose a different username");
                return false;
            } else {
                String insertUserQuery = "INSERT INTO Users (username, password_hash, encryption_key) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(insertUserQuery);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, passwordHash);
                preparedStatement.setString(3, encryptionKey);
                preparedStatement.executeUpdate();

                preparedStatement.close();
                new MessageWindow("Information", "User added successfully");
                return true;
            }
        } catch (SQLException e) {
            new MessageWindow("Error", "Database connection error");
            return false;
        }
    }

    /**
     * This method will update the username & the password on the User table in
     * the database, if the user name already exists this will generate a
     * message to the user
     *
     * @param userId Id of the user
     * @param username new username of the user
     * @param newPassword password HASH value of the entered new password
     * @return True or False value based on the operation success or not
     */
    public static boolean updateUser(int userId, String username, String newPassword) {
        try {
            Connection conn = dbConnect();
            if (conn == null) {
                return false;
            }

            newPassword = new Const().getHASH_PASSWORD(newPassword);

            if (isUsernameExists(username, conn)) {
                new MessageWindow("Error", "Username already exists. Please choose a different username");
                return false;
            } else {
                String updateUserQuery = "UPDATE Users SET password_hash = ?, username = ? WHERE user_id = ?";
                PreparedStatement preparedStatement = conn.prepareStatement(updateUserQuery);
                preparedStatement.setString(1, newPassword);
                preparedStatement.setString(2, username);
                preparedStatement.setInt(3, userId);
                preparedStatement.executeUpdate();

                preparedStatement.close();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * This method will update the user password on the User table in the
     * database
     *
     * @param userId Id of the user
     * @param newPassword password HASH value of the entered new password
     * @return True or False value based on the operation success or not
     */
    public static boolean updateUser(int userId, String newPassword) {
        try {
            Connection conn = dbConnect();
            if (conn == null) {
                return false;
            }

            newPassword = new Const().getHASH_PASSWORD(newPassword);

            String updateUserQuery = "UPDATE Users SET password_hash = ? WHERE user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(updateUserQuery);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * This method will delete the user details with the notes that belong to
     * the user
     *
     * @param userId Id of the user
     * @return True or False value based on the operation success or not
     */
    public static boolean deleteUser(int userId) {
        Connection conn = dbConnect();
        if (conn == null) {
            return false;
        }

        try {
            // 1. Retrieve note IDs associated with the user
            String retrieveNoteIdsQuery = "SELECT note_id FROM Notes WHERE user_id = ?";
            PreparedStatement retrieveNoteIdsStatement = conn.prepareStatement(retrieveNoteIdsQuery);
            retrieveNoteIdsStatement.setInt(1, userId);
            ResultSet noteIdsResultSet = retrieveNoteIdsStatement.executeQuery();

            // 2. Delete the user's record from the Users table
            String deleteUserQuery = "DELETE FROM Users WHERE user_id = ?";
            PreparedStatement deleteUserStatement = conn.prepareStatement(deleteUserQuery);
            deleteUserStatement.setInt(1, userId);
            deleteUserStatement.executeUpdate();

            // 3. Delete each note associated with the user
            while (noteIdsResultSet.next()) {
                int noteId = noteIdsResultSet.getInt("note_id");
                String deleteNoteQuery = "DELETE FROM Notes WHERE note_id = ?";
                PreparedStatement deleteNoteStatement = conn.prepareStatement(deleteNoteQuery);
                deleteNoteStatement.setInt(1, noteId);
                deleteNoteStatement.executeUpdate();
            }

            // Close the statements
            retrieveNoteIdsStatement.close();
            deleteUserStatement.close();

            return true;
        } catch (SQLException e) {
            new MessageWindow("Error", "Database error");
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                new MessageWindow("Error", "Databse connection error");
            }
        }
    }

    /**
     * This method will check if the username exists or not in the database
     *
     * @param username name of the user
     * @param conn database connection
     * @return True or False value based on the user exists or not
     */
    private static boolean isUsernameExists(String username, Connection conn) {
        try {
            String checkUsernameQuery = "SELECT COUNT(*) FROM Users WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(checkUsernameQuery);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            new MessageWindow("Error", "Database error");
        }
        return false;
    }

    /**
     * This method will check if the entered username and the password is
     * correct or not
     *
     * @param username name of the user
     * @param password user password
     * @return User credentials with user Id, username and the encryption key of
     * the user
     */
    public static Map<String, Object> checkUserCredentials(String username, String password) {
        Connection conn = dbConnect();
        if (conn == null) {
            return null;
        }

        try {
            String checkUserQuery = "SELECT user_id, username, encryption_key FROM Users WHERE username = ? AND password_hash = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(checkUserQuery);
            preparedStatement.setString(1, username);
            String hashedPassword = new Const().getHASH_PASSWORD(password);
            preparedStatement.setString(2, hashedPassword);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String resultUsername = resultSet.getString("username"); // Use a different variable name
                String encryptionKey = resultSet.getString("encryption_key");
                Map<String, Object> userCredentials = new HashMap<>();
                userCredentials.put("userId", userId);
                userCredentials.put("username", resultUsername); // Use the new variable name
                userCredentials.put("encryptionKey", encryptionKey);
                return userCredentials;
            }
        } catch (SQLException e) {
            new MessageWindow("Error", "Databse error");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                new MessageWindow("Error", "Database connection error");
            }
        }

        return null; // User credentials not found
    }

    /**
     * This method will create the User & Notes tables if not exists on the
     * database
     *
     * @param conn name of the user
     */
    private static void createTablesIfNotExist(Connection conn) {
        try {
            Statement statement = conn.createStatement();

            // Create Users Table if it doesn't exist
            statement.execute("CREATE TABLE IF NOT EXISTS Users ("
                    + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT UNIQUE,"
                    + "password_hash TEXT,"
                    + "encryption_key TEXT"
                    + ");");

            // Create Notes Table if it doesn't exist (with a new plain_text column)
            statement.execute("CREATE TABLE IF NOT EXISTS Notes ("
                    + "note_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER,"
                    + "title TEXT,"
                    + "note_blob BLOB,"
                    + "plain_text TEXT," // New column for plain text
                    + "created_at TIMESTAMP,"
                    + "updated_at TIMESTAMP,"
                    + "FOREIGN KEY (user_id) REFERENCES Users(user_id)"
                    + ");");

            // Add other table creation statements here if needed
            statement.close();
        } catch (Exception e) {
            new MessageWindow("Error", "Database error");
        }
    }

    /**
     * This method will generate a security key using a pin
     *
     * @param pin PIN number
     * @return Security key based on the pin that insert
     */
    private static SecretKey deriveKeyFromPin(String pin) throws Exception {
        char[] pinChars = pin.toCharArray();
        KeySpec keySpec = new PBEKeySpec(pinChars, SALT, 65536, 256);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * This method will create a encrypted backup file and save it in a given
     * location
     *
     * @param location Location to save the file
     * @return True or False value based on the operation success or not &
     * output file path & pin for the encrypted file & the file name
     */
    public static String[] encryptBackupFile(String location) {
        try {
            String[] result = new String[4]; // Create a string array to store the result

            String pin = generateRandomPin();
            String fileName = "Backup.sn";
            String outputFilePath = location + File.separator + fileName;

            // Initialize the result with "false" to indicate failure
            result[0] = "false"; // Status of the progress
            result[1] = outputFilePath; // Backup file path
            result[2] = pin; // PIN
            result[3] = fileName; // PIN

            // Generate a random IV (Initialization Vector)
            byte[] iv = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Derive a valid AES key from the PIN using PBKDF2
            SecretKey key = deriveKeyFromPin(pin);

            // Create the cipher in encryption mode
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);

            FileInputStream inputFileStream = new FileInputStream(new Const().getDATABASE_PATH());
            FileOutputStream outputFileStream = new FileOutputStream(outputFilePath);

            // Write the IV to the output file
            outputFileStream.write(iv);

            // Encrypt the file
            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputFileStream, cipher)) {
                int read;
                byte[] buffer = new byte[4096];
                while ((read = inputFileStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, read);
                }
            }

            inputFileStream.close();
            outputFileStream.close();

            // Set the status to "true" to indicate success
            result[0] = "true";

            return result;
        } catch (Exception e) {
            new MessageWindow("Error", "File encryption error");
        }

        return new String[]{"false", "", "", ""}; // Return a default result for failure
    }

    /**
     * This method will decrypt the backup file and import it as the new
     * database
     *
     * @param inputFilePath Backup file path
     * @param pin Pin to decrypt backup file
     * @return True or False value based on the operation success or not
     */
    public static boolean decryptBackupFile(String inputFilePath, String pin) {
        try {
            FileInputStream inputFileStream = new FileInputStream(inputFilePath);
            FileOutputStream outputFileStream = new FileOutputStream(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "backup.db");

            // Read the IV from the input file
            byte[] iv = new byte[16];
            inputFileStream.read(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Derive a valid AES key from the PIN using PBKDF2
            SecretKey key = deriveKeyFromPin(pin);

            // Create the cipher in decryption mode
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

            // Decrypt the file
            try (CipherInputStream cipherInputStream = new CipherInputStream(inputFileStream, cipher)) {
                int read;
                byte[] buffer = new byte[4096];
                while ((read = cipherInputStream.read(buffer)) != -1) {
                    outputFileStream.write(buffer, 0, read);
                }
            }

            inputFileStream.close();
            outputFileStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method will generate a random pin number that maximum length of 6
     */
    public static String generateRandomPin() {
        Random random = new Random();
        int min = 0; // The minimum 6-digit number
        int max = 999999; // The maximum 6-digit number
        int randomPin = random.nextInt((max - min) + 1) + min;
        return Integer.toString(randomPin);
    }

}
