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

/**
 * Specialized JTable for displaying DICOM attributes with alternating row
 * colors and indentation for nested structures.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomTable extends JTable {

    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int DEFAULT_ROW_HEIGHT = 22;
    private int currentFontSize = DEFAULT_FONT_SIZE;

    public DicomTable(DicomTableModel model) {
        super(model);
        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));

        updateFontAndRowHeight();
        setupKeyBindings();

        getColumnModel().getColumn(0).setCellRenderer(new IndentedRenderer());

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
    }

    public void zoomOut() {
        if (currentFontSize > 6) {
            currentFontSize--;
            updateFontAndRowHeight();
        }
    }

    public void resetZoom() {
        currentFontSize = DEFAULT_FONT_SIZE;
        updateFontAndRowHeight();
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
    }

    @Override
    public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            java.awt.Color bg = getBackground();
            if (bg == null)
                bg = java.awt.Color.WHITE;

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

    private class IndentedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            DicomTableModel model = (DicomTableModel) table.getModel();
            DicomTableModel.DicomNode node = model.getNodeAt(row);

            if (node != null) {
                int indent = Math.max(0, node.getDepth() - 1) * 20;
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, indent + 5, 0, 0));

                if (isSelected) {
                    setForeground(table.getSelectionForeground());
                } else {
                    setForeground(table.getForeground());
                }

                if (node.isExpandable()) {
                    String prefix = node.isExpanded() ? "▼ " : "► ";
                    setText(prefix + getText());
                } else {
                    // Subtle indent for non-expandable children
                    setText("  " + getText());
                }
            }
            return this;
        }
    }
}
