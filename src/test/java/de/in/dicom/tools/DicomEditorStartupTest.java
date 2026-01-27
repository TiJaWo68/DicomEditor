package de.in.dicom.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for DicomEditor startup logic.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomEditorStartupTest {

    private File validFile;
    private File invalidFile;

    @Before
    public void setUp() throws IOException {
        validFile = File.createTempFile("valid", ".dcm");
        validFile.deleteOnExit();
        Attributes a = new Attributes();
        a.setString(Tag.PatientName, VR.PN, "Test^Startup");
        a.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5.6.7.8.9.1");
        a.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        try (DicomOutputStream dos = new DicomOutputStream(validFile)) {
            dos.writeDataset(a.createFileMetaInformation(UID.ExplicitVRLittleEndian), a);
        }

        invalidFile = new File("non-existent-file-12345.dcm");
    }

    @After
    public void tearDown() {
        for (Frame frame : Frame.getFrames()) {
            frame.dispose();
        }
    }

    @Test
    public void testCreateFrameSyncValid() throws Exception {
        DicomEditorFrame frame = DicomEditorFrame.createFrameSync(validFile);
        assertNotNull("Frame should be created for valid file", frame);
        assertTrue("Frame should be visible", frame.isVisible());
        assertTrue("Title should contain filename", frame.getTitle().contains(validFile.getName()));
    }

    @Test
    public void testCreateFrameSyncInvalid() throws Exception {
        DicomEditorFrame frame = DicomEditorFrame.createFrameSync(invalidFile);
        assertEquals("Frame should NOT be created for invalid file", null, frame);
    }

    @Test
    public void testMainLogicNoArgs() throws Exception {
        DicomEditor.main(new String[0]);

        // Wait for all EDT events to process
        SwingUtilities.invokeAndWait(() -> {
        });
        Thread.sleep(1000);

        int count = 0;
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof DicomEditorFrame && frame.isVisible()) {
                count++;
            }
        }
        assertEquals("Should have exactly one empty frame", 1, count);
    }

    @Test
    public void testMainLogicInvalidArgs() throws Exception {
        DicomEditor.main(new String[] { invalidFile.getPath() });

        // Wait for all EDT events to process
        SwingUtilities.invokeAndWait(() -> {
        });
        Thread.sleep(1000);

        int count = 0;
        DicomEditorFrame foundFrame = null;
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof DicomEditorFrame && frame.isVisible()) {
                count++;
                foundFrame = (DicomEditorFrame) frame;
            }
        }
        assertEquals("Should have exactly one frame (fallback)", 1, count);
        assertTrue("Fallback frame should be 'New'", foundFrame.getTitle().contains("New"));
    }
}
