package de.in.dicom.tools.ui;

import static org.junit.Assert.*;
import org.junit.Test;

public class VRInfoProviderTest {

    @Test
    public void testGetHtmlInfo() {
        String aeInfo = VRInfoProvider.getHtmlInfo("AE");
        assertNotNull(aeInfo);
        assertTrue(aeInfo.contains("Application Entity"));
        assertTrue(aeInfo.contains("16 bytes maximum"));

        String pnInfo = VRInfoProvider.getHtmlInfo("PN");
        assertNotNull(pnInfo);
        assertTrue(pnInfo.contains("Person Name"));
        assertTrue(pnInfo.contains("family^given^middle^prefix^suffix"));

        assertNull(VRInfoProvider.getHtmlInfo("NON_EXISTENT"));
    }
}
