package de.in.dicom.tools.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.dcm4che3.data.Tag;
import org.junit.Test;

public class CSInfoProviderTest {

    @Test
    public void testGetDescriptionSingle() {
        String desc = CSInfoProvider.getDescription(Tag.Modality, "CT");
        assertNotNull(desc);
        assertEquals("CS_INFO:CT: Computed Tomography", desc);
    }

    @Test
    public void testGetDescriptionMultiValue() {
        String desc = CSInfoProvider.getDescription(Tag.ImageType, "ORIGINAL\\PRIMARY");
        assertNotNull(desc);
        // We want it to use "\n"
        assertEquals("CS_INFO:ORIGINAL: An image whose pixel values represent original observations\n" +
                "PRIMARY: An image created as a direct result of the patient examination", desc);
    }
}
