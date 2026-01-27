package de.in.dicom.tools.ui;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for DicomValidator, verifying various Value Representation (VR)
 * and tag-specific validation rules.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomValidatorTest {

    @Test
    public void testValidatePatientSex() {
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "M").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "F").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "O").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "").isOk());

        DicomValidator.ValidationResult error = DicomValidator.validate(Tag.PatientSex, VR.CS, "X");
        assertTrue(error.isError());
        assertEquals("PatientSex (0010,0040) must be 'M', 'F', or 'O'.", error.message);
    }

    @Test
    public void testValidatePatientName() {
        assertTrue(DicomValidator.validate(Tag.PatientName, VR.PN, "Doe^John").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientName, VR.PN, "Family^Given^Middle^Prefix^Suffix").isOk());

        DicomValidator.ValidationResult warning = DicomValidator.validate(Tag.PatientName, VR.PN,
                "Too^Many^Components^In^This^Name");
        assertTrue(warning.isWarning());
        assertTrue(warning.message.contains("usually has max 5 components"));
    }

    @Test
    public void testValidateVRs() {
        // AE
        assertTrue(DicomValidator.validate(Tag.RetrieveAETitle, VR.AE, "1-n", "MY_STORE").isOk());
        assertTrue(DicomValidator.validate(Tag.RetrieveAETitle, VR.AE, "1-n", "THIS_AET_IS_WAY_TOO_LONG").isError());
        assertTrue(DicomValidator.validate(Tag.RetrieveAETitle, VR.AE, "1", "AET\\TITLE").isError()); // VM mismatch
        assertTrue(DicomValidator.validate(Tag.RetrieveAETitle, VR.AE, "1-n", "AET1\\AET2").isOk()); // Multiple OK for
                                                                                                     // 1-n
        assertTrue(DicomValidator.validate(Tag.RetrieveAETitle, VR.AE, "1-n", "   ").isError()); // solely spaces

        // AS
        assertTrue(DicomValidator.validate(Tag.PatientAge, VR.AS, "1", "056Y").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientAge, VR.AS, "1", "123").isError());
        assertTrue(DicomValidator.validate(Tag.PatientAge, VR.AS, "1", "010X").isError());

        // AT
        assertTrue(DicomValidator.validate(Tag.DimensionIndexPointer, VR.AT, "1-n", "00100010").isOk());
        assertTrue(DicomValidator.validate(Tag.DimensionIndexPointer, VR.AT, "1-n", "GGGG-EEEE").isError());

        // CS
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "1", "M").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "1", "invalid").isError());
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "1", "WAY_TOO_LONG_FOR_CS").isError());

        // DA
        assertTrue(DicomValidator.validate(Tag.Date, VR.DA, "1", "20231025").isOk());
        assertTrue(DicomValidator.validate(Tag.Date, VR.DA, "1", "20231301").isError()); // invalid month
        assertTrue(DicomValidator.validate(Tag.Date, VR.DA, "1", "20231032").isError()); // invalid day

        // DS
        assertTrue(DicomValidator.validate(Tag.PatientSize, VR.DS, "1", "1.85").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientSize, VR.DS, "1", " 1.23 ").isOk()); // leading/trailing space
        assertTrue(DicomValidator.validate(Tag.PatientSize, VR.DS, "1", "1 . 23").isError()); // embedded space
        assertTrue(DicomValidator.validate(Tag.PatientSize, VR.DS, "1", "NotANumber").isError());

        // DT
        assertTrue(DicomValidator.validate(Tag.DateTime, VR.DT, "1", "20231025123000.123456+0100").isOk());
        assertTrue(DicomValidator.validate(Tag.DateTime, VR.DT, "1", "20231025").isOk());
        assertTrue(DicomValidator.validate(Tag.DateTime, VR.DT, "1", "invalid").isError());

        // IS
        assertTrue(DicomValidator.validate(Tag.InstanceNumber, VR.IS, "1", "1").isOk());
        assertTrue(DicomValidator.validate(Tag.InstanceNumber, VR.IS, "1", "2147483647").isOk());
        assertTrue(DicomValidator.validate(Tag.InstanceNumber, VR.IS, "1", "2147483648").isError()); // overflow
        assertTrue(DicomValidator.validate(Tag.InstanceNumber, VR.IS, "1", "1.5").isError());

        // LO/SH
        assertTrue(
                DicomValidator.validate(Tag.LongCodeValue, VR.LO, "1", "Some Long String with \u001B escape").isOk());
        assertTrue(DicomValidator.validate(Tag.LongCodeValue, VR.LO, "1", "Back\\Slash").isError());

        // TM
        assertTrue(DicomValidator.validate(Tag.Time, VR.TM, "1", "121838.993").isOk());
        assertTrue(DicomValidator.validate(Tag.Time, VR.TM, "1", "240000").isError()); // 24 is invalid
        assertTrue(DicomValidator.validate(Tag.Time, VR.TM, "1", "126000").isError()); // 60 min is invalid
        assertTrue(DicomValidator.validate(Tag.Time, VR.TM, "1", "abc").isError());

        // UI
        assertTrue(DicomValidator.validate(Tag.SOPInstanceUID, VR.UI, "1", "1.2.3.4").isOk());
        assertTrue(DicomValidator.validate(Tag.SOPInstanceUID, VR.UI, "1", "abc.123").isError());

        // UL/US/UV
        assertTrue(DicomValidator.validate(Tag.SimpleFrameList, VR.UL, "1", "4294967295").isOk());
        assertTrue(DicomValidator.validate(Tag.SimpleFrameList, VR.UL, "1", "4294967296").isError());
        assertTrue(DicomValidator.validate(Tag.Columns, VR.US, "1", "65535").isOk());
        assertTrue(DicomValidator.validate(Tag.Columns, VR.US, "1", "65536").isError());

        // Multiplicity
        assertTrue("DS with multiplicity 6 should be OK",
                DicomValidator.validate(Tag.ImageOrientationPatient, VR.DS, "6", "1\\0\\0\\0\\1\\0").isOk());
        assertTrue("DS with multiplicity 6 but only 5 values should be Error",
                DicomValidator.validate(Tag.ImageOrientationPatient, VR.DS, "6", "1\\0\\0\\0\\1").isError());
        assertTrue("DS with 1-n should allow any number",
                DicomValidator.validate(Tag.ImageOrientationPatient, VR.DS, "1-n", "1\\2\\3").isOk());
        assertTrue("Multi-valued error should report component",
                DicomValidator.validate(Tag.ImageOrientationPatient, VR.DS, "1-n", "1\\invalid\\3").message
                        .contains("component 'invalid'"));
    }
}
