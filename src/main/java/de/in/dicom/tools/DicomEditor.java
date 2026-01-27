package de.in.dicom.tools;

import java.awt.EventQueue;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.FlatDarculaLaf;

import de.in.utils.Log4jTools;

/**
 * Main entry point for the DicomEditor application. Sets up look and feel and
 * opens initial frames.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomEditor {

	private static final Logger LOGGER = LogManager.getLogger(DicomEditor.class);

	public static void main(String[] args) {
		Log4jTools.redirectStdOutErrLog();
		Log4jTools.logEnvironment(LOGGER);
		EventQueue.invokeLater(() -> {
			FlatDarculaLaf.setup();
			int windowsOpened = 0;
			if (args.length > 0) {
				for (String filename : args) {
					File file = new File(filename);
					if (DicomEditorFrame.createFrameSync(file) != null) {
						windowsOpened++;
					}
				}
			}
			if (windowsOpened == 0) {
				DicomEditorFrame frame = new DicomEditorFrame();
				frame.setVisible(true);
			}
		});
	}

}
