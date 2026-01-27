package de.in.dicom.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import javax.swing.JScrollPane;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import de.in.dicom.tools.ui.DicomTable;

public class DicomEditorFrameTest {

    @Test
    public void testConstructorWithFile() throws IOException {
        Attributes fmi = new Attributes();
        Attributes dicom = new Attributes();
        dicom.setString(Tag.PatientName, VR.PN, "Test^Reproduction");

        File dummyFile = File.createTempFile("test", ".dcm");
        dummyFile.deleteOnExit();

        DicomEditorFrame frame = new DicomEditorFrame(dummyFile, fmi, dicom);

        // The frame should have a table
        assertNotNull(frame.getTitle());
        assertEquals("DicomEditor : " + dummyFile.getName(), frame.getTitle());

        // Check how many components are in the content pane
        // It should only have one JScrollPane
        int scrollPaneCount = 0;
        for (java.awt.Component comp : frame.getContentPane().getComponents()) {
            if (comp instanceof JScrollPane) {
                scrollPaneCount++;
                JScrollPane sp = (JScrollPane) comp;
                if (sp.getViewport().getView() instanceof DicomTable) {
                    DicomTable table = (DicomTable) sp.getViewport().getView();
                    // The table should have content
                    // We added one tag to dicom
                    assertEquals(1, table.getModel().getRowCount());
                }
            }
        }

        assertEquals("Should have exactly one JScrollPane", 1, scrollPaneCount);
    }
}
