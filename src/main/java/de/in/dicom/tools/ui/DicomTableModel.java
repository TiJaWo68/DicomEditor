package de.in.dicom.tools.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;

import org.dcm4che3.util.TagUtils;

/**
 * Table model that represents DICOM attributes in a hierarchical structure,
 * supporting expansion of sequences and items.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class DicomTableModel extends AbstractTableModel {

    private final Attributes fmi;
    private final Attributes attributes;
    private final List<DicomNode> visibleNodes = new ArrayList<>();

    private DicomNode rootNode;

    private static final String[] COLUMN_NAMES = { "Tag", "VR", "Length", "Name", "Content" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, String.class, String.class, String.class,
            Object.class };

    public DicomTableModel(Attributes fmi, Attributes attributes) {
        this.fmi = fmi;
        this.attributes = attributes;
        initData();
    }

    private void initData() {
        rootNode = new DicomNode(null, null, -1, 0, false);
        rootNode.expanded = true;
        loadChildren(rootNode, fmi);
        loadChildren(rootNode, attributes);
        rebuildVisibleNodes();
    }

    private void loadChildren(DicomNode parent, Attributes attrs) {
        if (attrs == null)
            return;
        int[] tags = attrs.tags();
        Arrays.sort(tags);
        for (int tag : tags) {
            DicomNode node = new DicomNode(parent, attrs, tag, parent.depth + 1, false);
            parent.children.add(node);
        }
    }

    private void rebuildVisibleNodes() {
        visibleNodes.clear();
        addVisibleChildren(rootNode);
        fireTableDataChanged();
    }

    private void addVisibleChildren(DicomNode node) {
        for (DicomNode child : node.children) {
            visibleNodes.add(child);
            if (child.expanded) {
                addVisibleChildren(child);
            }
        }
    }

    public void toggleExpansion(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= visibleNodes.size())
            return;
        DicomNode node = visibleNodes.get(rowIndex);
        if (node.isExpandable()) {
            node.expanded = !node.expanded;
            if (node.expanded && node.children.isEmpty()) {
                if (node.vr == VR.SQ) {
                    Sequence seq = node.attributes.getSequence(node.tag);
                    if (seq != null) {
                        for (int i = 0; i < seq.size(); i++) {
                            DicomNode itemNode = new DicomNode(node, seq.get(i), -1, node.depth + 1, true);
                            itemNode.itemIndex = i;
                            node.children.add(itemNode);
                            loadChildren(itemNode, seq.get(i));
                        }
                    }
                }
            }
            rebuildVisibleNodes();
        }
    }

    public DicomNode getNodeAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= visibleNodes.size())
            return null;
        return visibleNodes.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return visibleNodes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DicomNode node = visibleNodes.get(rowIndex);
        switch (columnIndex) {
            case 0:
                if (node.tag == -1)
                    return "Item #" + (node.itemIndex + 1);
                return TagUtils.toString(node.tag).replaceAll("[()]", "");
            case 1:
                return node.vr != null ? node.vr.toString() : "";
            case 2:
                if (node.tag == -1)
                    return "";
                try {
                    if (node.vr == VR.SQ) {
                        return "";
                    }
                    byte[] b = node.attributes.getBytes(node.tag);
                    return b != null ? String.valueOf(b.length) : "";
                } catch (Exception e) {
                    return "";
                }
            case 3:
                if (node.tag == -1)
                    return "";
                return ElementDictionary.keywordOf(node.tag, null);
            case 4:
                if (node.isExpandable())
                    return "";
                return node.formatValue();
            default:
                return "";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 4) {
            DicomNode node = visibleNodes.get(rowIndex);
            return !node.isExpandable() && node.tag != -1 && !node.isHexDisplayed();
        }
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 4) {
            DicomNode node = visibleNodes.get(rowIndex);
            String strValue = (aValue != null) ? aValue.toString() : "";

            DicomValidator.ValidationResult result = DicomValidator.validate(node.tag, node.vr, strValue);

            if (result.isError()) {
                javax.swing.JOptionPane.showMessageDialog(null, result.message, "Validation Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return; // Block update
            } else if (result.isWarning()) {
                int choice = javax.swing.JOptionPane.showConfirmDialog(null, result.message, "Validation Warning",
                        javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
                if (choice != javax.swing.JOptionPane.YES_OPTION) {
                    return; // User cancelled update
                }
            }

            node.setValue(aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    // --- Editing ---

    public void addAttribute(int parentRowIndex, int tag, VR vr) {
        DicomNode parentNode = (parentRowIndex >= 0) ? visibleNodes.get(parentRowIndex) : rootNode;

        if (!parentNode.isExpandable() && parentNode != rootNode) {
            parentNode = parentNode.parent;
        }

        Attributes targetAttrs = null;
        if (parentNode == rootNode) {
            targetAttrs = attributes;
        } else if (parentNode.isItem) {
            targetAttrs = parentNode.attributes;
        } else if (parentNode.vr == VR.SQ) {
            parentNode = parentNode.parent;
            if (parentNode == rootNode)
                targetAttrs = attributes;
            else
                targetAttrs = parentNode.attributes;
        }

        if (targetAttrs != null) {
            if (vr == VR.SQ) {
                targetAttrs.newSequence(tag, 0);
            } else {
                targetAttrs.setString(tag, vr, "");
            }
            parentNode.children.clear();
            if (parentNode == rootNode) {
                loadChildren(rootNode, fmi);
                loadChildren(rootNode, attributes);
            } else {
                loadChildren(parentNode, targetAttrs);
            }
            rebuildVisibleNodes();
        }
    }

    public void removeRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= visibleNodes.size())
            return;
        DicomNode node = visibleNodes.get(rowIndex);
        if (node == rootNode)
            return;

        DicomNode parent = node.parent;
        if (parent != null) {
            if (node.isItem) {
                Sequence seq = parent.attributes.getSequence(parent.tag);
                if (seq != null && node.itemIndex >= 0 && node.itemIndex < seq.size()) {
                    seq.remove(node.itemIndex);
                }
            } else {
                node.attributes.remove(node.tag);
            }

            parent.children.clear();
            if (parent == rootNode) {
                loadChildren(rootNode, fmi);
                loadChildren(rootNode, attributes);
            } else if (parent.isItem) {
                loadChildren(parent, parent.attributes);
            } else if (parent.vr == VR.SQ) {
                Sequence seq = parent.attributes.getSequence(parent.tag);
                if (seq != null) {
                    for (int i = 0; i < seq.size(); i++) {
                        DicomNode itemNode = new DicomNode(parent, seq.get(i), -1, parent.depth + 1, true);
                        itemNode.itemIndex = i;
                        parent.children.add(itemNode);
                    }
                }
            }
            rebuildVisibleNodes();
        }
    }

    public class DicomNode {
        final DicomNode parent;
        final Attributes attributes;
        final int tag;
        final VR vr;
        final int depth;
        final List<DicomNode> children = new ArrayList<>();
        boolean expanded;
        int itemIndex = -1;
        boolean isItem;

        public DicomNode(DicomNode parent, Attributes attributes, int tag, int depth, boolean isItem) {
            this.parent = parent;
            this.attributes = attributes;
            this.tag = tag;
            this.depth = depth;
            this.isItem = isItem;
            if (attributes != null && !isItem && tag != -1) {
                this.vr = attributes.getVR(tag);
            } else {
                this.vr = null;
            }
        }

        public boolean isExpandable() {
            return vr == VR.SQ || isItem;
        }

        public boolean isHexDisplayed() {
            if (attributes == null || tag == -1 || isExpandable())
                return false;
            if (vr == VR.OW || vr == VR.OB)
                return true;
            if (vr == VR.UN) {
                if (!TagUtils.isPrivateTag(tag))
                    return true;
                try {
                    byte[] b = attributes.getBytes(tag);
                    return !isPrintable(b);
                } catch (java.io.IOException e) {
                    return true; // Default to hex if error
                }
            }
            return false;
        }

        private boolean isPrintable(byte[] bytes) {
            if (bytes == null || bytes.length == 0)
                return true;
            for (byte b : bytes) {
                if (b < 32 && b != 10 && b != 13 && b != 9) {
                    return false;
                }
            }
            return true;
        }

        public int getDepth() {
            return depth;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public String formatValue() {
            if (attributes == null || tag == -1)
                return "";
            try {
                if (vr == VR.SQ) {
                    return "";
                }
                if (isHexDisplayed()) {
                    byte[] b = attributes.getBytes(tag);
                    return HexUtils.toHexString(b, 8);
                }
                if (vr == VR.UN) {
                    byte[] b = attributes.getBytes(tag);
                    if (b == null)
                        return "";
                    return attributes.getSpecificCharacterSet().decode(b, null);
                }
                return attributes.getString(tag);
            } catch (Exception e) {
                return "Error";
            }
        }

        public void setValue(Object value) {
            String strValue = (String) value;
            try {
                if (isExpandable() || isHexDisplayed()) {
                    return;
                }
                if (vr == VR.UN) {
                    byte[] b = attributes.getSpecificCharacterSet().encode(strValue, null);
                    attributes.setBytes(tag, VR.UN, b);
                } else {
                    attributes.setString(tag, vr, strValue);
                }
            } catch (Exception e) {
            }
        }
    }
}
