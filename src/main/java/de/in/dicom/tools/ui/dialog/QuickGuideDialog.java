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
        super(parent, "Kurzanleitung", true);
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
                + "<h2>Schnellstart</h2>"
                + "<p>Willkommen im DicomEditor. Hier sind die wichtigsten Funktionen im Überblick:</p>"

                + "<h3>📂 Dateien öffnen</h3>"
                + "<ul>"
                + "<li>Nutzen Sie <b>Datei > Öffnen</b> im Menü.</li>"
                + "<li>Oder ziehen Sie eine DICOM-Datei einfach per <b>Drag & Drop</b> in das Fenster.</li>"
                + "</ul>"

                + "<h3>✏️ Attribute bearbeiten</h3>"
                + "<ul>"
                + "<li><b>Doppelklick</b> auf einen Wert in der Tabelle, um ihn zu bearbeiten.</li>"
                + "<li><b>Rechtsklick</b> auf die Tabelle öffnet das Kontextmenü (Tag hinzufügen/löschen).</li>"
                + "</ul>"

                + "<h3>💾 Speichern</h3>"
                + "<ul>"
                + "<li><b>Datei > Speichern</b> überschreibt die aktuelle Datei.</li>"
                + "<li><b>Datei > Speichern unter...</b> erstellt eine neue Datei.</li>"
                + "</ul>"

                + "<h3>🔤 Zeichensätze</h3>"
                + "<ul>"
                + "<li>Unter <b>Bearbeiten > Specific Character Set</b> können Sie das Encoding ändern (z.B. ISO_IR 100).</li>"
                + "</ul>"

                + "</body></html>";

        editorPane.setText(content);
        editorPane.setCaretPosition(0); // Scroll to top

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Close Button
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Schließen");
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
