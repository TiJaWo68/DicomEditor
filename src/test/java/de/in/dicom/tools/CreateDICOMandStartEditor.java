package de.in.dicom.tools;

import java.io.File;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;

/**
 * Test utility to create a sample DICOM file with nested sequences and launch
 * the DicomEditor.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class CreateDICOMandStartEditor {

	public static void main(String[] args) throws IOException {
		Attributes a = new Attributes();
		a.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
		a.setString(Tag.SOPClassUID, VR.UI, UID.EnhancedSRStorage);
		for (int i = 0; i < 3; i++)
			addSequences(a, 10010000 + i * 10);
		a.setString(10030000, VR.LO, "Ende");
		File file = new File("testSequences.dcm");
		try (DicomOutputStream dos = new DicomOutputStream(file)) {
			dos.writeDataset(a.createFileMetaInformation(UID.ExplicitVRLittleEndian), a);
		}
		DicomEditor.main(new String[] { file.getAbsolutePath() });
	}

	protected static void addSequences(Attributes a, int tagOffSet) {
		for (int i = 0; i < 3; i++) {
			Sequence seq = a.newSequence(tagOffSet + i, 0);
			for (int j = 0; j < 3; j++) {
				Attributes sa = new Attributes();
				sa.setString(10010000 + tagOffSet + j, VR.LO, i + " " + j);
				if (tagOffSet < 10010030)
					addSequences(sa, 10020000 + i * 10);
				sa.setString(10010000 + tagOffSet + j + 1, VR.LO, i + " " + j);
				sa.setString(10010000 + tagOffSet + j + 2, VR.LO, i + " " + j);
				seq.add(sa);
			}
		}
	}

}
