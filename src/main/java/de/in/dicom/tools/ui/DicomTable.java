package de.in.dicom.tools.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.event.TableModelEvent;

import de.in.dicom.tools.Settings;

/**
 * Specialized JTable for displaying DICOM attributes with alternating row
 * colors and indentation for nested structures.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomTable extends JTable {

    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int DEFAULT_ROW_HEIGHT = 22;
    private int currentFontSize;

    public DicomTable(DicomTableModel model) {
        super(model);
        currentFontSize = Settings.getFontSize();
        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));

        // Initial width update
        updateColumnWidths();

        updateFontAndRowHeight();
        setupKeyBindings();

        getColumnModel().getColumn(0).setCellRenderer(new IndentedRenderer());

        // Column 3: Length - Right Align with 10px right margin
        DefaultTableCellRenderer lengthRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 10));
                return this;
            }
        };
        lengthRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
        getColumnModel().getColumn(3).setCellRenderer(lengthRenderer);

        // Column 4: Status - Icon Renderer
        getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                // Click on first column to expand/collapse
                if (row >= 0 && col == 0) {
                    DicomTableModel model = (DicomTableModel) getModel();
                    DicomTableModel.DicomNode node = model.getNodeAt(row);
                    if (node != null && node.isExpandable()) {
                        model.toggleExpansion(row);
                    }
                } else if (row >= 0 && col == 1) {
                    DicomTableModel model = (DicomTableModel) getModel();
                    DicomTableModel.DicomNode node = model.getNodeAt(row);
                    if (node != null && node.tag != -1) {
                        TagInfoProvider.showTagInfo(DicomTable.this, node.tag, e.getLocationOnScreen());
                    }
                } else if (row >= 0 && col == 2) {
                    Object val = getValueAt(row, col);
                    if (val != null && !val.toString().isEmpty()) {
                        VRInfoProvider.showVRInfo(DicomTable.this, val.toString(), e.getLocationOnScreen());
                    }
                } else if (row >= 0 && col == 4) {
                    DicomTableModel model = (DicomTableModel) getModel();
                    Object val = model.getValueAt(row, col);
                    if (val instanceof String && !((String) val).isEmpty()) {
                        StatusInfoProvider.showStatusInfo(DicomTable.this, (String) val, e.getLocationOnScreen());
                    }
                }
            }
        });
    }

    private void setupKeyBindings() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK),
                "zoomIn");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK),
                "zoomIn");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK),
                "zoomOut");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK),
                "zoomOut");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK),
                "resetZoom");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, KeyEvent.CTRL_DOWN_MASK),
                "resetZoom");

        getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        });
        getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        });
        getActionMap().put("resetZoom", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetZoom();
            }
        });
    }

    public void zoomIn() {
        currentFontSize++;
        updateFontAndRowHeight();
        Settings.setFontSize(currentFontSize);
    }

    public void zoomOut() {
        if (currentFontSize > 6) {
            currentFontSize--;
            updateFontAndRowHeight();
            Settings.setFontSize(currentFontSize);
        }
    }

    public void resetZoom() {
        currentFontSize = 12; // Assuming 12 is the default if not specified elsewhere. Settings.getFontSize()
                              // would return saved value.
        updateFontAndRowHeight();
        Settings.setFontSize(currentFontSize);
    }

    private void updateFontAndRowHeight() {
        Font font = getFont();
        if (font == null) {
            font = new Font(Font.SANS_SERIF, Font.PLAIN, currentFontSize);
        } else {
            font = font.deriveFont((float) currentFontSize);
        }
        setFont(font);
        getTableHeader().setFont(font);
        setRowHeight(DEFAULT_ROW_HEIGHT * currentFontSize / DEFAULT_FONT_SIZE);
        updateColumnWidths();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        updateColumnWidths();
    }

    private void updateColumnWidths() {
        if (getModel() == null || getFont() == null)
            return;

        java.awt.FontMetrics fm = getFontMetrics(getFont());
        int rowCount = getRowCount();
        DicomTableModel model = (DicomTableModel) getModel();

        // 0: Tag, 1: Name, 2: VR, 3: Length, 4: Status, 5: Content
        int[] maxPixelWidths = new int[6];
        // Initialize with header widths (heuristic)
        maxPixelWidths[0] = fm.stringWidth("Tag") + 25;
        maxPixelWidths[1] = fm.stringWidth("Name") + 25;
        maxPixelWidths[2] = fm.stringWidth("VR") + 15;
        maxPixelWidths[3] = fm.stringWidth("Length") + 25; // 15 + 10 margin
        maxPixelWidths[4] = 30; // Status column fixed width
        maxPixelWidths[5] = fm.stringWidth("Content") + 25;

        for (int r = 0; r < rowCount; r++) {
            DicomTableModel.DicomNode node = model.getNodeAt(r);
            if (node == null)
                continue;

            // Column 0: Tag - needs indentation
            String tagVal = (String) model.getValueAt(r, 0);
            int indent = Math.max(0, node.getDepth() - 1) * 16;
            // +5 from IndentedRenderer border, + maybe expansion symbol (~15), + 10 right
            // margin
            int tagWidth = fm.stringWidth(tagVal) + indent + 36;
            if (tagWidth > maxPixelWidths[0])
                maxPixelWidths[0] = tagWidth;

            // Column 1: Name
            String nameVal = (String) model.getValueAt(r, 1);
            int nameWidth = fm.stringWidth(nameVal) + 15;
            if (nameWidth > maxPixelWidths[1])
                maxPixelWidths[1] = nameWidth;

            // Column 2: VR
            String vrVal = (String) model.getValueAt(r, 2);
            int vrWidth = fm.stringWidth(vrVal) + 15;
            if (vrWidth > maxPixelWidths[2])
                maxPixelWidths[2] = vrWidth;

            // Column 3: Length - add 10px for the right border margin
            String lenVal = (String) model.getValueAt(r, 3);
            int lenWidth = fm.stringWidth(lenVal) + 25; // 15 + 10 margin
            if (lenWidth > maxPixelWidths[3])
                maxPixelWidths[3] = lenWidth;

            // Column 5: Content
            Object contentVal = model.getValueAt(r, 5);
            if (contentVal != null) {
                int contentWidth = fm.stringWidth(contentVal.toString()) + 25;
                if (contentWidth > maxPixelWidths[5])
                    maxPixelWidths[5] = contentWidth;
            }
        }

        // Apply widths
        for (int i = 0; i < 6; i++) {
            if (i < getColumnCount()) {
                TableColumn col = getColumnModel().getColumn(i);
                // Reset bounds first to avoid clamping issues
                col.setMinWidth(0);
                col.setMaxWidth(Integer.MAX_VALUE);

                if (i == 4) { // Status column is fixed
                    col.setPreferredWidth(maxPixelWidths[i]);
                    col.setMinWidth(maxPixelWidths[i]);
                    col.setMaxWidth(maxPixelWidths[i]);
                } else if (i == 5) { // Content column can grow
                    col.setPreferredWidth(maxPixelWidths[i]);
                    col.setMinWidth(100);
                } else {
                    col.setPreferredWidth(maxPixelWidths[i]);
                    col.setMinWidth(maxPixelWidths[i]);
                    col.setMaxWidth(maxPixelWidths[i]);
                }
            }
        }
    }

    @Override
    public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            java.awt.Color bg = getBackground();
            if (bg == null)
                bg = java.awt.Color.WHITE;

            DicomTableModel model = (DicomTableModel) getModel();
            DicomTableModel.DicomNode node = model.getNodeAt(row);

            // Check for File Meta Information (Group 0002)
            boolean isFMI = (node != null && (node.tag >>> 16) == 0x0002);

            // Slightly darken the base background for FMI or depth
            if (isFMI) {
                // Darken by ~10%
                bg = new java.awt.Color(
                        (int) (bg.getRed() * 0.9),
                        (int) (bg.getGreen() * 0.9),
                        (int) (bg.getBlue() * 0.9));
            } else if (node != null && node.getDepth() > 0) {
                // Subtle cumulative tint for nesting
                double factor = Math.pow(0.97, Math.min(5, node.getDepth()));
                bg = new java.awt.Color(
                        (int) (bg.getRed() * factor),
                        (int) (bg.getGreen() * factor),
                        (int) (bg.getBlue() * factor));
            }

            if (row % 2 == 1) {
                float[] hsb = java.awt.Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
                if (hsb[2] > 0.5f) {
                    c.setBackground(new java.awt.Color(Math.max(0, bg.getRed() - 12), Math.max(0, bg.getGreen() - 12),
                            Math.max(0, bg.getBlue() - 12)));
                } else {
                    c.setBackground(
                            new java.awt.Color(Math.min(255, bg.getRed() + 12), Math.min(255, bg.getGreen() + 12),
                                    Math.min(255, bg.getBlue() + 12)));
                }
            } else {
                c.setBackground(bg);
            }
        }
        return c;
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            setHorizontalAlignment(javax.swing.JLabel.CENTER);
            setToolTipText(null);
            setIcon(null);

            if (value instanceof String) {
                String status = (String) value;
                if (status.startsWith("VALIDATION_ALERT:")) {
                    setText("⚠");
                    setForeground(java.awt.Color.RED);
                } else if (status.startsWith("UID_INFO:") || status.startsWith("CS_INFO:")) {
                    setText("ℹ");
                    setForeground(new java.awt.Color(0, 120, 215)); // Professional blue
                } else {
                    setText("");
                }
            }
            return this;
        }
    }

    private class IndentedRenderer extends DefaultTableCellRenderer {
        private DicomTableModel.DicomNode currentNode;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            DicomTableModel model = (DicomTableModel) table.getModel();
            currentNode = model.getNodeAt(row);

            if (currentNode != null) {
                int indent = Math.max(0, currentNode.getDepth() - 1) * 16;
                // Add 10px right margin to match Length column
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, indent + 5, 0, 10));

                if (currentNode.isExpandable()) {
                    String prefix = currentNode.isExpanded() ? "▼ " : "► ";
                    setText(prefix + getText());
                } else {
                    // Subtle indent for non-expandable children
                    setText("  " + getText());
                }
                setHorizontalAlignment(javax.swing.JLabel.RIGHT);
            }
            return this;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);

            if (currentNode != null && currentNode.getDepth() > 0) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setColor(new java.awt.Color(128, 128, 128, 80)); // Subtle gray
                int indentStep = 16;
                int xOffset = 5 + 4; // Start near the triangle center

                // Draw vertical lines for each parent level
                for (int i = 0; i < currentNode.getDepth(); i++) {
                    int x = xOffset + i * indentStep;
                    g2.drawLine(x, 0, x, getHeight());
                }
                g2.dispose();
            }
        }
    }
}
