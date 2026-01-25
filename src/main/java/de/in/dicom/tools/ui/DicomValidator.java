package de.in.dicom.tools.ui;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import java.util.regex.Pattern;

/**
 * Utility class for validating DICOM tag values based on their Value
 * Representation (VR) and specific tag rules.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomValidator {

    public enum Severity {
        OK, INFO, WARNING, ERROR
    }

    public static class ValidationResult {
        public final Severity severity;
        public final String message;

        public ValidationResult(Severity severity, String message) {
            this.severity = severity;
            this.message = message;
        }

        public boolean isOk() {
            return severity == Severity.OK;
        }

        public boolean isError() {
            return severity == Severity.ERROR;
        }

        public boolean isWarning() {
            return severity == Severity.WARNING;
        }
    }

    public static ValidationResult validate(int tag, VR vr, String value) {
        if (value == null)
            value = "";

        // 1. Tag-specific rules
        if (tag == Tag.PatientSex) {
            String v = value.trim().toUpperCase();
            if (!v.isEmpty() && !v.equals("M") && !v.equals("F") && !v.equals("O")) {
                return new ValidationResult(Severity.ERROR, "PatientSex (0010,0040) must be 'M', 'F', or 'O'.");
            }
        }

        if (tag == Tag.PatientName) {
            // PN: Max 5 components (4 carets)
            int caretCount = 0;
            for (char c : value.toCharArray()) {
                if (c == '^')
                    caretCount++;
            }
            if (caretCount > 4) {
                return new ValidationResult(Severity.WARNING,
                        "PatientName (0010,0010) usually has max 5 components (4 '^'). Proceed with '" + value + "'?");
            }
        }

        // 2. VR-specific rules (Comprehensive)
        if (value.isEmpty())
            return new ValidationResult(Severity.OK, "");

        switch (vr) {
            case AE: // Application Entity
                if (value.length() > 16)
                    return new ValidationResult(Severity.ERROR, "AE value too long (max 16 chars).");
                break;
            case AS: // Age String
                if (!Pattern.matches("\\d{3}[DWMY]", value)) {
                    return new ValidationResult(Severity.ERROR, "Invalid Age format (AS). Use 001Y, 002M, etc.");
                }
                break;
            case CS: // Code String
                if (value.length() > 16)
                    return new ValidationResult(Severity.ERROR, "CS value too long (max 16 chars).");
                if (!Pattern.matches("[A-Z0-9 _]+", value)) {
                    return new ValidationResult(Severity.ERROR,
                            "CS value contains invalid characters (Uppercase, numbers, space, underscore only).");
                }
                break;
            case DA: // Date
                if (!Pattern.matches("\\d{8}", value)) {
                    return new ValidationResult(Severity.ERROR, "Invalid Date format (DA). Use YYYYMMDD.");
                }
                break;
            case DS: // Decimal String
                if (value.length() > 16)
                    return new ValidationResult(Severity.ERROR, "DS value too long (max 16 chars).");
                try {
                    Double.parseDouble(value.trim());
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Decimal String (DS).");
                }
                break;
            case IS: // Integer String
                if (value.length() > 12)
                    return new ValidationResult(Severity.ERROR, "IS value too long (max 12 chars).");
                try {
                    Long.parseLong(value.trim());
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Integer String (IS).");
                }
                break;
            case LO: // Long String
                if (value.length() > 64)
                    return new ValidationResult(Severity.ERROR, "LO value too long (max 64 chars).");
                break;
            case SH: // Short String
                if (value.length() > 16)
                    return new ValidationResult(Severity.ERROR, "SH value too long (max 16 chars).");
                break;
            case TM: // Time
                if (!Pattern.matches("\\d{2}(\\d{2}(\\d{2}(\\.\\d{1,6})?)?)?", value)) {
                    return new ValidationResult(Severity.ERROR, "Invalid Time format (TM). Use HHMMSS.FFFFFF.");
                }
                break;
            case UI: // Unique Identifier
                if (value.length() > 64)
                    return new ValidationResult(Severity.ERROR, "UI value too long (max 64 chars).");
                if (!Pattern.matches("[0-9.]+", value)) {
                    return new ValidationResult(Severity.ERROR,
                            "Invalid UID format (UI). Only digits and dots allowed.");
                }
                break;
            default:
                break;
        }

        // Default: OK
        return new ValidationResult(Severity.OK, "");
    }
}
