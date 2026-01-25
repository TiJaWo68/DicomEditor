package de.in.dicom.tools.ui.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import de.in.dicom.tools.ui.helper.LibraryLoader;
import de.in.dicom.tools.ui.helper.LibraryLoader.LibraryInfo;
import de.in.utils.Version;

/**
 * Redesigned About Dialog for DicomEditor with sidebar and library credits.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class AboutDialog extends JDialog {

    private final JPanel contentCardPanel;
    private final CardLayout cardLayout;
    private final List<JButton> sidebarButtons = new ArrayList<>();

    public AboutDialog(JFrame parent) {
        super(parent, "Über DicomEditor", true);
        setLayout(new BorderLayout());
        setSize(800, 500);
        setLocationRelativeTo(parent);

        // Sidebar
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 60, 60)));
        sidebarPanel.setBackground(UIManager.getColor("Panel.background"));
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));

        sidebarPanel.add(Box.createVerticalStrut(10));

        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Add Sections
        addSection(sidebarPanel, "Allgemein", createGeneralPanel());
        addSection(sidebarPanel, "Bibliotheken", createLibrariesPanel());

        sidebarPanel.add(Box.createVerticalGlue());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, contentCardPanel);
        splitPane.setDividerSize(0);
        splitPane.setDividerLocation(200);
        splitPane.setEnabled(false);

        add(splitPane, BorderLayout.CENTER);

        // Close Button at bottom
        JButton closeButton = new JButton("Schließen");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Select first item by default
        if (!sidebarButtons.isEmpty()) {
            sidebarButtons.get(0).doClick();
        }

        // Close on Escape
        getRootPane().registerKeyboardAction(e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void addSection(JPanel sidebar, String title, JPanel content) {
        contentCardPanel.add(content, title);

        JButton btn = new JButton(title);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.putClientProperty("JButton.buttonType", "square");
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(14f));

        btn.addActionListener(e -> {
            cardLayout.show(contentCardPanel, title);
            updateSidebarSelection(btn);
        });

        sidebarButtons.add(btn);
        sidebar.add(btn);
    }

    private void updateSidebarSelection(JButton selected) {
        Color accentColor = UIManager.getColor("Component.accentColor");
        if (accentColor == null)
            accentColor = new Color(51, 153, 255);

        for (JButton btn : sidebarButtons) {
            if (btn == selected) {
                btn.setBackground(accentColor);
                btn.setForeground(Color.WHITE);
                btn.setFont(btn.getFont().deriveFont(Font.BOLD));
            } else {
                btn.setBackground(null);
                btn.setForeground(UIManager.getColor("Label.foreground"));
                btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
            }
        }
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Splash Image (scaled)
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/DicomEditorSplash.png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(400, -1, Image.SCALE_SMOOTH);
            JLabel splashLabel = new JLabel(new ImageIcon(scaledImage));
            splashLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalGlue());
            panel.add(splashLabel);
            panel.add(Box.createVerticalStrut(20));
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("[Splash-Image konnte nicht geladen werden]");
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(errorLabel);
        }

        JLabel titleLabel = new JLabel("DicomEditor");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        String version = Version.retrieveVersionFromPom("de.in", "dicomeditor");
        JLabel versionLabel = new JLabel("Version " + version);
        versionLabel.setFont(versionLabel.getFont().deriveFont(14f));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setForeground(Color.GRAY);
        panel.add(versionLabel);

        panel.add(Box.createVerticalStrut(20));
        JLabel copyrightLabel = new JLabel("© 2026 TiJaWo68");
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(copyrightLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createLibrariesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel header = new JLabel("Verwendete Bibliotheken");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        LibraryLoader loader = new LibraryLoader();
        List<LibraryInfo> libs = loader.loadLibraries();

        for (LibraryInfo lib : libs) {
            addLib(listPanel, lib.name(), lib.version(), lib.license(), lib.projectUrl(), lib.licenseUrl());
        }

        if (libs.isEmpty()) {
            listPanel.add(new JLabel("Keine Lizenzinformationen gefunden (licenses.xml fehlt)."));
            listPanel.add(new JLabel("Bitte führen Sie 'mvn generate-resources' aus."));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addLib(JPanel container, String name, String version, String license, String projectUrl,
            String licenseUrl) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        // Project Link (Name)
        JLabel nameLabel = new JLabel();
        if (projectUrl != null && !projectUrl.isEmpty()) {
            nameLabel.setText("<html><a href='" + projectUrl + "' style='color: #58a6ff; text-decoration: underline;'>"
                    + name + "</a> <span style='font-size: 10px; color: gray;'>v" + version + "</span></html>");
            nameLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            nameLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openUrl(projectUrl);
                }
            });
        } else {
            nameLabel.setText(
                    "<html><b>" + name + "</b> <span style='font-size: 10px; color: gray;'>v" + version
                            + "</span></html>");
        }
        nameLabel.setFont(nameLabel.getFont().deriveFont(14f));

        // License Link
        JLabel licenseLabel = new JLabel();
        if (licenseUrl != null && !licenseUrl.isEmpty()) {
            licenseLabel.setText("<html>Lizenz: <a href='" + licenseUrl
                    + "' style='color: #58a6ff; text-decoration: none;'>" + license + "</a></html>");
            licenseLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            licenseLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openUrl(licenseUrl);
                }
            });
        } else {
            licenseLabel.setText("Lizenz: " + license);
        }
        licenseLabel.setFont(licenseLabel.getFont().deriveFont(12f));
        licenseLabel.setForeground(Color.GRAY);

        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(licenseLabel);

        item.add(textPanel, BorderLayout.CENTER);

        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(item);
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
