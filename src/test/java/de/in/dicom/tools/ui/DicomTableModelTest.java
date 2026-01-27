package de.in.dicom.tools.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for DicomTableModel, covering hierarchical structure, sequence
 * expansion, and attribute editing.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomTableModelTest {

    private Attributes attrs;
    private DicomTableModel model;

    @Before
    public void setUp() {
        attrs = new Attributes();
        attrs.setString(Tag.PatientName, VR.PN, "John Doe");
        attrs.setString(Tag.PatientID, VR.LO, "12345");

        // Add Sequence
        Attributes seqItem = new Attributes();
        seqItem.setString(Tag.CodeValue, VR.SH, "C1");
        attrs.newSequence(Tag.ConceptNameCodeSequence, 1).add(seqItem);

        model = new DicomTableModel(null, attrs);
    }

    @Test
    public void testStructure() {
        // Flat list should have 3 items initially (top level tags)
        // PatientName, PatientID, ConceptNameCodeSequence
        assertEquals(3, model.getRowCount());

        // Check content
        boolean foundPN = false;
        boolean foundSQ = false;

        for (int i = 0; i < model.getRowCount(); i++) {
            String tagStr = (String) model.getValueAt(i, 0);
            if (tagStr.equals("0010,0010"))
                foundPN = true;
            if (tagStr.equals("0040,A043"))
                foundSQ = true;
        }

        assertTrue(foundPN);
        assertTrue(foundSQ);
    }

    @Test
    public void testExpansion() {
        int sqIndex = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            String tagStr = (String) model.getValueAt(i, 0);
            if (tagStr.equals("0040,A043")) {
                sqIndex = i;
                break;
            }
        }

        assertTrue("Sequence not found", sqIndex != -1);

        // Toggle expansion
        model.toggleExpansion(sqIndex);

        // Now should have +1 row (the Item)
        assertEquals(4, model.getRowCount());

        // Check Item row
        String itemLabel = (String) model.getValueAt(sqIndex + 1, 0);
        assertEquals("Item #1", itemLabel);

        // Toggle item expansion
        model.toggleExpansion(sqIndex + 1);

        // Now +1 row (CodeValue)
        assertEquals(5, model.getRowCount());
        String codeTag = (String) model.getValueAt(sqIndex + 2, 0);
        assertEquals("0008,0100", codeTag);
    }

    @Test
    public void testAddAttribute() {
        int initialCount = model.getRowCount();
        model.addAttribute(-1, Tag.StudyDate, VR.DA); // Add to root

        assertEquals(initialCount + 1, model.getRowCount());
        assertTrue(attrs.contains(Tag.StudyDate));
    }

    @Test
    public void testRemoveRow() {
        int pnIndex = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            String tagStr = (String) model.getValueAt(i, 0);
            if (tagStr.equals("0010,0010")) {
                pnIndex = i;
                break;
            }
        }

        model.removeRow(pnIndex);

        assertEquals(2, model.getRowCount());
        assertTrue(!attrs.contains(Tag.PatientName));
    }

    @Test
    public void testSqlLengthAndValue() {
        int sqIndex = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            String tagStr = (String) model.getValueAt(i, 0);
            if (tagStr.equals("0040,A043")) {
                sqIndex = i;
                break;
            }
        }

        assertTrue("Sequence not found", sqIndex != -1);

        // Verify VR column (index 2) returns "SQ"
        Object vrValue = model.getValueAt(sqIndex, 2);
        assertEquals("SQ", vrValue);

        // Verify Length column (index 3) returns empty string for SQ
        Object lengthValue = model.getValueAt(sqIndex, 3);
        assertEquals("", lengthValue);

        // Verify Content column (index 5) returns empty string for SQ
        Object contentValue = model.getValueAt(sqIndex, 5);
        assertEquals("", contentValue);
    }

    @Test
    public void testPrivateTagVRUNAsString() {
        int privateTag = 0x00091001;
        attrs.setBytes(privateTag, VR.UN, "Hello".getBytes());
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0009,1001")) {
                row = i;
                break;
            }
        }
        assertTrue("Private tag not found", row != -1);
        assertEquals("Hello", model.getValueAt(row, 5));
        assertTrue("Should be editable", model.isCellEditable(row, 5));
    }

    @Test
    public void testPrivateTagVRUNAsHex() {
        int privateTag = 0x00091002;
        byte[] nonPrintable = { 0x01, 0x02, 0x03 };
        attrs.setBytes(privateTag, VR.UN, nonPrintable);
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0009,1002")) {
                row = i;
                break;
            }
        }
        assertTrue("Private tag not found", row != -1);
        String hexValue = (String) model.getValueAt(row, 5);
        assertTrue("Should be hex formatted", hexValue.startsWith("01 02 03"));
        assertTrue("Should NOT be editable", !model.isCellEditable(row, 5));
    }

    @Test
    public void testNonPrivateTagVRUNAsHex() {
        // Picking a tag that could be UN if unknown, but better use a known public tag
        // with UN
        int knownPublicTag = 0x00080005; // SpecificCharacterSet
        attrs.setBytes(knownPublicTag, VR.UN, "ISO_IR 100".getBytes());
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0008,0005")) {
                row = i;
                break;
            }
        }
        assertTrue("Public tag not found", row != -1);
        String value = (String) model.getValueAt(row, 5);
        // Should be hex because it's public VR.UN
        assertTrue("Should be hex formatted", value.contains(" "));
        assertTrue("Should NOT be editable", !model.isCellEditable(row, 5));
    }

    @Test
    public void testOBOWNotEditable() {
        attrs.setBytes(Tag.PixelData, VR.OB, new byte[10]);
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("7FE0,0010")) {
                row = i;
                break;
            }
        }
        assertTrue("PixelData not found", row != -1);
        assertTrue("Should NOT be editable", !model.isCellEditable(row, 5));
    }

    @Test
    public void testPrivateTagVRUNSetValue() throws java.io.IOException {
        int privateTag = 0x00091001;
        attrs.setBytes(privateTag, VR.UN, "OldValue".getBytes());
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0009,1001")) {
                row = i;
                break;
            }
        }

        model.setValueAt("NewValue", row, 5);

        assertEquals("NewValue", model.getValueAt(row, 5));
        assertEquals("NewValue", attrs.getSpecificCharacterSet().decode(attrs.getBytes(privateTag), null));
    }

    @Test
    public void testStatusColumnUIDInfo() {
        attrs.setString(Tag.SOPClassUID, VR.UI, "1.2.840.10008.1.1");
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0008,0016")) {
                row = i;
                break;
            }
        }
        assertTrue("SOPClassUID not found", row != -1);
        String status = (String) model.getValueAt(row, 4);
        assertTrue("Status should contain UID info", status.startsWith("UID_INFO:"));
        assertTrue("Status should contain 'Verification'", status.contains("Verification"));
    }

    @Test
    public void testStatusColumnUnknownUIDInfo() {
        attrs.setString(Tag.SOPClassUID, VR.UI, "1.2.3.4");
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0008,0016")) {
                row = i;
                break;
            }
        }
        assertTrue("SOPClassUID not found", row != -1);
        String status = (String) model.getValueAt(row, 4);
        assertEquals("Status should be empty for unknown UID", "", status);
    }

    @Test
    public void testStatusColumnValidationAlert() {
        // Invalid Date format
        attrs.setString(Tag.StudyDate, VR.DA, "invalid-date");
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0008,0020")) {
                row = i;
                break;
            }
        }
        assertTrue("StudyDate not found", row != -1);
        String status = (String) model.getValueAt(row, 4);
        assertTrue("Status should contain validation alert", status.startsWith("VALIDATION_ALERT:"));
    }

    @Test
    public void testStatusColumnCSInfo() {
        attrs.setString(Tag.Modality, VR.CS, "CT");
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0008,0060")) {
                row = i;
                break;
            }
        }
        assertTrue("Modality not found", row != -1);
        String status = (String) model.getValueAt(row, 4);
        assertTrue("Status should contain CS info", status.startsWith("CS_INFO:"));
        assertTrue("Status should contain 'Computed Tomography'", status.contains("Computed Tomography"));
    }

    @Test
    public void testStatusColumnCSInfoMultiValue() {
        attrs.setString(Tag.ImageType, VR.CS, "ORIGINAL\\PRIMARY\\AXIAL");
        model = new DicomTableModel(null, attrs);

        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("0008,0008")) {
                row = i;
                break;
            }
        }
        assertTrue("ImageType not found", row != -1);
        String status = (String) model.getValueAt(row, 4);
        assertTrue("Status should contain CS info", status.startsWith("CS_INFO:"));
        assertTrue("Status should contain 'ORIGINAL'", status.contains("ORIGINAL"));
        assertTrue("Status should contain 'PRIMARY'", status.contains("PRIMARY"));
        assertTrue("Status should contain 'AXIAL'", status.contains("AXIAL"));
    }
}
