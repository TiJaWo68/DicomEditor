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
        return validate(tag, vr, null, value);
    }

    public static ValidationResult validate(int tag, VR vr, String vm, String value) {
        if (value == null)
            value = "";

        // 1. Tag-specific rules (often apply to the first or whole value)
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

        // 2. Split into components and validate each
        String[] parts;
        if (vr == VR.LT || vr == VR.ST || vr == VR.UT) {
            parts = new String[] { value };
        } else {
            parts = value.split("\\\\", -1);
        }

        // Check Multiplicity if provided
        if (vm != null && !vm.isEmpty() && !vm.equals("?")) {
            if (!isValidMultiplicity(parts.length, vm)) {
                return new ValidationResult(Severity.ERROR,
                        "Value Multiplicity (VM) mismatch. Expected " + vm + ", got " + parts.length + ".");
            }
        }

        for (String part : parts) {
            ValidationResult res = validateSingleValue(tag, vr, part);
            if (!res.isOk()) {
                if (parts.length > 1) {
                    return new ValidationResult(res.severity, "Error in component '" + part + "': " + res.message);
                }
                return res;
            }
        }

        return new ValidationResult(Severity.OK, "");
    }

    public static boolean isValidMultiplicity(int count, String vmSpec) {
        if (vmSpec == null || vmSpec.isEmpty() || vmSpec.equals("?"))
            return true;

        String[] ranges = vmSpec.split("-");
        if (ranges.length == 1) {
            String s = ranges[0].trim();
            if (s.endsWith("n")) { // e.g. "n", "2n"
                if (s.length() == 1)
                    return count >= 1;
                try {
                    int factor = Integer.parseInt(s.substring(0, s.length() - 1));
                    return count > 0 && count % factor == 0;
                } catch (NumberFormatException e) {
                    return true;
                }
            }
            try {
                return count == Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return true;
            }
        } else if (ranges.length == 2) {
            try {
                int min = Integer.parseInt(ranges[0].trim());
                String maxPart = ranges[1].trim();
                if (maxPart.equals("n")) {
                    return count >= min;
                }
                if (maxPart.endsWith("n")) {
                    int factor = Integer.parseInt(maxPart.substring(0, maxPart.length() - 1));
                    return count >= min && count % factor == 0;
                }
                int max = Integer.parseInt(maxPart);
                return count >= min && count <= max;
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return true;
    }

    /**
     * Internal helper to validate a single component of a multi-valued tag.
     */
    private static ValidationResult validateSingleValue(int tag, VR vr, String value) {
        if (value == null)
            value = "";

        if (value.isEmpty())
            return new ValidationResult(Severity.OK, "");

        switch (vr) {
            case AE: // Application Entity
                if (value.getBytes().length > 16)
                    return new ValidationResult(Severity.ERROR, "AE value too long (max 16 bytes).");
                if (containsControlChars(value) || value.contains("\\"))
                    return new ValidationResult(Severity.ERROR,
                            "AE contains invalid characters (backslash or control chars).");
                if (value.trim().isEmpty())
                    return new ValidationResult(Severity.ERROR, "AE cannot consist solely of spaces.");
                break;

            case AS: // Age String
                if (!Pattern.matches("\\d{3}[DWMY]", value)) {
                    return new ValidationResult(Severity.ERROR,
                            "Invalid Age format (AS). Must be nnnD, nnnW, nnnM, or nnnY.");
                }
                break;

            case AT: // Attribute Tag
                if (!Pattern.matches("[0-9A-Fa-f]{8}", value)) {
                    return new ValidationResult(Severity.ERROR,
                            "Invalid Attribute Tag format. Expected 8 hex digits (GGGGEEEE).");
                }
                break;

            case CS: // Code String
            {
                String[] parts = value.split("\\\\", -1);
                for (String part : parts) {
                    if (part.length() > 16)
                        return new ValidationResult(Severity.ERROR, "CS component too long (max 16 chars).");
                    if (!Pattern.matches("[A-Z0-9 _]*", part)) {
                        return new ValidationResult(Severity.ERROR,
                                "CS contains invalid characters (Uppercase, 0-9, space, underscore only).");
                    }
                }
                break;
            }

            case DA: // Date
                if (!Pattern.matches("\\d{8}", value)) {
                    return new ValidationResult(Severity.ERROR, "Invalid Date format (DA). Use YYYYMMDD.");
                }
                // Basic date check (could be improved with date parsing)
                int month = Integer.parseInt(value.substring(4, 6));
                int day = Integer.parseInt(value.substring(6, 8));
                if (month < 1 || month > 12 || day < 1 || day > 31) {
                    return new ValidationResult(Severity.ERROR, "Invalid logic for Date (DA). Check month/day.");
                }
                break;

            case DS: // Decimal String
                if (value.length() > 16)
                    return new ValidationResult(Severity.ERROR, "DS value too long (max 16 chars).");
                // Leading/trailing spaces allowed, embedded not.
                if (value.trim().contains(" "))
                    return new ValidationResult(Severity.ERROR, "DS cannot contain embedded spaces.");
                if (!Pattern.matches("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([Ee][+-]?\\d+)?", value.trim())) {
                    return new ValidationResult(Severity.ERROR, "Invalid Decimal String (DS) format.");
                }
                break;

            case DT: // Date Time
            {
                // YYYYMMDDHHMMSS.FFFFFF&ZZXX
                if (!Pattern.matches("\\d{4,14}(\\.\\d{1,6})?([+-]\\d{4})?", value.trim())) {
                    return new ValidationResult(Severity.ERROR, "Invalid Date Time format (DT).");
                }
                break;
            }

            case FL: // Float
            case FD: // Double
                try {
                    Double.parseDouble(value.trim());
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid floating point value.");
                }
                break;

            case IS: // Integer String
                if (value.length() > 12)
                    return new ValidationResult(Severity.ERROR, "IS value too long (max 12 chars).");
                try {
                    long val = Long.parseLong(value.trim());
                    if (val < -2147483648L || val > 2147483647L) {
                        return new ValidationResult(Severity.ERROR, "IS value out of range (-2^31 to 2^31-1).");
                    }
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Integer String (IS).");
                }
                break;

            case LO: // Long String
                if (value.length() > 64)
                    return new ValidationResult(Severity.ERROR, "LO value too long (max 64 chars).");
                if (containsControlCharsExcludingEsc(value) || value.contains("\\"))
                    return new ValidationResult(Severity.ERROR,
                            "LO contains invalid characters (backslash or control chars).");
                break;

            case LT: // Long Text
                if (value.length() > 10240)
                    return new ValidationResult(Severity.ERROR, "LT value too long (max 10240 chars).");
                // \ is allowed in LT
                if (containsControlCharsExcludingTextControls(value))
                    return new ValidationResult(Severity.ERROR, "LT contains invalid control characters.");
                break;

            case PN: // Person Name
                if (value.length() > 64) // 64 per component group
                    return new ValidationResult(Severity.WARNING,
                            "PN value very long. Ensure groups don't exceed 64 chars.");
                if (containsControlCharsExcludingEsc(value) || value.contains("\\"))
                    return new ValidationResult(Severity.ERROR,
                            "PN contains invalid characters (backslash or control chars).");
                break;

            case SH: // Short String
                if (value.length() > 16)
                    return new ValidationResult(Severity.ERROR, "SH value too long (max 16 chars).");
                if (containsControlCharsExcludingEsc(value) || value.contains("\\"))
                    return new ValidationResult(Severity.ERROR,
                            "SH contains invalid characters (backslash or control chars).");
                break;

            case SL: // Signed Long
                try {
                    Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Signed Long (32-bit).");
                }
                break;

            case SS: // Signed Short
                try {
                    Short.parseShort(value.trim());
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Signed Short (16-bit).");
                }
                break;

            case SV: // Signed Very Long (64-bit)
                try {
                    Long.parseLong(value.trim());
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Signed Very Long (64-bit).");
                }
                break;

            case ST: // Short Text
                if (value.length() > 1024)
                    return new ValidationResult(Severity.ERROR, "ST value too long (max 1024 chars).");
                if (containsControlCharsExcludingTextControls(value))
                    return new ValidationResult(Severity.ERROR, "ST contains invalid control characters.");
                break;

            case TM: // Time
                if (!Pattern.matches("\\d{2}(\\d{2}(\\d{2}(\\.\\d{1,6})?)?)?", value.trim())) {
                    return new ValidationResult(Severity.ERROR, "Invalid Time format (TM). Use HHMMSS.FFFFFF.");
                }
                String t = value.trim();
                if (t.length() >= 2) {
                    int hh = Integer.parseInt(t.substring(0, 2));
                    if (hh < 0 || hh > 23)
                        return new ValidationResult(Severity.ERROR, "Invalid hour in TM.");
                }
                if (t.length() >= 4) {
                    int mm = Integer.parseInt(t.substring(2, 4));
                    if (mm < 0 || mm > 59)
                        return new ValidationResult(Severity.ERROR, "Invalid minute in TM.");
                }
                if (t.length() >= 6) {
                    int ss = Integer.parseInt(t.substring(4, 6));
                    if (ss < 0 || ss > 60)
                        return new ValidationResult(Severity.ERROR, "Invalid second in TM.");
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

            case UL: // Unsigned Long
                try {
                    long val = Long.parseLong(value.trim());
                    if (val < 0 || val > 4294967295L) {
                        return new ValidationResult(Severity.ERROR, "UL value out of range (0 to 2^32-1).");
                    }
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Unsigned Long.");
                }
                break;

            case UR: // URI
                if (value.contains("\\"))
                    return new ValidationResult(Severity.ERROR, "UR cannot contain backslash.");
                break;

            case US: // Unsigned Short
                try {
                    int val = Integer.parseInt(value.trim());
                    if (val < 0 || val > 65535) {
                        return new ValidationResult(Severity.ERROR, "US value out of range (0 to 65535).");
                    }
                } catch (NumberFormatException e) {
                    return new ValidationResult(Severity.ERROR, "Invalid Unsigned Short.");
                }
                break;

            case UV: // Unsigned 64-bit Long
                try {
                    // Java doesn't have unsigned long, use BigInteger if needed, but for validation
                    // we can check format
                    if (!Pattern.matches("\\d+", value.trim()))
                        return new ValidationResult(Severity.ERROR, "Invalid Unsigned 64-bit Long.");
                    // Check range would require more work, but usually long.parse is enough for
                    // positive
                    long val = Long.parseLong(value.trim()); // This only goes up to 2^63-1
                    if (val < 0)
                        return new ValidationResult(Severity.ERROR, "UV cannot be negative.");
                } catch (NumberFormatException e) {
                    // Try BigInteger for values > 2^63-1
                    try {
                        new java.math.BigInteger(value.trim());
                    } catch (Exception ex) {
                        return new ValidationResult(Severity.ERROR, "Invalid Unsigned 64-bit Long format.");
                    }
                }
                break;

            default:
                break;
        }

        // Default: OK
        return new ValidationResult(Severity.OK, "");
    }

    private static boolean containsControlChars(String s) {
        for (char c : s.toCharArray()) {
            if (c < 32 || c == 127)
                return true;
        }
        return false;
    }

    private static boolean containsControlCharsExcludingEsc(String s) {
        for (char c : s.toCharArray()) {
            if ((c < 32 && c != 27) || c == 127)
                return true;
        }
        return false;
    }

    private static boolean containsControlCharsExcludingTextControls(String s) {
        for (char c : s.toCharArray()) {
            // TAB(9), LF(10), FF(12), CR(13), ESC(27)
            if (c < 32 && c != 9 && c != 10 && c != 12 && c != 13 && c != 27)
                return true;
            if (c == 127)
                return true;
        }
        return false;
    }
}
