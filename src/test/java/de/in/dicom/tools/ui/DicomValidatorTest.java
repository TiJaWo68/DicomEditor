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
        assertTrue(DicomValidator.validate(Tag.RetrieveAETitle, VR.AE, "MY_STORE").isOk());
        assertTrue(DicomValidator.validate(Tag.RetrieveAETitle, VR.AE, "THIS_AET_IS_WAY_TOO_LONG").isError());

        // AS
        assertTrue(DicomValidator.validate(Tag.PatientAge, VR.AS, "056Y").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientAge, VR.AS, "123").isError());

        // CS
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "M").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientSex, VR.CS, "invalid").isError()); // lowercase not allowed in CS
                                                                                         // by default or too long

        // DA
        assertTrue(DicomValidator.validate(Tag.Date, VR.DA, "20231025").isOk());
        assertTrue(DicomValidator.validate(Tag.Date, VR.DA, "invalid").isError());

        // DS
        assertTrue(DicomValidator.validate(Tag.PatientSize, VR.DS, "1.85").isOk());
        assertTrue(DicomValidator.validate(Tag.PatientSize, VR.DS, "NotANumber").isError());

        // IS
        assertTrue(DicomValidator.validate(Tag.InstanceNumber, VR.IS, "1").isOk());
        assertTrue(DicomValidator.validate(Tag.InstanceNumber, VR.IS, "1.5").isError());

        // TM
        assertTrue(DicomValidator.validate(Tag.Time, VR.TM, "121838.993").isOk());
        assertTrue(DicomValidator.validate(Tag.Time, VR.TM, "999999").isOk()); // Regex allows it, but logically invalid
        assertTrue(DicomValidator.validate(Tag.Time, VR.TM, "abc").isError());

        // UI
        assertTrue(DicomValidator.validate(Tag.SOPInstanceUID, VR.UI, "1.2.3.4").isOk());
        assertTrue(DicomValidator.validate(Tag.SOPInstanceUID, VR.UI, "abc.123").isError());
    }
}
