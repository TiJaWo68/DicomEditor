package de.in.dicom.tools.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Provides detailed information for DICOM status/validation messages.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class StatusInfoProvider {

    public static void showStatusInfo(Component parent, String statusString, Point location) {
        if (statusString == null || statusString.isEmpty()) {
            return;
        }

        String title = "Information";
        String content = statusString;
        java.awt.Color headerColor = new java.awt.Color(0, 120, 215); // Default blue

        if (statusString.startsWith("VALIDATION_ALERT:")) {
            title = "Validation Alert";
            content = statusString.substring("VALIDATION_ALERT:".length());
            headerColor = java.awt.Color.RED;
        } else if (statusString.startsWith("UID_INFO:")) {
            title = "UID Information";
            content = statusString.substring("UID_INFO:".length());
            headerColor = new java.awt.Color(0, 100, 180); // Professional blue
        } else if (statusString.startsWith("CS_INFO:")) {
            title = "Code String Information";
            content = statusString.substring("CS_INFO:".length());
            headerColor = new java.awt.Color(0, 120, 150); // Another shade of blue
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title,
                JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new java.awt.BorderLayout());

        // Header
        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(headerLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        headerLabel.setForeground(headerColor);
        headerLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 5, 15));
        dialog.add(headerLabel, java.awt.BorderLayout.NORTH);

        // Content
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new JLabel().getBackground());
        textPane.setText(content);
        textPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 10, 15));

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        dialog.add(scrollPane, java.awt.BorderLayout.CENTER);

        // Footer with Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 5, 10));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        // ESC key binding
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        dialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.pack();
        dialog.setMinimumSize(new java.awt.Dimension(300, 150));
        dialog.setSize(Math.max(300, dialog.getWidth()), Math.min(400, dialog.getHeight()));

        if (location != null) {
            dialog.setLocation(location);
        } else {
            dialog.setLocationRelativeTo(parent);
        }

        dialog.setVisible(true);
    }
}
