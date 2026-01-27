package de.in.dicom.tools.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Provides explanation for DICOM Value Representations (VR).
 * 
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class VRInfoProvider {

        private static final Map<String, String> VR_DATA = new HashMap<>();
        private static final String SOURCE_URL = "https://dicom.nema.org/medical/dicom/current/output/chtml/part05/sect_6.2.html";

        static {
                addVR("AE", "Application Entity",
                                "A string of characters that identifies an Application Entity with leading and trailing spaces being non-significant. A Value consisting solely of spaces shall not be used.",
                                "Default Character Repertoire excluding character code 5CH (the BACKSLASH \"\\\"), and all control characters.",
                                "16 bytes maximum");
                addVR("AS", "Age String",
                                "A string of characters with one of the following formats -- nnnD, nnnW, nnnM, nnnY; where nnn shall contain the number of days for D, weeks for W, months for M, or years for Y.",
                                "\"0\"-\"9\", \"D\", \"W\", \"M\", \"Y\" of Default Character Repertoire",
                                "4 bytes fixed");
                addVR("AT", "Attribute Tag",
                                "Ordered pair of 16-bit unsigned integers that is the Value of a Data Element Tag.",
                                "not applicable",
                                "4 bytes fixed");
                addVR("CS", "Code String",
                                "A string of characters identifying a controlled concept.",
                                "Uppercase characters, \"0\"-\"9\", the SPACE character, and underscore \"_\", of the Default Character Repertoire",
                                "16 bytes maximum");
                addVR("DA", "Date",
                                "A string of characters of the format YYYYMMDD; where YYYY shall contain year, MM shall contain the month, and DD shall contain the day.",
                                "\"0\"-\"9\" of Default Character Repertoire",
                                "8 bytes fixed");
                addVR("DS", "Decimal String",
                                "A string of characters representing either a fixed point number or a floating point number.",
                                "\"0\"-\"9\", \"+\", \"-\", \"E\", \"e\", \".\" and the SPACE character of Default Character Repertoire",
                                "16 bytes maximum");
                addVR("DT", "Date Time",
                                "A concatenated date-time character string in the format: YYYYMMDDHHMMSS.FFFFFF&ZZXX",
                                "\"0\"-\"9\", \"+\", \"-\", \".\" and the SPACE character of Default Character Repertoire",
                                "26 bytes maximum");
                addVR("FL", "Floating Point Single",
                                "Single precision binary floating point value represented in IEEE 754 binary32 format.",
                                "not applicable",
                                "4 bytes fixed");
                addVR("FD", "Floating Point Double",
                                "Double precision binary floating point value represented in IEEE 754 binary64 format.",
                                "not applicable",
                                "8 bytes fixed");
                addVR("IS", "Integer String",
                                "A string of characters representing an Integer in base-10 (decimal). Range: -2^31 <= n <= (2^31-1).",
                                "\"0\"-\"9\", \"+\", \"-\" and the SPACE character of Default Character Repertoire",
                                "12 bytes maximum");
                addVR("LO", "Long String",
                                "A character string that may be padded with leading and/or trailing spaces.",
                                "Default Character Repertoire excluding character code 5CH (the BACKSLASH \"\\\"), and all Control Characters except ESC.",
                                "64 chars maximum");
                addVR("LT", "Long Text",
                                "A character string that may contain one or more paragraphs.",
                                "Default Character Repertoire excluding Control Characters except TAB, LF, FF, CR (and ESC).",
                                "10240 chars maximum");
                addVR("OB", "Other Byte",
                                "An octet-stream where the encoding of the contents is specified by the negotiated Transfer Syntax.",
                                "not applicable",
                                "2^32-2 bytes maximum (depending on VL)");
                addVR("OD", "Other Double",
                                "A stream of IEEE 754 binary64 values.",
                                "not applicable",
                                "2^32-8 bytes maximum");
                addVR("OF", "Other Float",
                                "A stream of IEEE 754 binary32 values.",
                                "not applicable",
                                "2^32-4 bytes maximum");
                addVR("OL", "Other Long",
                                "A stream of 32-bit words.",
                                "not applicable",
                                "2^32-4 bytes maximum");
                addVR("OV", "Other 64-bit Very Long",
                                "A stream of 64-bit words.",
                                "not applicable",
                                "2^32-8 bytes maximum");
                addVR("OW", "Other Word",
                                "A stream of 16-bit words.",
                                "not applicable",
                                "2^32-2 bytes maximum");
                addVR("PN", "Person Name",
                                "A character string encoded using a 5 component convention: family^given^middle^prefix^suffix.",
                                "Default Character Repertoire excluding character code 5CH (the BACKSLASH \"\\\") and all Control Characters except ESC.",
                                "64 chars maximum per component group");
                addVR("SH", "Short String",
                                "A character string that may be padded with leading and/or trailing spaces.",
                                "Default Character Repertoire excluding character code 5CH (the BACKSLASH \"\\\") and all Control Characters except ESC.",
                                "16 chars maximum");
                addVR("SL", "Signed Long",
                                "Signed binary integer 32 bits long in 2's complement form.",
                                "not applicable",
                                "4 bytes fixed");
                addVR("SQ", "Sequence of Items",
                                "Value is a Sequence of zero or more Items.",
                                "not applicable",
                                "Undefined length or 2^32-2 bytes maximum");
                addVR("SS", "Signed Short",
                                "Signed binary integer 16 bits long in 2's complement form.",
                                "not applicable",
                                "2 bytes fixed");
                addVR("ST", "Short Text",
                                "A character string that may contain one or more paragraphs.",
                                "Default Character Repertoire excluding Control Characters except TAB, LF, FF, CR (and ESC).",
                                "1024 chars maximum");
                addVR("SV", "Signed 64-bit Very Long",
                                "Signed binary integer 64 bits long.",
                                "not applicable",
                                "8 bytes fixed");
                addVR("TM", "Time",
                                "A string of characters of the format HHMMSS.FFFFFF; where HH=00-23, MM=00-59, SS=00-60.",
                                "\"0\"-\"9\", \".\" and the SPACE character of Default Character Repertoire",
                                "14 bytes maximum");
                addVR("UC", "Unlimited Characters",
                                "A character string that may be of unlimited length.",
                                "Default Character Repertoire excluding character code 5CH (the BACKSLASH \"\\\"), and all Control Characters except ESC.",
                                "2^32-2 bytes maximum");
                addVR("UI", "Unique Identifier (UID)",
                                "A character string containing a UID (series of numeric components separated by dots).",
                                "\"0\"-\"9\", \".\" of Default Character Repertoire",
                                "64 bytes maximum");
                addVR("UL", "Unsigned Long",
                                "Unsigned binary integer 32 bits long.",
                                "not applicable",
                                "4 bytes fixed");
                addVR("UN", "Unknown",
                                "An octet-stream where the encoding of the contents is unknown.",
                                "not applicable",
                                "Any valid DICOM length");
                addVR("UR", "Universal Resource Identifier or Locator (URI/URL)",
                                "A string of characters that identifies a URI or a URL as defined in RFC3986.",
                                "Permitted set defined in IETF RFC3986 Section 2.",
                                "2^32-2 bytes maximum");
                addVR("US", "Unsigned Short",
                                "Unsigned binary integer 16 bits long.",
                                "not applicable",
                                "2 bytes fixed");
                addVR("UT", "Unlimited Text",
                                "A character string that may contain one or more paragraphs.",
                                "Default Character Repertoire excluding Control Characters except TAB, LF, FF, CR (and ESC).",
                                "2^32-2 bytes maximum");
                addVR("UV", "Unsigned 64-bit Very Long",
                                "Unsigned binary integer 64 bits long.",
                                "not applicable",
                                "8 bytes fixed");
        }

        private static void addVR(String vr, String name, String definition, String repertoire, String length) {
                StringBuilder html = new StringBuilder();
                html.append("<html><body style='font-family: sans-serif; padding: 10px; width: 300px;'>");
                html.append("<h3>VR: ").append(vr).append(" (").append(name).append(")</h3>");
                html.append("<p><b>Definition:</b><br/>").append(definition).append("</p>");
                html.append("<p><b>Character Repertoire:</b><br/>").append(repertoire).append("</p>");
                html.append("<p><b>Length:</b><br/>").append(length).append("</p>");
                html.append("<hr/>");
                html.append("<p><small>Source: <a href='").append(SOURCE_URL)
                                .append("'>DICOM Standard Part 5</a></small></p>");
                html.append("</body></html>");
                VR_DATA.put(vr, html.toString());
        }

        public static String getHtmlInfo(String vrName) {
                return VR_DATA.get(vrName);
        }

        public static void showVRInfo(Component parent, String vrName, Point location) {
                String info = getHtmlInfo(vrName);
                if (info == null) {
                        return;
                }

                JEditorPane editorPane = new JEditorPane("text/html", info);
                editorPane.setEditable(false);
                editorPane.setOpaque(false);
                editorPane.addHyperlinkListener(e -> {
                        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                                try {
                                        Desktop.getDesktop().browse(e.getURL().toURI());
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                }
                        }
                });

                JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "VR: " + vrName,
                                JDialog.ModalityType.APPLICATION_MODAL);
                dialog.setLayout(new java.awt.BorderLayout());
                dialog.add(new JScrollPane(editorPane), java.awt.BorderLayout.CENTER);

                // Add ESC key binding to close the dialog
                dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
                dialog.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                dialog.dispose();
                        }
                });

                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> dialog.dispose());
                JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
                buttonPanel.add(closeButton);
                dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

                dialog.pack();
                if (location != null) {
                        dialog.setLocation(location);
                } else {
                        dialog.setLocationRelativeTo(parent);
                }
                dialog.setVisible(true);
        }
}
