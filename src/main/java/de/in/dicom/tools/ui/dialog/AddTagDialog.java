package de.in.dicom.tools.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder; // Added missing import
import org.dcm4che3.data.VR;

/**
 * Dialog for adding a new DICOM tag with a specific Value Representation (VR).
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class AddTagDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private JTextField tagField;
    private JComboBox<VR> vrComboBox;
    private boolean approved = false;

    public AddTagDialog() {
        setTitle("Add Tag");
        setModal(true);
        setBounds(100, 100, 300, 180);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JPanel panel = new JPanel();
        contentPanel.add(panel);
        panel.setLayout(new java.awt.GridLayout(2, 2, 5, 5));

        JLabel lblTag = new JLabel("Tag (Hex):");
        panel.add(lblTag);

        tagField = new JTextField();
        // tagField.setToolTipText("gggg,eeee");
        panel.add(tagField);
        tagField.setColumns(10);

        JLabel lblVr = new JLabel("VR:");
        panel.add(lblVr);

        vrComboBox = new JComboBox<>(VR.values());
        panel.add(vrComboBox);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(e -> {
            approved = true;
            setVisible(false);
        });
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(e -> {
            approved = false;
            setVisible(false);
        });
        buttonPane.add(cancelButton);
    }

    public int getTag() {
        try {
            String text = tagField.getText().trim();
            if (text.isEmpty())
                return -1;
            // Handle hex input with or without comma
            text = text.replace(",", "").replace("x", "");
            return (int) Long.parseLong(text, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public VR getVR() {
        return (VR) vrComboBox.getSelectedItem();
    }

    public boolean isApproved() {
        return approved;
    }
}
