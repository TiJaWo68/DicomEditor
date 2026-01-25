package de.in.dicom.tools.ui;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.in.dicom.tools.DicomEditorFrame;
import de.in.dicom.tools.ui.actions.HelpActions;

/**
 * Main Menu Bar for DicomEditor.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomEditorMenu extends JMenuBar {

    public DicomEditorMenu(DicomEditorFrame frame) {
        HelpActions helpActions = new HelpActions(frame);

        JMenu mFile = new JMenu("File");
        add(mFile);

        JMenuItem miLoad = new JMenuItem("Open");
        miLoad.addActionListener(e -> frame.openFile());
        mFile.add(miLoad);

        JMenuItem miSave = new JMenuItem("Save");
        miSave.addActionListener(e -> frame.saveFile(false));
        mFile.add(miSave);

        JMenuItem miSaveAs = new JMenuItem("Save As...");
        miSaveAs.addActionListener(e -> frame.saveFile(true));
        mFile.add(miSaveAs);

        mFile.addSeparator();

        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> System.exit(0));
        mFile.add(miExit);

        JMenu mEdit = new JMenu("Edit");
        add(mEdit);

        JMenu mCharset = new JMenu("Specific Character Set");
        mEdit.add(mCharset);

        String[] charsets = { "ISO_IR 100", "ISO_IR 192", "GB18030", "ISO_IR 144", "ISO_IR 127", "ISO_IR 138",
                "ISO_IR 13" };
        for (String cs : charsets) {
            JMenuItem mi = new JMenuItem(cs);
            mi.addActionListener(e -> frame.setCharacterSet(cs));
            mCharset.add(mi);
        }

        // Right alignment for Help menu
        add(Box.createHorizontalGlue());

        JMenu helpMenu = new JMenu("Hilfe");
        JMenuItem quickGuideItem = new JMenuItem("Kurzanleitung");
        quickGuideItem.addActionListener(e -> helpActions.openQuickGuide());
        helpMenu.add(quickGuideItem);

        JMenuItem updateItem = new JMenuItem("Update");
        updateItem.addActionListener(e -> helpActions.checkForUpdates());
        helpMenu.add(updateItem);

        helpMenu.addSeparator();

        JMenuItem aboutItem = new JMenuItem("Über DicomEditor");
        aboutItem.addActionListener(e -> helpActions.openAboutDialog());
        helpMenu.add(aboutItem);

        add(helpMenu);
    }
}
