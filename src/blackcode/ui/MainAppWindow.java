/* DON-CODE */
package blackcode.ui;

import static blackcode.BlackCode.cons;
import blackcode.uiclass.Database;
import blackcode.uiclass.MessageWindow;
import blackcode.uiclass.TableActionCellEditor;
import blackcode.uiclass.TableActionCellRender;
import blackcode.uiclass.TableActionEvent;
import blackcode.uiclass.WordWrapCellRenderer;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.AWTException;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class is the Main user interface of the application
 *
 * <p>
 * DON-CODE
 * </p>
 *
 * @author madfox99
 */
public class MainAppWindow extends javax.swing.JFrame {

    /**
     * This field represents the value for the application current X and Y
     * coordinates
     */
    private int xx, yy;
    /**
     * This field represents the value if the user click the Settings button or
     * not
     */
    private boolean isSettingsClicked = false; // Set false as default value
    /**
     * This field represents the value if the application is in the system tray
     * or not
     */
    private boolean isAppIntheSystemTray = false;
    /**
     * This field represents the value for current panel the user can see
     */
    private String currentCardPanel = "labelPanel";
    /**
     * This field represents the value for current user Id
     */
    public static int userId;
    /**
     * This field represents the value for current user name
     */
    private String username;
    /**
     * This field represents the value of the encryption key for the current
     * user
     */
    private String encryptionKey;
    /**
     * This field represents the value of the pin number for the current user
     */
    public String pin;
    /**
     * This field represents the value of the file path from the file chooser
     */
    public String filePath;
    /**
     * This field represents the value for card layouts
     */
    CardLayout cardMainNavigationPanel, cardMainCard;
    /**
     * This field represents the value of the Table model
     */
    public static DefaultTableModel model;
    /**
     * This field represents the value of the note id
     */
    private int noteID;
    /**
     * This field represents the value for the system tray
     */
    SystemTray systemTray;
    /**
     * This field represents the value for the system tray icon
     */
    TrayIcon trayIcon;

