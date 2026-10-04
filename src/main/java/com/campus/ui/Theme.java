package com.campus.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * One place for colours, fonts and styling helpers.
 * Change a colour here and the whole app changes.
 */
public class Theme {
    // ---- colours ----
    public static final Color BG      = new Color(0xF3F5FB);
    public static final Color SIDEBAR = new Color(0x1B1F3B);
    public static final Color TEXT    = new Color(0x2D3142);
    public static final Color MUTED   = new Color(0x7A8199);
    public static final Color LINE    = new Color(0xD9DEEA);

    // one accent colour per feature
    public static final Color DASH = new Color(0x4F46E5); // indigo
    public static final Color ATT  = new Color(0x10B981); // green
    public static final Color TASK = new Color(0xF59E0B); // amber
    public static final Color EXP  = new Color(0xEC4899); // pink
    public static final Color PLAN = new Color(0x8B5CF6); // violet

    public static final Color DANGER = new Color(0xEF4444);
    public static final Color GREY   = new Color(0x6B7280);

    // ---- fonts ----
    public static final Font BASE  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font BIG   = new Font("Segoe UI", Font.BOLD, 34);

    /** Blend a colour towards white (t = 0 keeps it, t = 1 gives white). */
    public static Color lighten(Color c, float t) {
        return new Color(
                (int) (c.getRed() + (255 - c.getRed()) * t),
                (int) (c.getGreen() + (255 - c.getGreen()) * t),
                (int) (c.getBlue() + (255 - c.getBlue()) * t));
    }

    // ---- rounded white card with a soft shadow ----
    public static class RoundedPanel extends JPanel {
        private final Color fill;
        public RoundedPanel(Color fill) {
            this.fill = fill;
            setOpaque(false);
            setBorder(new EmptyBorder(14, 18, 18, 18));
        }
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0, 0, 0, 22));
            g.fillRoundRect(1, 4, getWidth() - 2, getHeight() - 4, 20, 20); // shadow
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 20, 20);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    public static RoundedPanel card() {
        RoundedPanel p = new RoundedPanel(Color.WHITE);
        p.setLayout(new BorderLayout());
        return p;
    }

    public static JPanel formRow() {
        JPanel p = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 8));
        p.setOpaque(false);
        return p;
    }

    /** Keeps a label and its input together so they wrap as one unit. */
    public static JPanel group(String label, JComponent input) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.add(label(label));
        p.add(input);
        return p;
    }

    /** A FlowLayout that moves items to a new line when the window is too narrow. */
    public static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) { return size(target, true); }

        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension d = size(target, false);
            d.width -= getHgap() + 1;
            return d;
        }

        private Dimension size(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int width = target.getWidth();
                if (width == 0) {
                    Container p = target.getParent();
                    while (p != null && p.getWidth() == 0) p = p.getParent();
                    if (p != null) width = p.getWidth() - p.getInsets().left - p.getInsets().right;
                }
                if (width <= 0) width = Integer.MAX_VALUE;
                Insets in = target.getInsets();
                int maxWidth = width - in.left - in.right - getHgap() * 2;
                Dimension dim = new Dimension(0, 0);
                int rowW = 0, rowH = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowW + d.width > maxWidth && rowW > 0) {
                        addRow(dim, rowW, rowH);
                        rowW = 0;
                        rowH = 0;
                    }
                    if (rowW != 0) rowW += getHgap();
                    rowW += d.width;
                    rowH = Math.max(rowH, d.height);
                }
                addRow(dim, rowW, rowH);
                dim.width += in.left + in.right + getHgap() * 2;
                dim.height += in.top + in.bottom + getVgap() * 2;
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowW, int rowH) {
            dim.width = Math.max(dim.width, rowW);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowH;
        }
    }

    // ---- rounded coloured button ----
    public static class RoundButton extends JButton {
        private final Color base;
        RoundButton(String text, Color base) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(BOLD);
            setBorder(new EmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = base;
            if (getModel().isPressed()) c = base.darker();
            else if (getModel().isRollover()) c = lighten(base, 0.18f);
            g.setColor(c);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    public static JButton button(String text, Color color) {
        return new RoundButton(text, color);
    }

    // ---- inputs ----
    public static void styleField(JTextField f) {
        f.setFont(BASE);
        f.setForeground(TEXT);
        f.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        styleField(f);
        return f;
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(BASE);
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    // ---- tables ----
    public static class RowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(BASE);
            if (!sel) {
                setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xF7F8FD));
                setForeground(TEXT);
            }
            return this;
        }
    }

    public static void styleTable(JTable t, Color accent) {
        t.setRowHeight(34);
        t.setFont(BASE);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(lighten(accent, 0.78f));
        t.setSelectionForeground(TEXT);
        t.setDefaultRenderer(Object.class, new RowRenderer());

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, false, false, r, c);
                setOpaque(true);
                setBackground(accent);
                setForeground(Color.WHITE);
                setFont(BOLD);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(LEFT);
                return this;
            }
        });
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static EmptyBorder pad(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }
}