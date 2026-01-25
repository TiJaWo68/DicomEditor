package de.in.dicom.tools;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;

import de.in.dicom.tools.ui.DicomEditorMenu;
import de.in.dicom.tools.ui.DicomTable;
import de.in.dicom.tools.ui.DicomTableModel;
import de.in.dicom.tools.ui.dialog.AddTagDialog;
import de.in.icons.DicomEditorIcon;
import de.in.utils.gui.FrameIcons;

/**
 * Main window of the DICOM editor. Handles file loading, saving, and the main
 * table display.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomEditorFrame extends JFrame {

	private static final Logger LOGGER = LogManager.getLogger(DicomEditorFrame.class);

	private File file;
	private Attributes fmi, dicom;
	private DicomTable table;

	public DicomEditorFrame() {
		super("DicomEditor");
		initMenu();
		// Add valid empty attributes for empty frame
		dicom = new Attributes();
		fmi = new Attributes();
		addTable(null, fmi, dicom);

		addDropTargetListener();
		setIconImages(FrameIcons.createIconList(DicomEditorIcon.class, null, 16, 32, 48, 64, 128, 256));

		// Handle close operation manually to support "Last Frame" logic if needed, but
		// DISPOSE_ON_CLOSE is usually sufficient if the main
		// method waits or main frame logic exists. Requirement: Schließen schließt den
		// aktuellen Frame (und beendet den DicomEditor wenn es
		// der letzte war)
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (JFrame.getFrames().length == 0) {
					System.exit(0);
				}
			}
		});

		setPreferredSize(new Dimension(800, 1024));
		pack();
	}

	public DicomEditorFrame(File file, Attributes fmi, Attributes dicom) throws HeadlessException {
		this();
		addTable(file, fmi, dicom);
		pack();
	}

	private void initMenu() {
		setJMenuBar(new DicomEditorMenu(this));
	}

	public void setCharacterSet(String cs) {
		if (dicom != null) {
			dicom.setString(Tag.SpecificCharacterSet, VR.CS, cs);
			repaint();
		}
	}

	public void openFile() {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			new LoadWorker(fc.getSelectedFile(), this).execute();
		}
	}

	public void saveFile(boolean saveAs) {
		if (dicom == null)
			return;

		File target = file;
		if (saveAs || target == null) {
			JFileChooser fc = new JFileChooser(file != null ? file.getParentFile() : null);
			if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			target = fc.getSelectedFile();
		}

		try (DicomOutputStream dos = new DicomOutputStream(target)) {
			dos.writeDataset(fmi, dicom);
			file = target;
			setTitle("DicomEditor : " + file.getName());
			JOptionPane.showMessageDialog(this, "File saved successfully.");
		} catch (IOException e) {
			LOGGER.error("Error saving file", e);
			JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void addDropTargetListener() {
		DropTarget dt = new DropTarget();
		try {
			dt.addDropTargetListener(new DropTargetAdapter() {

				@Override
				public void drop(DropTargetDropEvent dtde) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					try {
						List<File> fileList = (List<File>) dtde.getTransferable()
								.getTransferData(DataFlavor.javaFileListFlavor);
						if (fileList != null)
							for (File file : fileList)
								new LoadWorker(file, DicomEditorFrame.this).execute();
					} catch (UnsupportedFlavorException | IOException ex) {
						LOGGER.warn("", ex);
					}
				}
			});
		} catch (TooManyListenersException ex) {
			LOGGER.warn("", ex);
		}
		setDropTarget(dt);
	}

	protected void addFile(File file) {
		new LoadWorker(file, this).execute();
	}

	private static class LoadResult {
		File file;
		Attributes fmi;
		Attributes dicom;

		LoadResult(File file, Attributes fmi, Attributes dicom) {
			this.file = file;
			this.fmi = fmi;
			this.dicom = dicom;
		}
	}

	private static class LoadWorker extends SwingWorker<LoadResult, Void> {
		private final File file;
		private final DicomEditorFrame frame;

		LoadWorker(File file, DicomEditorFrame frame) {
			this.file = file;
			this.frame = frame;
		}

		@Override
		protected LoadResult doInBackground() throws Exception {
			if (!file.canRead()) {
				throw new IOException("Cannot read file: " + file.getPath());
			}
			try (DicomInputStream dis = new DicomInputStream(file)) {
				return new LoadResult(file, dis.readFileMetaInformation(), dis.readDataset());
			}
		}

		@Override
		protected void done() {
			try {
				LoadResult result = get();
				if (frame != null) {
					if (frame.getContentPane().getComponentCount() > 0) {
						frame.getContentPane().removeAll();
					}
					frame.addTable(result.file, result.fmi, result.dicom);
					frame.revalidate();
					frame.repaint();
				} else {
					DicomEditorFrame newFrame = new DicomEditorFrame(result.file, result.fmi, result.dicom);
					newFrame.setVisible(true);
				}
			} catch (Exception ex) {
				LOGGER.warn("could not create editor for " + file.getPath(), ex);
			}
		}
	}

	public static void createFrame(File file) {
		new LoadWorker(file, null).execute();
	}

	/**
	 * @param file
	 * @param fmi
	 * @param dicom
	 */
	protected void addTable(File file, Attributes fmi, Attributes dicom) {
		this.file = file;
		this.fmi = fmi;
		this.dicom = dicom;
		setTitle("DicomEditor : " + (file != null ? file.getName() : "New"));

		if (dicom != null) {
			table = new DicomTable(new DicomTableModel(fmi, dicom));
			JScrollPane sp = new JScrollPane(table);
			add(sp);
			createContextMenu();
		}
	}

	private void createContextMenu() {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem addTag = new JMenuItem("Add Tag...");
		addTag.addActionListener(e -> {
			AddTagDialog dialog = new AddTagDialog();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
			if (dialog.isApproved()) {
				int tag = dialog.getTag();
				VR vr = dialog.getVR();
				if (tag != -1) {
					int row = table.getSelectedRow();
					DicomTableModel model = (DicomTableModel) table.getModel();
					model.addAttribute(row, tag, vr);
				}
			}
		});
		menu.add(addTag);

		JMenuItem delTag = new JMenuItem("Delete Tag");
		delTag.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row != -1) {
				DicomTableModel model = (DicomTableModel) table.getModel();
				model.removeRow(row);
			}
		});
		menu.add(delTag);

		table.setComponentPopupMenu(menu);
	}
}
