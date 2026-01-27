package de.in.dicom.tools.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

/**
 * A dialog that displays a quick guide for the application.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class QuickGuideDialog extends JDialog {

    public QuickGuideDialog(JFrame parent) {
        super(parent, "Quick Guide", true);
        setLayout(new BorderLayout());
        setSize(600, 500);
        setLocationRelativeTo(parent);

        // HTML Content
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(new Font("SansSerif", Font.PLAIN, 14));

        String content = "<html><body style='font-family: sans-serif; padding: 10px;'>"
                + "<h2>Quick Start</h2>"
                + "<p>Welcome to DicomEditor. Here's an overview of the key features:</p>"

                + "<h3>📂 Opening Files</h3>"
                + "<ul>"
                + "<li>Use <b>File > Open</b> in the menu.</li>"
                + "<li>Or simply <b>Drag & Drop</b> a DICOM file into the window.</li>"
                + "</ul>"

                + "<h3>✏️ Editing Attributes</h3>"
                + "<ul>"
                + "<li><b>Double-click</b> a value in the table to edit it.</li>"
                + "<li><b>Right-click</b> the table to open the context menu (Add/Delete Tag).</li>"
                + "</ul>"

                + "<h3>💾 Saving</h3>"
                + "<ul>"
                + "<li><b>File > Save</b> overwrites the current file.</li>"
                + "<li><b>File > Save As...</b> creates a new file.</li>"
                + "</ul>"

                + "<h3>🔤 Character Sets</h3>"
                + "<ul>"
                + "<li>Under <b>Edit > Specific Character Set</b>, you can change the encoding (e.g., ISO_IR 100).</li>"
                + "</ul>"

                + "</body></html>";

        editorPane.setText(content);
        editorPane.setCaretPosition(0); // Scroll to top

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Close Button
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buttonPanel, BorderLayout.SOUTH);

        // Close on Escape
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
