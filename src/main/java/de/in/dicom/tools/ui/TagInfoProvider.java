package de.in.dicom.tools.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;

/**
 * Provides information about DICOM Tags from the official standard (PS 3.6).
 * 
 * @author Antigravity
 */
public class TagInfoProvider {

    public record TagData(int tag, String name, String keyword, String vr, String vm, String notes) {
        public String getTagString() {
            return String.format("(%04X,%04X)", (tag >> 16) & 0xFFFF, tag & 0xFFFF);
        }
    }

    private static final Map<Integer, TagData> TAG_CACHE = new HashMap<>();
    private static boolean initialized = false;

    private synchronized static void initialize() {
        if (initialized)
            return;
        try (InputStream is = TagInfoProvider.class.getResourceAsStream("/part06.xml")) {
            if (is == null) {
                System.err.println("part06.xml not found in resources");
                return;
            }

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                private boolean inTBody = false;
                private boolean inRow = false;
                private boolean inCell = false;
                private int cellIndex = -1;
                private StringBuilder content = new StringBuilder();

                private String currentTag;
                private String currentName;
                private String currentKeyword;
                private String currentVR;
                private String currentVM;
                private String currentNotes;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if ("tbody".equals(qName)) {
                        inTBody = true;
                    } else if (inTBody && "tr".equals(qName)) {
                        inRow = true;
                        cellIndex = -1;
                    } else if (inRow && "td".equals(qName)) {
                        inCell = true;
                        cellIndex++;
                        content.setLength(0);
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    if (inCell) {
                        content.append(ch, start, length);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    if ("tbody".equals(qName)) {
                        inTBody = false;
                    } else if ("tr".equals(qName)) {
                        if (inRow) {
                            processRow();
                            inRow = false;
                        }
                    } else if ("td".equals(qName)) {
                        if (inCell) {
                            String value = content.toString().trim().replace("\u200B", ""); // Remove zero-width space
                            switch (cellIndex) {
                                case 0 -> currentTag = value;
                                case 1 -> currentName = value;
                                case 2 -> currentKeyword = value;
                                case 3 -> currentVR = value;
                                case 4 -> currentVM = value;
                                case 5 -> currentNotes = value;
                            }
                            inCell = false;
                        }
                    }
                }

                private void processRow() {
                    if (currentTag == null)
                        return;

                    try {
                        // Clean up the tag string (might contain italics, spaces, etc.)
                        String cleanTag = currentTag.replaceAll("\\s+", "");
                        // Pattern for (GGGG,EEEE)
                        Pattern p = Pattern.compile("\\(([0-9A-Fa-f]{4}),([0-9A-Fa-f]{4})\\)");
                        Matcher m = p.matcher(cleanTag);
                        if (m.find()) {
                            int group = Integer.parseInt(m.group(1), 16);
                            int element = Integer.parseInt(m.group(2), 16);
                            int tag = (group << 16) | element;

                            TAG_CACHE.putIfAbsent(tag,
                                    new TagData(tag, currentName, currentKeyword, currentVR, currentVM, currentNotes));
                        }
                    } catch (Exception e) {
                        // Skip unparseable tags
                    }

                    // Reset
                    currentTag = null;
                    currentName = null;
                    currentKeyword = null;
                    currentVR = null;
                    currentVM = null;
                    currentNotes = null;
                }
            };

            saxParser.parse(is, handler);
            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TagData getTagData(int tag) {
        if (!initialized) {
            initialize();
        }
        return TAG_CACHE.get(tag);
    }

    public static void showTagInfo(Component parent, int tag, Point location) {
        TagData data = getTagData(tag);
        if (data == null) {
            JOptionPane.showMessageDialog(parent,
                    "No detailed information found for tag "
                            + String.format("(%04X,%04X)", (tag >> 16) & 0xFFFF, tag & 0xFFFF));
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: sans-serif; padding: 10px; width: 400px;'>");
        html.append("<h2>Tag: ").append(data.getTagString()).append("</h2>");
        html.append("<p><b>Name:</b><br/>").append(data.name()).append("</p>");
        html.append("<p><b>Keyword:</b><br/>").append(data.keyword()).append("</p>");
        html.append("<p><b>Value Representation (VR):</b><br/>").append(data.vr()).append("</p>");
        html.append("<p><b>Value Multiplicity (VM):</b><br/>").append(data.vm()).append("</p>");
        if (data.notes() != null && !data.notes().isEmpty()) {
            html.append("<p><b>Notes:</b><br/>").append(data.notes()).append("</p>");
        }
        html.append("<hr/>");
        html.append(
                "<p><small>Source: <a href='https://dicom.nema.org/medical/dicom/current/output/html/part06.html'>DICOM Standard PS 3.6 (Data Dictionary)</a></small></p>");
        html.append("</body></html>");

        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
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

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Tag Information",
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
