package blackcode.uiclass;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class WordWrapCellRenderer implements TableCellRenderer {

    private DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
    
    public WordWrapCellRenderer() {
        defaultRenderer.setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true); // Enable word wrap
        textArea.setWrapStyleWord(true); // Wrap at word boundaries

        if (value != null) {
            textArea.setText(value.toString());
        }

        // Optionally, you can set a specific font for word wrapping
        Font font = new Font("Sans-serif", Font.PLAIN, 12);
        textArea.setFont(font);
        
        textArea.setBorder(BorderFactory.createLineBorder(new Color(102, 102, 102))); // White border

        if (isSelected) {
            textArea.setBackground(table.getSelectionBackground());
            textArea.setForeground(table.getSelectionForeground());
        } else {
            textArea.setBackground(table.getBackground());
            textArea.setForeground(table.getForeground());
        }
        
        return textArea;
    }
}
