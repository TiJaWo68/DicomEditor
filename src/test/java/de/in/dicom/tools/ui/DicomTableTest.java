package de.in.dicom.tools.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.awt.FontMetrics;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Before;
import org.junit.Test;

public class DicomTableTest {

    private DicomTable table;

    @Before
    public void setUp() {
        DicomTableModel model = new DicomTableModel(new Attributes(), new Attributes());
        table = new DicomTable(model) {
            @Override
            public FontMetrics getFontMetrics(Font font) {
                return new FontMetrics(font) {
                    @Override
                    public int stringWidth(String str) {
                        if (str == null)
                            return 0;
                        return str.length() * font.getSize();
                    }
                };
            }
        };
    }

    @Test
    public void testZoomIn() {
        int initialSize = table.getFont().getSize();
        int initialHeight = table.getRowHeight();

        table.zoomIn();

        assertEquals(initialSize + 1, table.getFont().getSize());
        assertTrue(table.getRowHeight() > initialHeight);
    }

    @Test
    public void testZoomOut() {
        int initialSize = table.getFont().getSize();
        int initialHeight = table.getRowHeight();

        table.zoomOut();

        assertEquals(initialSize - 1, table.getFont().getSize());
        assertTrue(table.getRowHeight() < initialHeight);
    }

    @Test
    public void testResetZoom() {
        table.zoomIn();
        table.zoomIn();
        table.resetZoom();

        assertEquals(12, table.getFont().getSize());
        assertEquals(22, table.getRowHeight());
    }

    @Test
    public void testColumnWidthAdjustment() {
        // Setup data to ensure content width > header width
        Attributes attrs = new Attributes();
        attrs.setString(Tag.PatientName, VR.PN, "VeryLongPatientNameForTestingWidthAdjustment");
        DicomTableModel model = new DicomTableModel(new Attributes(), attrs);
        table.setModel(model);

        // Initial widths
        int tagWidth = table.getColumnModel().getColumn(0).getPreferredWidth();
        int nameWidth = table.getColumnModel().getColumn(1).getPreferredWidth();
        int vrWidth = table.getColumnModel().getColumn(2).getPreferredWidth();
        int lengthWidth = table.getColumnModel().getColumn(3).getPreferredWidth();

        // Trigger zoom multiple times to ensure measurable change
        table.zoomIn();
        table.zoomIn();
        table.zoomIn();
        table.zoomIn();
        table.zoomIn();

        // Assert widths increased
        assertTrue("Tag width should increase", table.getColumnModel().getColumn(0).getPreferredWidth() > tagWidth);
        assertTrue("Name width should increase", table.getColumnModel().getColumn(1).getPreferredWidth() > nameWidth);
        assertTrue("VR width should increase", table.getColumnModel().getColumn(2).getPreferredWidth() > vrWidth);
        assertTrue("Length width should increase",
                table.getColumnModel().getColumn(3).getPreferredWidth() > lengthWidth);

        // Check fixed width property (min == max == preferred)
        assertEquals(table.getColumnModel().getColumn(1).getPreferredWidth(),
                table.getColumnModel().getColumn(1).getMaxWidth());
        assertEquals(table.getColumnModel().getColumn(1).getPreferredWidth(),
                table.getColumnModel().getColumn(1).getMinWidth());
    }

    @Test
    public void testFMIDarkerBackground() {
        Attributes fmi = new Attributes();
        fmi.setString(Tag.FileMetaInformationVersion, VR.OB, "0100");
        DicomTableModel model = new DicomTableModel(fmi, new Attributes());
        table.setModel(model);

        // Row 0 should be FMI (Tag 0002,0001)
        java.awt.Color bgFMI = table.prepareRenderer(table.getCellRenderer(0, 0), 0, 0).getBackground();

        // Standard attribute row
        Attributes attrs = new Attributes();
        attrs.setString(Tag.PatientName, VR.PN, "Test");
        model = new DicomTableModel(new Attributes(), attrs);
        table.setModel(model);

        // Row 0 should be normal (Tag 0010,0010)
        java.awt.Color bgNormal = table.prepareRenderer(table.getCellRenderer(0, 0), 0, 0).getBackground();

        // FMI background should be different (darker)
        assertTrue("FMI background should be different from normal row", !bgFMI.equals(bgNormal));
    }

    @Test
    public void testLengthColumnRightAligned() {
        javax.swing.table.TableCellRenderer renderer = table.getColumnModel().getColumn(3).getCellRenderer();
        assertTrue("Renderer should be DefaultTableCellRenderer",
                renderer instanceof javax.swing.table.DefaultTableCellRenderer);
        assertEquals("Alignment should be RIGHT", javax.swing.JLabel.RIGHT,
                ((javax.swing.table.DefaultTableCellRenderer) renderer).getHorizontalAlignment());
    }

    @Test
    public void testStatusColumnCentered() {
        javax.swing.table.TableCellRenderer renderer = table.getColumnModel().getColumn(4).getCellRenderer();
        assertTrue("Renderer should be StatusRenderer",
                renderer.getClass().getName().contains("StatusRenderer"));

        // Trigger renderer
        ((javax.swing.table.DefaultTableCellRenderer) renderer).getTableCellRendererComponent(table, "UID_INFO:Test",
                false, false, 0, 4);

        assertEquals("Alignment should be CENTER", javax.swing.JLabel.CENTER,
                ((javax.swing.table.DefaultTableCellRenderer) renderer).getHorizontalAlignment());
    }

    @Test
    public void testTagColumnRightAligned() {
        javax.swing.table.TableCellRenderer renderer = table.getColumnModel().getColumn(0).getCellRenderer();
        assertTrue("Renderer should be IndentedRenderer",
                renderer.getClass().getName().contains("IndentedRenderer"));

        // Initial setup for the test row
        Attributes attrs = new Attributes();
        attrs.setString(Tag.PatientName, VR.PN, "Test");
        table.setModel(new DicomTableModel(new Attributes(), attrs));

        // Need to call getTableCellRendererComponent to trigger the alignment change
        ((javax.swing.table.DefaultTableCellRenderer) renderer).getTableCellRendererComponent(table, "0008,0010", false,
                false, 0, 0);

        assertEquals("Alignment should be RIGHT", javax.swing.JLabel.RIGHT,
                ((javax.swing.table.DefaultTableCellRenderer) renderer).getHorizontalAlignment());
    }
}
