package de.in.dicom.tools.ui;

import java.util.HashMap;
import java.util.Map;
import org.dcm4che3.data.Tag;

/**
 * Provides human-readable explanations for DICOM Code String (CS) tags.
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class CSInfoProvider {

    private static final Map<Integer, Map<String, String>> TAG_VALUE_MAP = new HashMap<>();

    static {
        // (0010,0040) Patient's Sex
        addTagInfo(Tag.PatientSex, Map.of(
                "M", "Male",
                "F", "Female",
                "O", "Other"));

        // (0008,0060) Modality
        addTagInfo(Tag.Modality, Map.ofEntries(
                Map.entry("AR", "Autorefraction"),
                Map.entry("AU", "Audio"),
                Map.entry("BDUS", "Bone Density (Ultrasound)"),
                Map.entry("BMD", "Bone Mineral Densitometry"),
                Map.entry("CR", "Computed Radiography"),
                Map.entry("CT", "Computed Tomography"),
                Map.entry("DG", "Diaphanography"),
                Map.entry("DX", "Digital Radiography"),
                Map.entry("ECG", "Electrocardiography"),
                Map.entry("EPS", "Cardiac Electrophysiology"),
                Map.entry("ES", "Endoscopy"),
                Map.entry("GM", "General Microscopy"),
                Map.entry("HC", "Hard Copy"),
                Map.entry("HD", "Hemodynamic Waveform"),
                Map.entry("IO", "Intra-oral Radiography"),
                Map.entry("IVUS", "Intravascular Ultrasound"),
                Map.entry("KO", "Key Object Selection"),
                Map.entry("LS", "Laser Surface Scanner"),
                Map.entry("MG", "Mammography"),
                Map.entry("MR", "Magnetic Resonance"),
                Map.entry("NM", "Nuclear Medicine"),
                Map.entry("OCT", "Optical Coherence Tomography"),
                Map.entry("OP", "Ophthalmic Photography"),
                Map.entry("OPM", "Ophthalmic Mapping"),
                Map.entry("OPR", "Ophthalmic Refraction"),
                Map.entry("OPT", "Ophthalmic Tomography"),
                Map.entry("OPV", "Ophthalmic Visual Field"),
                Map.entry("OT", "Other"),
                Map.entry("PR", "Presentation State"),
                Map.entry("PT", "Positron Emission Tomography (PET)"),
                Map.entry("PX", "Panoramic X-Ray"),
                Map.entry("REG", "Registration"),
                Map.entry("RF", "Radiofluoroscopy"),
                Map.entry("RG", "Radiographic imaging"),
                Map.entry("RTDOSE", "RT Dose"),
                Map.entry("RTIMAGE", "RT Image"),
                Map.entry("RTPLAN", "RT Plan"),
                Map.entry("RTRECORD", "RT Treatment Record"),
                Map.entry("RTSTRUCT", "RT Structure Set"),
                Map.entry("SEG", "Segmentation"),
                Map.entry("SM", "Slide Microscopy"),
                Map.entry("SMR", "Stereometric Relationship"),
                Map.entry("SR", "Structured Report"),
                Map.entry("STAIN", "Automated Slide Stainer"),
                Map.entry("TG", "Thermography"),
                Map.entry("US", "Ultrasound"),
                Map.entry("VA", "Visual Acuity"),
                Map.entry("XA", "X-Ray Angiography"),
                Map.entry("XC", "External-camera Photography")));

        // (0008,0008) Image Type
        addTagInfo(Tag.ImageType, Map.of(
                "ORIGINAL", "An image whose pixel values represent original observations",
                "DERIVED", "An image whose pixel values have been derived from one or more original images",
                "PRIMARY", "An image created as a direct result of the patient examination",
                "SECONDARY", "An image created after the initial patient examination",
                "AXIAL", "Cross-sectional image perpendicular to the long axis of the body",
                "SAGITTAL", "Cross-sectional image dividing the body into left and right",
                "CORONAL", "Cross-sectional image dividing the body into front and back"));

        // (0008,0005) Specific Character Set
        addTagInfo(Tag.SpecificCharacterSet, Map.ofEntries(
                Map.entry("ISO_IR 6", "Default (Basic G0 set)"),
                Map.entry("ISO_IR 100", "Latin alphabet No. 1 (Western Europe)"),
                Map.entry("ISO_IR 101", "Latin alphabet No. 2 (Central/Eastern Europe)"),
                Map.entry("ISO_IR 109", "Latin alphabet No. 3 (South Europe)"),
                Map.entry("ISO_IR 110", "Latin alphabet No. 4 (North Europe)"),
                Map.entry("ISO_IR 144", "Cyrillic"),
                Map.entry("ISO_IR 127", "Arabic"),
                Map.entry("ISO_IR 126", "Greek"),
                Map.entry("ISO_IR 138", "Hebrew"),
                Map.entry("ISO_IR 148", "Latin alphabet No. 5 (Turkish)"),
                Map.entry("ISO_IR 13", "Japanese (Katakana)"),
                Map.entry("ISO_IR 166", "Thai"),
                Map.entry("ISO_IR 192", "Unicode (UTF-8)"),
                Map.entry("GB18030", "Chinese (Simplified)"),
                Map.entry("GBK", "Chinese (Simplified, legacy)")));

        // (0028,0004) Photometric Interpretation
        addTagInfo(Tag.PhotometricInterpretation, Map.ofEntries(
                Map.entry("MONOCHROME1", "Greyscale: 0 is white"),
                Map.entry("MONOCHROME2", "Greyscale: 0 is black"),
                Map.entry("PALETTE COLOR", "Indexed color with lookup table"),
                Map.entry("RGB", "Red, Green, Blue color"),
                Map.entry("YBR_FULL", "Luminance Y, Chrominance Cb and Cr"),
                Map.entry("YBR_FULL_422", "Luminance Y, Chrominance Cb and Cr with 4:2:2 subsampling"),
                Map.entry("YBR_PARTIAL_422", "Luminance Y, Chrominance Cb and Cr with partial range 4:2:2"),
                Map.entry("YBR_RCT", "Reversible Color Transformation (Lossless)"),
                Map.entry("YBR_ICT", "Irreversible Color Transformation (Lossy)")));

        // (0008,0064) Conversion Type
        addTagInfo(Tag.ConversionType, Map.of(
                "DV", "Digitized Video",
                "DI", "Digital Interface",
                "DF", "Digitized Film",
                "WSD", "Workstation",
                "SD", "Scanned Document",
                "SI", "Scanned Image",
                "DRW", "Drawing",
                "TOCD", "Table of Contents Drawing"));

        // (0018,5100) Patient Position
        addTagInfo(Tag.PatientPosition, Map.of(
                "HFP", "Head First-Prone",
                "HFS", "Head First-Supine",
                "HFDR", "Head First-Decubitus Right",
                "HFDL", "Head First-Decubitus Left",
                "FFP", "Feet First-Prone",
                "FFS", "Feet First-Supine",
                "FFDR", "Feet First-Decubitus Right",
                "FFDL", "Feet First-Decubitus Left"));

        // (0018,0023) MR Acquisition Type
        addTagInfo(Tag.MRAcquisitionType, Map.of(
                "2D", "Two-dimensional acquisition",
                "3D", "Three-dimensional acquisition"));

        // (0018,0020) Scanning Sequence
        addTagInfo(Tag.ScanningSequence, Map.of(
                "SE", "Spin Echo",
                "IR", "Inversion Recovery",
                "GR", "Gradient Recalled",
                "EP", "Echo Planar",
                "RM", "Research Mode"));

        // (0040,1009) Reporting Priority
        addTagInfo(Tag.ReportingPriority, Map.of(
                "HIGH", "Highest priority",
                "ROUTINE", "Normal priority",
                "MEDIUM", "Medium priority",
                "LOW", "Low priority"));
    }

    private static void addTagInfo(int tag, Map<String, String> values) {
        TAG_VALUE_MAP.put(tag, values);
    }

    /**
     * Gets a human-readable explanation for a CS tag value.
     * Supports multi-valued strings separated by backslash.
     * 
     * @param tag   The DICOM tag
     * @param value The value of the tag
     * @return An explanation or null if not found
     */
    public static String getDescription(int tag, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Map<String, String> valueMap = TAG_VALUE_MAP.get(tag);
        if (valueMap == null) {
            // Also check for ModalitiesInStudy which uses same values as Modality
            if (tag == Tag.ModalitiesInStudy) {
                valueMap = TAG_VALUE_MAP.get(Tag.Modality);
            } else {
                return null;
            }
        }

        String[] parts = value.split("\\\\");
        StringBuilder sb = new StringBuilder();
        boolean foundAtLeastOne = false;

        for (String part : parts) {
            String trimmed = part.trim();
            String explanation = valueMap.get(trimmed);
            if (explanation != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(trimmed).append(": ").append(explanation);
                foundAtLeastOne = true;
            }
        }

        if (foundAtLeastOne) {
            return "CS_INFO:" + sb.toString();
        }

        return null;
    }
}
