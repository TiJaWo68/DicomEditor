package de.in.dicom.tools.ui;

import static org.junit.Assert.*;
import org.junit.Test;
import org.dcm4che3.data.Tag;

public class TagInfoProviderTest {

    @Test
    public void testTagLookup() {
        // PatientName: (0010,0010)
        TagInfoProvider.TagData patientName = TagInfoProvider.getTagData(Tag.PatientName);
        assertNotNull("PatientName tag should be found", patientName);
        assertEquals("Patient's Name", patientName.name());
        assertEquals("PatientName", patientName.keyword());
        assertEquals("PN", patientName.vr());
        assertEquals("1", patientName.vm());

        // SpecificCharacterSet: (0008,0005)
        TagInfoProvider.TagData specificCharacterSet = TagInfoProvider.getTagData(Tag.SpecificCharacterSet);
        assertNotNull("SpecificCharacterSet tag should be found", specificCharacterSet);
        assertEquals("Specific Character Set", specificCharacterSet.name());
        assertEquals("CS", specificCharacterSet.vr());
        assertEquals("1-n", specificCharacterSet.vm());
    }

    @Test
    public void testRetiredTag() {
        // Length to End: (0008,0001) - Retired
        TagInfoProvider.TagData lengthToEnd = TagInfoProvider.getTagData(0x00080001);
        assertNotNull("LengthToEnd tag should be found", lengthToEnd);
        assertEquals("Length to End", lengthToEnd.name());
        assertEquals("RET", lengthToEnd.notes());
    }
}
