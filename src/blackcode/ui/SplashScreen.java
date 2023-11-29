/* DON-CODE */
package blackcode.ui;

import static blackcode.BlackCode.cons;
import blackcode.uiclass.Database;
import blackcode.uiclass.MessageWindow;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;

public class SplashScreen extends javax.swing.JFrame {

    public SplashScreen() {
        initComponents();
        this.setLocationRelativeTo(null); // Set location to center
        FlatDarkLaf.setup(); // Set FlatLf Dark theme for that application
        this.show();
        setBackground(new Color(0, 0, 0, 0)); // For transparent background
        progressBar.setBackground(new Color(0, 0, 0, 0)); // For transparent background
        getRootPane().setOpaque(false);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(cons.getUNLOCK_IMG()))); // Set application icon
        constructorFunction(); // Main functions
        progressBar(); // Progress bar
    }

    private void progressBar() {
        for (int i = 0; i <= 100; i++) {
            progressBar.setValue(i);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                new MessageWindow("Error", "Application error");
            }
        }
    }

    // Check the device OS
    private boolean checkOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            new MessageWindow("Error", "This is a 'Windows' based application");
            System.exit(0);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sticky Notes++");
        setAlwaysOnTop(true);
        setUndecorated(true);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/blackcode/img/stickyNote++_shadow.png"))); // NOI18N

        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Sticky Note++");

        progressBar.setBackground(new java.awt.Color(102, 204, 0));
        progressBar.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        progressBar.setForeground(new java.awt.Color(255, 102, 0));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void constructorFunction() {
        FlatDarkLaf.setup(); // Set FlatLf Dark theme for the application
        // Check OS & create a folder if not exists
        checkOS();
        if (cons.getFILE_PATH() != null) {
            if (!new File(cons.getDATABASE_PATH()).exists()) {
                if (new Database().dbConnect().equals(null)) {
                    new MessageWindow("Error", "Database connection error");
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
}
