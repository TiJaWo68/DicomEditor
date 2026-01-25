package de.in.dicom.tools.ui.actions;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.in.dicom.tools.DicomEditor;
import de.in.dicom.tools.ui.dialog.AboutDialog;
import de.in.dicom.tools.ui.dialog.QuickGuideDialog;
import de.in.updraft.GithubUpdater;
import de.in.updraft.UpdateChannel;
import de.in.updraft.UpdateInfo;
import de.in.updraft.source.GithubReleaseSource;
import de.in.utils.Version;

/**
 * Handles help-related actions for DicomEditor.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class HelpActions {

    private static final Logger LOGGER = LogManager.getLogger(HelpActions.class);
    private final JFrame parentFrame;
    private final GithubUpdater updater;
    private final String currentVersion;

    public HelpActions(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.currentVersion = Version.retrieveVersionFromPom("de.in", "dicomeditor");

        Path appJar = null;
        try {
            appJar = Paths.get(DicomEditor.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to determine application JAR path", e);
        }

        this.updater = new GithubUpdater(currentVersion,
                new GithubReleaseSource("TiJaWo68", "dicomeditor", UpdateChannel.STABLE), appJar);
    }

    public void openQuickGuide() {
        new QuickGuideDialog(parentFrame).setVisible(true);
    }

    public void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                LOGGER.info("Checking for updates...");
                UpdateInfo info = updater.checkForUpdates();
                if (info != null) {
                    LOGGER.info("Update found: " + info.version());
                    SwingUtilities.invokeLater(() -> showUpdateDialog(info.version()));
                } else {
                    LOGGER.info("No update found.");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parentFrame,
                            "Ihre Version " + currentVersion + " ist aktuell.", "Kein Update verfügbar",
                            JOptionPane.INFORMATION_MESSAGE));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to check for updates", e);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parentFrame,
                        "Fehler bei der Update-Prüfung: " + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE));
            }
        });
    }

    private void showUpdateDialog(String newVersion) {
        int option = JOptionPane.showConfirmDialog(parentFrame,
                "Eine neue Version von DicomEditor ist verfügbar: " + newVersion
                        + "\n\nMöchten Sie zur Download-Seite auf GitHub wechseln?",
                "Update verfügbar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            openUrl("https://github.com/TiJaWo68/dicomeditor/releases");
        }
    }

    public void openAboutDialog() {
        new AboutDialog(parentFrame).setVisible(true);
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "Fehler beim Öffnen der Webseite: " + ex.getMessage(), "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