    /**
     * This constructor will show the user interface of the Main application
     * according to the login user
     *
     * @param userId Database Id value of the user
     * @param username Username of the user
     * @param encryptionKey File encryption key of the user
     */
    public MainAppWindow(int userId, String username, String encryptionKey) {
        initComponents();
        setBackground(new Color(0, 0, 0, 0)); // Transparent background color
        FlatDarkLaf.setup(); // Set FlatLf Dark theme for that application
        this.userId = userId;
        this.username = username;
        this.encryptionKey = encryptionKey;
        this.setLocationRelativeTo(null); // Set location to center        
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(cons.getSTICKYNOTE_IMG()))); // Set app icon
        this.settingUserUsername.setText(username);
        this.settingUpdateUsername.setText(username);
        setWindowSize(); // Set Appilcation Window Size
        countNotes(); // Set value in noteCountLabel
        this.show();
        // Setup card layout        
        cardMainNavigationPanel = (CardLayout) mainNavigationPanel.getLayout();
        cardMainCard = (CardLayout) mainCard.getLayout();
        // JTable setup
        jtable.getTableHeader().setDefaultRenderer(new NoHeaderRenderer());
        jtable.setRowHeight(0, 60);
        // Jtable Column Setup
        TableColumn column;
        // For Column 0
        column = jtable.getColumnModel().getColumn(0);
        column.setCellRenderer(new WordWrapCellRenderer());
        // For Column 1        
        column = jtable.getColumnModel().getColumn(1);
        column.setMinWidth(23);
        column.setMaxWidth(23);
        // For Column 2
        column = jtable.getColumnModel().getColumn(2);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        // Assign model
        model = (DefaultTableModel) jtable.getModel();
        // Add Table event
        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row) {
                new NoteAppWindow((int) model.getValueAt(row, 2), row);
                noteCountLabel.setText(String.valueOf(new Database().countUserNotes(userId)));
            }

            @Override
            public void onDelete(int row) {
                if (jtable.isEditing()) {
                    jtable.getCellEditor().stopCellEditing();
                }
                int userChoice = JOptionPane.showConfirmDialog(null, "Do you want to Delete?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (userChoice == JOptionPane.YES_OPTION) {
                    new Database().deleteNote((int) model.getValueAt(row, 2));
                    model.removeRow(row);
                    noteCountLabel.setText(Integer.toString(new Database().countUserNotes(userId)));
                }
            }
        };
        // JTable Column 1 Renderer
        jtable.getColumnModel().getColumn(1).setCellRenderer(new TableActionCellRender());
        // JTable Column 1 Editor
        jtable.getColumnModel().getColumn(1).setCellEditor(new TableActionCellEditor(event));
        setSavedNotesInTable();

    }

    /**
     * This class will render the Header less table
     */
    class NoHeaderRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return new JLabel();
        }
    }

    /**
     * This method will add the rows of plain texts to the table according to
     * the logged in user
     */
    private void setSavedNotesInTable() {
        List<Pair<Integer, String>> noteAndPlainTextList = new Database().getAllPlainTextNotes(userId);
        for (Pair<Integer, String> pair : noteAndPlainTextList) {
            model.addRow(new Object[]{pair.getRight(), null, pair.getLeft()});
        }
    }

    /**
     * This method will add the rows of plain texts to the table according to
     * the keyword that user want to search
     *
     * @param keyword Keyword that want to search in the notes
     */
    private void setSavedNotesInTableNotesByKeyword(String keyword) {
        model.setRowCount(0);
        List<Pair<Integer, String>> noteAndPlainTextList = new Database().getPlainTextNotesByKeyword(userId, keyword);
        for (Pair<Integer, String> pair : noteAndPlainTextList) {
            model.addRow(new Object[]{pair.getRight(), null, pair.getLeft()});
        }
    }

    /**
     * This method will add the application into the system tray while it is
     * close
     */
    private void systemTray() {
        if (!isAppIntheSystemTray) {
            systemTray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(cons.getSTICKYNOTE_IMG15())));
            PopupMenu popupMenu = new PopupMenu();
            MenuItem show = new MenuItem("Show");
            show.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                }
            });
            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            popupMenu.add(show);
            popupMenu.add(exit);
            trayIcon.setPopupMenu(popupMenu);
            try {
                systemTray.add(trayIcon);
            } catch (AWTException ex) {
                Logger.getLogger(MainAppWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
            isAppIntheSystemTray = true;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        shadowPanel = new blackcode.uiclass.PanelShadow();
        topNavBar = new javax.swing.JPanel();
        plusPanel = new javax.swing.JPanel();
        plusLabel = new javax.swing.JLabel();
        closePanel = new javax.swing.JPanel();
        closeLabel = new javax.swing.JLabel();
        bottomNavBar = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        noteCountLabel = new javax.swing.JLabel();
        settingsPanel = new javax.swing.JPanel();
        settingsLabel = new javax.swing.JLabel();
        refreshPanel = new javax.swing.JPanel();
        refreshLabel = new javax.swing.JLabel();
        mainCard = new javax.swing.JPanel();
        mainCard_note = new javax.swing.JPanel();
        searchIconPanel = new javax.swing.JPanel();
        searchIconLabel = new javax.swing.JLabel();
        mainNavigationPanel = new javax.swing.JPanel();
        labelPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        searchTextField = new javax.swing.JTextField();
        searchClosePanel = new javax.swing.JPanel();
        searchCloseLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtable = new javax.swing.JTable();
        mainCard_settings = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        settingUserUsername = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        settingUpdateUsername = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        settingUpdateUpdate = new javax.swing.JButton();
        settingUpdatePassword = new javax.swing.JPasswordField();
        settingUpdateRePassword = new javax.swing.JPasswordField();
        jLabel8 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        settingDeleteDelete = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        mainCard_backupDetails = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        backupLocationText = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        mainCard_importDetails = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        pinText = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        locationText = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        topNavBar.setBackground(new java.awt.Color(44, 51, 51));
        topNavBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                topNavBarMouseDragged(evt);
            }
        });
        topNavBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                topNavBarMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                topNavBarMousePressed(evt);
            }
        });

        plusPanel.setBackground(new java.awt.Color(44, 51, 51));
        plusPanel.setForeground(new java.awt.Color(51, 51, 51));

        plusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        plusLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/plus_20px.png"))); // NOI18N
        plusLabel.setToolTipText("Add a New Note");
        plusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                plusLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                plusLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                plusLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout plusPanelLayout = new javax.swing.GroupLayout(plusPanel);
        plusPanel.setLayout(plusPanelLayout);
        plusPanelLayout.setHorizontalGroup(
            plusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(plusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );
        plusPanelLayout.setVerticalGroup(
            plusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(plusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        closePanel.setBackground(new java.awt.Color(44, 51, 51));

        closeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        closeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/Close_20px.png"))); // NOI18N
        closeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout closePanelLayout = new javax.swing.GroupLayout(closePanel);
        closePanel.setLayout(closePanelLayout);
        closePanelLayout.setHorizontalGroup(
            closePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(closeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );
        closePanelLayout.setVerticalGroup(
            closePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(closeLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout topNavBarLayout = new javax.swing.GroupLayout(topNavBar);
        topNavBar.setLayout(topNavBarLayout);
        topNavBarLayout.setHorizontalGroup(
            topNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topNavBarLayout.createSequentialGroup()
                .addComponent(plusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        topNavBarLayout.setVerticalGroup(
            topNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topNavBarLayout.createSequentialGroup()
                .addGroup(topNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        bottomNavBar.setBackground(new java.awt.Color(51, 51, 51));

        jLabel4.setText("Note Count |");

        settingsPanel.setBackground(new java.awt.Color(51, 51, 51));

        settingsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        settingsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/icons8_settings_15px_2.png"))); // NOI18N
        settingsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingsLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settingsLabelMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        refreshPanel.setBackground(new java.awt.Color(51, 51, 51));

        refreshLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        refreshLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/Synchronize_15px.png"))); // NOI18N
        refreshLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                refreshLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshLabelMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                refreshLabelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                refreshLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout refreshPanelLayout = new javax.swing.GroupLayout(refreshPanel);
        refreshPanel.setLayout(refreshPanelLayout);
        refreshPanelLayout.setHorizontalGroup(
            refreshPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(refreshLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );
        refreshPanelLayout.setVerticalGroup(
            refreshPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(refreshLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout bottomNavBarLayout = new javax.swing.GroupLayout(bottomNavBar);
        bottomNavBar.setLayout(bottomNavBarLayout);
        bottomNavBarLayout.setHorizontalGroup(
            bottomNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomNavBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(refreshPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        bottomNavBarLayout.setVerticalGroup(
            bottomNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCountLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomNavBarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(bottomNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        mainCard.setLayout(new java.awt.CardLayout());

        mainCard_note.setPreferredSize(new java.awt.Dimension(300, 300));

        searchIconPanel.setBackground(new java.awt.Color(51, 51, 51));
        searchIconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchIconPanelMouseClicked(evt);
            }
        });

        searchIconLabel.setBackground(new java.awt.Color(51, 51, 51));
        searchIconLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        searchIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/Search_15px.png"))); // NOI18N
        searchIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchIconLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchIconLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchIconLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout searchIconPanelLayout = new javax.swing.GroupLayout(searchIconPanel);
        searchIconPanel.setLayout(searchIconPanelLayout);
        searchIconPanelLayout.setHorizontalGroup(
            searchIconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchIconLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );
        searchIconPanelLayout.setVerticalGroup(
            searchIconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchIconLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        mainNavigationPanel.setBackground(new java.awt.Color(204, 0, 102));
        mainNavigationPanel.setLayout(new java.awt.CardLayout());

        labelPanel.setBackground(new java.awt.Color(51, 51, 51));

        jLabel2.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Sticky Note++");
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));

        javax.swing.GroupLayout labelPanelLayout = new javax.swing.GroupLayout(labelPanel);
        labelPanel.setLayout(labelPanelLayout);
        labelPanelLayout.setHorizontalGroup(
            labelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(labelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                .addContainerGap())
        );
        labelPanelLayout.setVerticalGroup(
            labelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        mainNavigationPanel.add(labelPanel, "labelPanel");

        searchPanel.setBackground(new java.awt.Color(51, 51, 51));

        searchTextField.setBackground(new java.awt.Color(51, 51, 51));
        searchTextField.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        searchTextField.setText("Search...");
        searchTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        searchTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchTextFieldFocusLost(evt);
            }
        });
        searchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                searchTextFieldKeyPressed(evt);
            }
        });

        searchClosePanel.setBackground(new java.awt.Color(51, 51, 51));

        searchCloseLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        searchCloseLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/Clear_15px.png"))); // NOI18N
        searchCloseLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchCloseLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchCloseLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchCloseLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout searchClosePanelLayout = new javax.swing.GroupLayout(searchClosePanel);
        searchClosePanel.setLayout(searchClosePanelLayout);
        searchClosePanelLayout.setHorizontalGroup(
            searchClosePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchCloseLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );
        searchClosePanelLayout.setVerticalGroup(
            searchClosePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchClosePanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(searchCloseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(searchClosePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchTextField)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(searchClosePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        mainNavigationPanel.add(searchPanel, "searchPanel");

        jtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jtable.setRowHeight(140);
        jScrollPane2.setViewportView(jtable);
        if (jtable.getColumnModel().getColumnCount() > 0) {
            jtable.getColumnModel().getColumn(0).setResizable(false);
            jtable.getColumnModel().getColumn(1).setResizable(false);
            jtable.getColumnModel().getColumn(2).setResizable(false);
        }

        javax.swing.GroupLayout mainCard_noteLayout = new javax.swing.GroupLayout(mainCard_note);
        mainCard_note.setLayout(mainCard_noteLayout);
        mainCard_noteLayout.setHorizontalGroup(
            mainCard_noteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCard_noteLayout.createSequentialGroup()
                .addComponent(mainNavigationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(searchIconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        mainCard_noteLayout.setVerticalGroup(
            mainCard_noteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCard_noteLayout.createSequentialGroup()
                .addGroup(mainCard_noteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainNavigationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchIconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        mainCard.add(mainCard_note, "mainCard_note");

        mainCard_settings.setBackground(new java.awt.Color(60, 63, 65));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 204, 204));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Settings");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));

        jPanel1.setBackground(new java.awt.Color(60, 63, 65));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));

        jLabel3.setForeground(new java.awt.Color(204, 204, 204));
        jLabel3.setText("Username");

        settingUserUsername.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(settingUserUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(settingUserUsername))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(60, 63, 65));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Update", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        settingUpdateUsername.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                settingUpdateUsernameKeyPressed(evt);
            }
        });

        jLabel5.setForeground(new java.awt.Color(204, 204, 204));
        jLabel5.setText("Username");

        jLabel6.setForeground(new java.awt.Color(204, 204, 204));
        jLabel6.setText("Password");

        jLabel7.setForeground(new java.awt.Color(204, 204, 204));
        jLabel7.setText("Re-Password");

        settingUpdateUpdate.setBackground(new java.awt.Color(0, 102, 255));
        settingUpdateUpdate.setForeground(new java.awt.Color(255, 255, 255));
        settingUpdateUpdate.setText("Update");
        settingUpdateUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settingUpdateUpdateMouseEntered(evt);
            }
        });
        settingUpdateUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingUpdateUpdateActionPerformed(evt);
            }
        });

        settingUpdatePassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                settingUpdatePasswordKeyPressed(evt);
            }
        });

        settingUpdateRePassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                settingUpdateRePasswordKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(settingUpdateRePassword, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(settingUpdatePassword, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                    .addComponent(settingUpdateUsername)))))
                    .addComponent(settingUpdateUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settingUpdateUsername)
                    .addComponent(jLabel5))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingUpdatePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(settingUpdateRePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(settingUpdateUpdate)
                .addContainerGap())
        );

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jPanel3.setBackground(new java.awt.Color(60, 63, 65));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Delete", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jLabel9.setForeground(new java.awt.Color(204, 204, 204));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Delete the user account with all notes");

        settingDeleteDelete.setBackground(new java.awt.Color(255, 0, 0));
        settingDeleteDelete.setForeground(new java.awt.Color(255, 255, 255));
        settingDeleteDelete.setText("Delete");
        settingDeleteDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settingDeleteDeleteMouseEntered(evt);
            }
        });
        settingDeleteDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingDeleteDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingDeleteDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(settingDeleteDelete)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(60, 63, 65));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Backup", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jPanel5.setBackground(new java.awt.Color(60, 63, 65));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jLabel10.setForeground(new java.awt.Color(204, 204, 204));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Backup the database");

        jButton1.setBackground(new java.awt.Color(51, 204, 0));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Backup");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton1MouseEntered(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(60, 63, 65));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jLabel11.setForeground(new java.awt.Color(204, 204, 204));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Import backup database");

        jButton2.setBackground(new java.awt.Color(255, 102, 0));
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Import");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton2MouseEntered(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/icons8_chevron_left_20px.png"))); // NOI18N
        jLabel15.setText("Home");
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel15MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel15MouseEntered(evt);
            }
        });

        javax.swing.GroupLayout mainCard_settingsLayout = new javax.swing.GroupLayout(mainCard_settings);
        mainCard_settings.setLayout(mainCard_settingsLayout);
        mainCard_settingsLayout.setHorizontalGroup(
            mainCard_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainCard_settingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainCard_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainCard_settingsLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addContainerGap())
        );
        mainCard_settingsLayout.setVerticalGroup(
            mainCard_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCard_settingsLayout.createSequentialGroup()
                .addGroup(mainCard_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainCard_settingsLayout.createSequentialGroup()
                        .addGap(356, 356, 356)
                        .addComponent(jLabel8))
                    .addGroup(mainCard_settingsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(mainCard_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainCard_settingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(jLabel16))
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainCard.add(mainCard_settings, "mainCard_settings");

        mainCard_backupDetails.setBackground(new java.awt.Color(60, 63, 65));

        jPanel7.setBackground(new java.awt.Color(60, 63, 65));
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Backup Database Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jPanel8.setBackground(new java.awt.Color(60, 63, 65));
        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Backup Location", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(204, 204, 204))); // NOI18N

        backupLocationText.setBackground(new java.awt.Color(60, 63, 65));
        backupLocationText.setText("Enter the Backup Location...");
        backupLocationText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                backupLocationTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                backupLocationTextFocusLost(evt);
            }
        });
        backupLocationText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                backupLocationTextKeyPressed(evt);
            }
        });

        jLabel12.setForeground(new java.awt.Color(204, 204, 204));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("or");

        jButton3.setBackground(new java.awt.Color(204, 0, 204));
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Choose the Location");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton3MouseEntered(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(backupLocationText)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(backupLocationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton4.setBackground(new java.awt.Color(51, 204, 0));
        jButton4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Backup");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton4MouseEntered(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/icons8_chevron_left_20px.png"))); // NOI18N
        jLabel13.setText("Settings");
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel13MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel13MouseEntered(evt);
            }
        });

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/121212.png"))); // NOI18N

        javax.swing.GroupLayout mainCard_backupDetailsLayout = new javax.swing.GroupLayout(mainCard_backupDetails);
        mainCard_backupDetails.setLayout(mainCard_backupDetailsLayout);
        mainCard_backupDetailsLayout.setHorizontalGroup(
            mainCard_backupDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCard_backupDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainCard_backupDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainCard_backupDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainCard_backupDetailsLayout.setVerticalGroup(
            mainCard_backupDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCard_backupDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
        );

        mainCard.add(mainCard_backupDetails, "mainCard_backupDetails");

        mainCard_importDetails.setBackground(new java.awt.Color(60, 63, 65));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/icons8_chevron_left_20px.png"))); // NOI18N
        jLabel17.setText("Settings");
        jLabel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel17MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel17MouseEntered(evt);
            }
        });

        jPanel9.setBackground(new java.awt.Color(60, 63, 65));
        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Import Database Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jLabel18.setForeground(new java.awt.Color(242, 242, 242));
        jLabel18.setText("PIN");

        pinText.setBackground(new java.awt.Color(60, 63, 65));
        pinText.setText("Enter the Backup PIN...");
        pinText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pinTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pinTextFocusLost(evt);
            }
        });
        pinText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pinTextKeyPressed(evt);
            }
        });

        jPanel10.setBackground(new java.awt.Color(60, 63, 65));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Import Location", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(204, 204, 204))); // NOI18N
        jPanel10.setForeground(new java.awt.Color(204, 204, 204));

        locationText.setBackground(new java.awt.Color(60, 63, 65));
        locationText.setText("Enter the Import Location...");
        locationText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                locationTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                locationTextFocusLost(evt);
            }
        });
        locationText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                locationTextKeyPressed(evt);
            }
        });

        jLabel19.setForeground(new java.awt.Color(204, 204, 204));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("or");

        jButton5.setBackground(new java.awt.Color(204, 0, 204));
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("Choose the Backup File");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton5MouseEntered(evt);
            }
        });
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(locationText)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(locationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton6.setBackground(new java.awt.Color(255, 102, 0));
        jButton6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Import");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton6MouseEntered(evt);
            }
        });
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pinText))
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pinText)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/131313.png"))); // NOI18N

        javax.swing.GroupLayout mainCard_importDetailsLayout = new javax.swing.GroupLayout(mainCard_importDetails);
        mainCard_importDetails.setLayout(mainCard_importDetailsLayout);
        mainCard_importDetailsLayout.setHorizontalGroup(
            mainCard_importDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCard_importDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainCard_importDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainCard_importDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainCard_importDetailsLayout.setVerticalGroup(
            mainCard_importDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainCard_importDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE))
        );

        mainCard.add(mainCard_importDetails, "mainCard_importDetails");

        javax.swing.GroupLayout shadowPanelLayout = new javax.swing.GroupLayout(shadowPanel);
        shadowPanel.setLayout(shadowPanelLayout);
        shadowPanelLayout.setHorizontalGroup(
            shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bottomNavBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(topNavBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainCard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        shadowPanelLayout.setVerticalGroup(
            shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topNavBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mainCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(bottomNavBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(shadowPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(shadowPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * This method will set the current x & y coordinates to the variable xx &
     * yy when the mouse press on the top navbar
     *
     * @param evt Mouse press event
     */
    private void topNavBarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topNavBarMousePressed
        xx = evt.getX();
        yy = evt.getY();
    }//GEN-LAST:event_topNavBarMousePressed

    /**
     * This method will maximize & minimize the application when the mouse
     * dragged
     *
     * @param evt Mouse drag event
     */
    private void topNavBarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topNavBarMouseDragged
        if ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            this.setExtendedState(JFrame.NORMAL);
        } else {
            this.setLocation(evt.getXOnScreen() - xx - 6, evt.getYOnScreen() - yy - 6);
        }

    }//GEN-LAST:event_topNavBarMouseDragged

    private void topNavBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topNavBarMouseClicked
    }//GEN-LAST:event_topNavBarMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    }//GEN-LAST:event_formWindowClosing

    /**
     * This method will make the search icon entry cursor to be HAND_CURSOR and
     * change the background colour
     *
     * @param evt Mouse entry event
     */
    private void searchIconLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchIconLabelMouseEntered
        searchIconPanel.setBackground(new Color(82, 82, 82));
        searchIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchIconLabelMouseEntered

    /**
     * This method will change the search icon background colour
     *
     * @param evt Mouse exit event
     */
    private void searchIconLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchIconLabelMouseExited
        searchIconPanel.setBackground(new Color(51, 51, 51));
    }//GEN-LAST:event_searchIconLabelMouseExited

    /**
     * This method will make the close icon entry cursor to be HAND_CURSOR and
     * change the background colour
     *
     * @param evt Mouse entry event
     */
    private void closeLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeLabelMouseEntered
        closePanel.setBackground(new Color(255, 0, 0));
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_closeLabelMouseEntered

    /**
     * This method will make the plus icon entry cursor to be HAND_CURSOR and
     * change the background colour & change the icon
     *
     * @param evt Mouse entry event
     */
    private void plusLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plusLabelMouseEntered
        plusPanel.setBackground(new Color(255, 255, 0));
        plusLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(cons.getPLUSMATHBLACK()))));
        plusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_plusLabelMouseEntered

    /**
     * This method will change the plus icon background colour & change the icon
     *
     * @param evt Mouse exit event
     */
    private void plusLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plusLabelMouseExited
        plusPanel.setBackground(new Color(51, 51, 51));
        plusLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(cons.getPLUSMATH()))));
    }//GEN-LAST:event_plusLabelMouseExited

    private void searchIconLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchIconLabelMouseClicked
        if (currentCardPanel != "searchPanel") {
            cardMainNavigationPanel.show(mainNavigationPanel, "searchPanel");
            currentCardPanel = "searchPanel";
        } else {
            model.setRowCount(0);
            setSavedNotesInTableNotesByKeyword(searchTextField.getText());
        }
    }//GEN-LAST:event_searchIconLabelMouseClicked

    private void searchIconPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchIconPanelMouseClicked
    }//GEN-LAST:event_searchIconPanelMouseClicked

    private void closeLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeLabelMouseExited
        closePanel.setBackground(new Color(44, 51, 51));
    }//GEN-LAST:event_closeLabelMouseExited

    private void closeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeLabelMouseClicked
        if (SystemTray.isSupported()) {
            this.hide();
            systemTray();
        } else {
            System.exit(0);
        }
    }//GEN-LAST:event_closeLabelMouseClicked

    private void searchCloseLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchCloseLabelMouseExited
        searchClosePanel.setBackground(new Color(51, 51, 51));
    }//GEN-LAST:event_searchCloseLabelMouseExited

    private void searchCloseLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchCloseLabelMouseEntered
        searchClosePanel.setBackground(new Color(82, 82, 82));
        searchCloseLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchCloseLabelMouseEntered

    private void searchCloseLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchCloseLabelMouseClicked
        searchTextField.setText("Search...");
        if (currentCardPanel != "labelPanel") {
            CardLayout card = (CardLayout) mainNavigationPanel.getLayout();
            card.show(mainNavigationPanel, "labelPanel");
            currentCardPanel = "labelPanel";
        }
        model.setRowCount(0);
        setSavedNotesInTable();
    }//GEN-LAST:event_searchCloseLabelMouseClicked

    private void searchTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchTextFieldFocusLost
        if (searchTextField.getText().isEmpty()) {
            searchTextField.setText("Search...");
        }
    }//GEN-LAST:event_searchTextFieldFocusLost

    private void searchTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchTextFieldFocusGained
        if (searchTextField.getText().equals("Search...")) {
            searchTextField.setText("");
        }
    }//GEN-LAST:event_searchTextFieldFocusGained

    public void countNotes() {
        noteCountLabel.setText(String.valueOf(new Database().countUserNotes(this.userId)));
    }

    private void plusLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_plusLabelMouseClicked
        noteID = new Database().saveNote(userId, null, null, null);
        model.addRow(new Object[]{null, null, noteID});
        new NoteAppWindow(noteID, jtable.getRowCount() - 1);
        this.noteCountLabel.setText(Integer.toString(new Database().countUserNotes(userId)));

    }//GEN-LAST:event_plusLabelMouseClicked

    private void settingsLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsLabelMouseClicked
        labelBackToNormal();
        resetTexts();
        if (isSettingsClicked == false) {
            isSettingsClicked = true;
            settingsPanel.setBackground(new Color(204, 204, 204));
            settingsLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blackcode/img/home_15px.png"))));
            cardMainCard.show(mainCard, "mainCard_settings");
        } else {
            isSettingsClicked = false;
            settingsPanel.setBackground(new Color(51, 51, 51));
            settingsLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blackcode/img/icons8_settings_15px_2.png"))));
            cardMainCard.show(mainCard, "mainCard_note");
        }
    }//GEN-LAST:event_settingsLabelMouseClicked

    private void settingsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsLabelMouseEntered
        settingsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_settingsLabelMouseEntered

    private void settingUpdateUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingUpdateUpdateActionPerformed
        String userName = settingUpdateUsername.getText();
        String password = settingUpdatePassword.getText();
        String rePassword = settingUpdateRePassword.getText();

        if (!password.equals(rePassword) || password.equals("")) {
            new MessageWindow("Warning", "Password & Repeat-Password entries do not match or Empty");
            this.settingUpdatePassword.setText("");
            this.settingUpdateRePassword.setText("");
            this.settingUpdatePassword.requestFocus();
        } else {
            if (userName.equals("")) {
                new MessageWindow("Warning", "Username entrie is Empty");
            } else {
                boolean updateStatus;
                if (this.username.equals(userName)) {
                    updateStatus = new Database().updateUser(userId, password);
                } else {
                    updateStatus = new Database().updateUser(userId, userName, password);
                }

                if (updateStatus) {
                    new MessageWindow("Information", "User updated successfully");
                    this.username = userName;
                    settingUserUsername.setText(this.username);
                    settingUpdateUsername.setText(this.username);
                    settingUpdatePassword.setText("");
                    settingUpdateRePassword.setText("");
                } else {
                    this.settingUpdateUsername.setText("");
                    this.settingUpdateUsername.requestFocus();
                }
            }
        }
    }//GEN-LAST:event_settingUpdateUpdateActionPerformed

    private void refreshLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshLabelMouseEntered
        refreshLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_refreshLabelMouseEntered

    private void settingDeleteDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingDeleteDeleteActionPerformed
        int userChoice = JOptionPane.showConfirmDialog(null, "Are you sure you want to proceed?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (userChoice == JOptionPane.YES_OPTION) {
            if (new Database().deleteUser(this.userId)) {
                new MessageWindow("Information", "User deleted successfully");
                this.userId = 0;
                this.username = null;
                this.encryptionKey = null;
                this.dispose();
                new UserLoginWindow();
                if (isAppIntheSystemTray) {
                    isAppIntheSystemTray = false;
                    systemTray.remove(trayIcon);
                }
            } else {
                new MessageWindow("Error", "User does not deleted");
            }
        }
    }//GEN-LAST:event_settingDeleteDeleteActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cardMainCard.show(mainCard, "mainCard_backupDetails");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        cardMainCard.show(mainCard, "mainCard_importDetails");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jLabel13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MouseClicked
        cardMainCard.show(mainCard, "mainCard_settings");
        resetTexts();
    }//GEN-LAST:event_jLabel13MouseClicked

    private void jLabel13MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MouseEntered
        jLabel13.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel13MouseEntered

    private void jLabel15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseClicked
        labelBackToNormal();
        isSettingsClicked = false;
        settingsPanel.setBackground(new Color(51, 51, 51));
        settingsLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blackcode/img/icons8_settings_15px_2.png"))));
        cardMainCard.show(mainCard, "mainCard_note");
    }//GEN-LAST:event_jLabel15MouseClicked

    private void jLabel15MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseEntered
        jLabel15.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel15MouseEntered

    private void jLabel17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseClicked
        cardMainCard.show(mainCard, "mainCard_settings");
        resetTexts();
    }//GEN-LAST:event_jLabel17MouseClicked

    /**
     * This method will reset the texts in the label for the PIN & Import
     * location & Backup location
     */
    private void resetTexts() {
        pinText.setText("Enter the Backup PIN...");
        locationText.setText("Enter the Import Location...");
        backupLocationText.setText("Enter the Backup Location...");
    }

    private void jLabel17MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel17MouseEntered
        jLabel17.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel17MouseEntered

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        JFileChooser fileChooser = new JFileChooser();

        // Set the file chooser to select files only (not directories)
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Create a file extension filter for ".sn" files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SN Files (*.sn)", "sn");
        fileChooser.setFileFilter(filter);

        // Show the file chooser dialog and capture the result
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            java.io.File selectedFile = fileChooser.getSelectedFile();
            // Get the file location as a string
            String filePath = selectedFile.getAbsolutePath();
            locationText.setText(filePath);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JFileChooser fileChooser = new JFileChooser();

        // Set the file chooser to select directories only
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Show the file chooser dialog and capture the result
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            // Get the selected folder
            java.io.File selectedFolder = fileChooser.getSelectedFile();

            // Get the folder path as a string
            String folderPath = selectedFolder.getAbsolutePath();

            // Set the folder path to a text field or display it as needed
            backupLocationText.setText(folderPath);
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseEntered
        jButton5.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton5MouseEntered

    private void jButton6MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseEntered
        jButton6.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton6MouseEntered

    private void jButton3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseEntered
        jButton3.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton3MouseEntered

    private void jButton4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseEntered
        jButton4.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton4MouseEntered

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (backupLocationText.getText().equals("") || backupLocationText.getText().equals("Enter the Backup Location...") || backupLocationText.getText().equals(null)) {
            new MessageWindow("Error", "Enter a Backup Location");
            backupLocationText.requestFocus();
        } else {
            String result[] = new Database().encryptBackupFile(backupLocationText.getText());
            if (result[0].equals("true")) {
                new BackupDetails(result[2], result[1], result[3]);
            } else {
                new MessageWindow("Error", "Backup error");
            }

        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void backupLocationTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_backupLocationTextFocusGained
        if (backupLocationText.getText().equals("Enter the Backup Location...")) {
            backupLocationText.setText("");
        }
    }//GEN-LAST:event_backupLocationTextFocusGained

    private void backupLocationTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_backupLocationTextFocusLost
        if (backupLocationText.getText().isEmpty()) {
            backupLocationText.setText("Enter the Backup Location...");
        }
    }//GEN-LAST:event_backupLocationTextFocusLost

    private void pinTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pinTextFocusGained
        if (pinText.getText().equals("Enter the Backup PIN...")) {
            pinText.setText("");
        }
    }//GEN-LAST:event_pinTextFocusGained

    private void pinTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pinTextFocusLost
        if (pinText.getText().isEmpty()) {
            pinText.setText("Enter the Backup PIN...");
        }
    }//GEN-LAST:event_pinTextFocusLost

    private void locationTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_locationTextFocusGained
        if (locationText.getText().equals("Enter the Import Location...")) {
            locationText.setText("");
        }
    }//GEN-LAST:event_locationTextFocusGained

    private void locationTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_locationTextFocusLost
        if (locationText.getText().isEmpty()) {
            locationText.setText("Enter the Import Location...");
        }
    }//GEN-LAST:event_locationTextFocusLost

    private void settingUpdateUpdateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingUpdateUpdateMouseEntered
        settingUpdateUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_settingUpdateUpdateMouseEntered

    private void settingDeleteDeleteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingDeleteDeleteMouseEntered
        settingDeleteDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_settingDeleteDeleteMouseEntered

    private void jButton1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseEntered
        jButton1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton1MouseEntered

    private void jButton2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseEntered
        jButton2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton2MouseEntered

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (pinText.getText().equals("") || pinText.getText().equals(null) || pinText.getText().equals("Enter the Backup PIN...")) {
            if (locationText.getText().equals("") || locationText.getText().equals(null) || locationText.getText().equals("Enter the Import Location...")) {
                new MessageWindow("Error", "Enter the Backup PIN & Backup file Location");
                pinText.requestFocus();
            }
        } else {
            if (locationText.getText().equals("") || locationText.getText().equals(null) || locationText.getText().equals("Enter the Import Location...")) {
                new MessageWindow("Error", "Enter the Backup file Location");
                locationText.requestFocus();
            } else {
                if (new Database().decryptBackupFile(locationText.getText(), pinText.getText())) {
                    new MessageWindow("Information", "Backup import successfully");
                } else {
                    new MessageWindow("Error", "Backup import error");
                }
            }
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void settingUpdateUsernameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_settingUpdateUsernameKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            settingUpdatePassword.requestFocus();
        }
    }//GEN-LAST:event_settingUpdateUsernameKeyPressed

    private void settingUpdatePasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_settingUpdatePasswordKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            settingUpdateRePassword.requestFocus();
        }
    }//GEN-LAST:event_settingUpdatePasswordKeyPressed

    private void settingUpdateRePasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_settingUpdateRePasswordKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            settingUpdateUpdate.requestFocus();
        }
    }//GEN-LAST:event_settingUpdateRePasswordKeyPressed

    private void backupLocationTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_backupLocationTextKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            jButton4.requestFocus();
        }
    }//GEN-LAST:event_backupLocationTextKeyPressed

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3MouseClicked

    private void pinTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pinTextKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            locationText.requestFocus();
        }
    }//GEN-LAST:event_pinTextKeyPressed

    private void locationTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_locationTextKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            jButton6.requestFocus();
        }
    }//GEN-LAST:event_locationTextKeyPressed

    private void refreshLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshLabelMousePressed
        refreshPanel.setBackground(new Color(204, 204, 204));
        refreshLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blackcode/img/icons8_available_updates_15px.png"))));
    }//GEN-LAST:event_refreshLabelMousePressed

    private void refreshLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshLabelMouseReleased
        refreshPanel.setBackground(new Color(51, 51, 51));
        refreshLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blackcode/img/Synchronize_15px.png"))));
    }//GEN-LAST:event_refreshLabelMouseReleased

    private void refreshLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshLabelMouseClicked
        List<Pair<Integer, String>> noteAndPlainTextList = new Database().getAllPlainTextNotes(userId);
        model.setRowCount(0);
        // Add new rows
        for (Pair<Integer, String> pair : noteAndPlainTextList) {
            model.addRow(new Object[]{pair.getRight(), null, pair.getLeft()});
        }

        this.noteCountLabel.setText(Integer.toString(new Database().countUserNotes(userId)));
    }//GEN-LAST:event_refreshLabelMouseClicked

    private void searchTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchTextFieldKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            model.setRowCount(0);
            setSavedNotesInTableNotesByKeyword(searchTextField.getText());
        }
    }//GEN-LAST:event_searchTextFieldKeyPressed

    private void labelBackToNormal() {
        settingsPanel.setBackground(new Color(51, 51, 51));
        settingsLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blackcode/img/icons8_settings_15px_2.png"))));
    }

    private void setWindowSize() {
        // Get default toolkit
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // Get the screen size
        Dimension screenSize = toolkit.getScreenSize();
        if (screenSize.width == 1920 && screenSize.height == 1080) {
            this.setSize(359, 642);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField backupLocationText;
    private javax.swing.JPanel bottomNavBar;
    private javax.swing.JLabel closeLabel;
    private javax.swing.JPanel closePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    public static javax.swing.JTable jtable;
    private javax.swing.JPanel labelPanel;
    private javax.swing.JTextField locationText;
    private javax.swing.JPanel mainCard;
    private javax.swing.JPanel mainCard_backupDetails;
    private javax.swing.JPanel mainCard_importDetails;
    private javax.swing.JPanel mainCard_note;
    private javax.swing.JPanel mainCard_settings;
    private javax.swing.JPanel mainNavigationPanel;
    public javax.swing.JLabel noteCountLabel;
    private javax.swing.JTextField pinText;
    private javax.swing.JLabel plusLabel;
    private javax.swing.JPanel plusPanel;
    private javax.swing.JLabel refreshLabel;
    private javax.swing.JPanel refreshPanel;
    private javax.swing.JLabel searchCloseLabel;
    private javax.swing.JPanel searchClosePanel;
    private javax.swing.JLabel searchIconLabel;
    private javax.swing.JPanel searchIconPanel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton settingDeleteDelete;
    private javax.swing.JPasswordField settingUpdatePassword;
    private javax.swing.JPasswordField settingUpdateRePassword;
    private javax.swing.JButton settingUpdateUpdate;
    private javax.swing.JTextField settingUpdateUsername;
    private javax.swing.JTextField settingUserUsername;
    private javax.swing.JLabel settingsLabel;
    private javax.swing.JPanel settingsPanel;
    private blackcode.uiclass.PanelShadow shadowPanel;
    private javax.swing.JPanel topNavBar;
    // End of variables declaration//GEN-END:variables
}
