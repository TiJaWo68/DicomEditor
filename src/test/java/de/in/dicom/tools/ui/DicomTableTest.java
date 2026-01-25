package de.in.dicom.tools.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dcm4che3.data.Attributes;
import org.junit.Before;
import org.junit.Test;

public class DicomTableTest {

    private DicomTable table;

    @Before
    public void setUp() {
        DicomTableModel model = new DicomTableModel(new Attributes(), new Attributes());
        table = new DicomTable(model);
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
}
