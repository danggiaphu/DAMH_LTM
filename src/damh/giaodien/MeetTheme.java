package damh.giaodien;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class MeetTheme {

    // --- GOOGLE MEET COLOR PALETTE ---
    public static final Color BG_DARK = new Color(0x20, 0x21, 0x24);          // Màu nền chính Google Meet
    public static final Color BG_SURFACE = new Color(0x29, 0x2a, 0x2d);       // Nền thanh điều khiển / floating dock
    public static final Color CARD_BG = new Color(0x2d, 0x2e, 0x30);          // Nền ô video / thẻ nội dung
    public static final Color CARD_BG_ALT = new Color(0x35, 0x37, 0x3a);      // Nền phụ
    public static final Color CARD_BORDER = new Color(0x3c, 0x40, 0x43);      // Viền thẻ Google Meet
    public static final Color CARD_BORDER_FOCUS = new Color(0x8a, 0xb4, 0xf8);// Viền khi chọn (Google Blue light)

    public static final Color TEXT_PRIMARY = new Color(0xf1, 0xf3, 0xf4);     // Chữ trắng sắc nét
    public static final Color TEXT_SECONDARY = new Color(0x9a, 0xa0, 0xa6);   // Chữ xám Google Meet
    public static final Color TEXT_MUTED = new Color(0x80, 0x86, 0x8b);       // Chữ mờ hơn

    public static final Color GOOGLE_BLUE = new Color(0x1a, 0x73, 0xe8);      // Xanh Google chuẩn
    public static final Color GOOGLE_BLUE_HOVER = new Color(0x18, 0x5a, 0xbc);
    public static final Color GOOGLE_BLUE_LIGHT = new Color(0x8a, 0xb4, 0xf8);
    public static final Color GOOGLE_GREEN = new Color(0x1e, 0x8e, 0x3e);     // Xanh lá Google Meet
    public static final Color GOOGLE_GREEN_BRIGHT = new Color(0x34, 0xa8, 0x53);
    public static final Color GOOGLE_GREEN_HOVER = new Color(0x13, 0x73, 0x33);
    public static final Color GOOGLE_RED = new Color(0xea, 0x43, 0x35);       // Đỏ nút kết thúc cuộc gọi
    public static final Color GOOGLE_RED_HOVER = new Color(0xd9, 0x30, 0x25);
    public static final Color GOOGLE_YELLOW = new Color(0xfb, 0xbc, 0x04);    // Vàng cảnh báo Google
    public static final Color GOOGLE_AMBER = new Color(0xf2, 0x99, 0x00);

    public static final Color BTN_NEUTRAL_BG = new Color(0x3c, 0x40, 0x43);   // Nút điều khiển xám Meet
    public static final Color BTN_NEUTRAL_HOVER = new Color(0x47, 0x4b, 0x50);

    public static final Color INPUT_BG = new Color(0x30, 0x31, 0x34);
    public static final Color INPUT_BORDER = new Color(0x5f, 0x63, 0x68);

    public static final Color TABLE_ROW_ALT = new Color(0x28, 0x29, 0x2c);
    public static final Color TABLE_SELECTION = new Color(0x18, 0x5a, 0xbc);

    /**
     * Vẽ biểu tượng camera Google Meet chuẩn 4 màu (Xanh lá, Xanh dương, Đỏ, Vàng)
     */
    public static void drawMeetLogo(Graphics2D g2, int x, int y, int size) {
        setRenderingHints(g2);
        float s = size / 40.0f; // Scale theo kích thước chuẩn 40px
        
        // Thân camera: bo góc
        int bodyW = (int) (22 * s);
        int bodyH = (int) (22 * s);
        int bodyX = x + (int) (4 * s);
        int bodyY = y + (int) (9 * s);

        // Nửa trên thân: Xanh dương Google
        g2.setColor(GOOGLE_BLUE);
        g2.fillRoundRect(bodyX, bodyY, bodyW, (int)(12 * s), (int)(6 * s), (int)(6 * s));
        // Nửa dưới thân: Xanh lá Google
        g2.setColor(GOOGLE_GREEN_BRIGHT);
        g2.fillRoundRect(bodyX, bodyY + (int)(10 * s), bodyW, (int)(12 * s), (int)(6 * s), (int)(6 * s));
        
        // Ống kính (Tam giác cạnh phải)
        int lensX = bodyX + bodyW + (int)(2 * s);
        int[] topX = {lensX, lensX + (int)(10 * s), lensX + (int)(10 * s), lensX};
        int[] topY = {bodyY + (int)(5 * s), bodyY, bodyY + (int)(11 * s), bodyY + (int)(9 * s)};
        g2.setColor(GOOGLE_RED);
        g2.fillPolygon(topX, topY, 4);

        int[] botX = {lensX, lensX + (int)(10 * s), lensX + (int)(10 * s), lensX};
        int[] botY = {bodyY + (int)(13 * s), bodyY + (int)(11 * s), bodyY + (int)(22 * s), bodyY + (int)(17 * s)};
        g2.setColor(GOOGLE_YELLOW);
        g2.fillPolygon(botX, botY, 4);
    }

    // --- FONTS ---
    public static final String FONT_FAMILY = getPreferredFontFamily();

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, (int) size);
    }

    public static Font fontBold(float size) {
        return font(Font.BOLD, size);
    }

    public static Font fontPlain(float size) {
        return font(Font.PLAIN, size);
    }

    public static Font fontMono(float size) {
        return new Font("Consolas", Font.PLAIN, (int) size);
    }

    private static String getPreferredFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] families = ge.getAvailableFontFamilyNames();
        for (String f : new String[]{"Segoe UI", "Google Sans", "Roboto", "Arial"}) {
            for (String available : families) {
                if (f.equalsIgnoreCase(available)) {
                    return f;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    /**
     * Bật Antialiasing chất lượng cao cho Graphics2D
     */
    public static void setRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    // =========================================================================
    // CUSTOM MEET COMPONENTS
    // =========================================================================

    /**
     * Panel hiển thị Logo Google Meet 4 màu kèm tiêu đề
     */
    public static class MeetLogoHeader extends JPanel {
        private String title = "Google Meet";
        private String subtitle = "Hệ Thống Nộp Bài & Thi Trực Tuyến Qua Mạng LAN";
        private int logoSize = 36;

        public MeetLogoHeader(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
            setOpaque(false);
            setPreferredSize(new Dimension(500, 52));
        }

        public MeetLogoHeader() {
            this("Google Meet", "Hệ Thống Nộp Bài & Thi Trực Tuyến Qua Mạng LAN");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);

            int h = getHeight();
            int logoY = (h - logoSize) / 2;
            drawMeetLogo(g2, 8, logoY, logoSize);

            // Chữ "Google Meet"
            g2.setColor(TEXT_PRIMARY);
            g2.setFont(fontBold(18));
            g2.drawString(title, 8 + logoSize + 14, logoY + 18);

            // Phụ đề
            if (subtitle != null && !subtitle.isEmpty()) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(fontPlain(12));
                g2.drawString(subtitle, 8 + logoSize + 15, logoY + 34);
            }

            g2.dispose();
        }
    }

    /**
     * Thẻ Card bo tròn góc phong cách Google Meet
     */
    public static class MeetCard extends JPanel {
        private int cornerRadius = 16;
        private Color bgColor = CARD_BG;
        private Color borderColor = CARD_BORDER;
        private boolean showBorder = true;
        private Color leftAccentColor = null;
        private int accentWidth = 4;
        private boolean hoverEffect = false;
        private boolean isHovered = false;

        public MeetCard(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            setOpaque(false);
            initHover();
        }

        public MeetCard() {
            this(16);
        }

        private void initHover() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (hoverEffect) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoverEffect) {
                        isHovered = false;
                        repaint();
                    }
                }
            });
        }

        public void setHoverEffect(boolean hover) {
            this.hoverEffect = hover;
            if (hover) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }

        public void setLeftAccent(Color color) {
            this.leftAccentColor = color;
            repaint();
        }

        public void setCardBackground(Color color) {
            this.bgColor = color;
            repaint();
        }

        public void setBorderColor(Color color) {
            this.borderColor = color;
            repaint();
        }

        public void setShowBorder(boolean show) {
            this.showBorder = show;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);

            int width = getWidth();
            int height = getHeight();

            Color curBg = (isHovered && hoverEffect) ? CARD_BG_ALT : bgColor;
            Color curBorder = (isHovered && hoverEffect) ? CARD_BORDER_FOCUS : borderColor;

            // Vẽ nền bo tròn
            g2.setColor(curBg);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

            // Vẽ dải Accent bên trái nếu có
            if (leftAccentColor != null) {
                g2.setColor(leftAccentColor);
                g2.fillRoundRect(0, 0, accentWidth + cornerRadius, height, cornerRadius, cornerRadius);
                g2.setColor(curBg);
                g2.fillRect(accentWidth, 0, cornerRadius, height);
            }

            // Vẽ viền
            if (showBorder) {
                g2.setColor(curBorder);
                g2.setStroke(new BasicStroke(isHovered ? 1.6f : 1.2f));
                g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Nút bấm bo tròn phong cách Google Meet
     */
    public static class MeetButton extends JButton {
        public enum Variant {
            PRIMARY,    // Xanh Google Meet (#1a73e8)
            SUCCESS,    // Xanh lá Meet (#1e8e3e)
            DANGER,     // Đỏ Meet End-Call (#ea4335)
            WARNING,    // Vàng hổ phách Meet (#f29900)
            NEUTRAL,    // Xám nút điều khiển (#3c4043)
            OUTLINE     // Trong suốt có viền
        }

        private Variant variant = Variant.PRIMARY;
        private int cornerRadius = 24;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private Color customColor = null;

        public MeetButton(String text, Variant variant) {
            super(text);
            this.variant = variant;
            init();
        }

        public MeetButton(String text) {
            this(text, Variant.PRIMARY);
        }

        public void setVariant(Variant v) {
            this.variant = v;
            repaint();
        }

        public void setCornerRadius(int r) {
            this.cornerRadius = r;
            repaint();
        }

        public void setCustomColor(Color c) {
            this.customColor = c;
            repaint();
        }

        private void init() {
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(fontBold(13));
            setForeground(TEXT_PRIMARY);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);

            int width = getWidth();
            int height = getHeight();

            Color bg;
            Color border = null;

            if (customColor != null) {
                bg = customColor;
                if (isHovered) bg = bg.brighter();
            } else {
                switch (variant) {
                    case SUCCESS:
                        bg = isHovered ? GOOGLE_GREEN_HOVER : GOOGLE_GREEN;
                        break;
                    case WARNING:
                        bg = isHovered ? new Color(0xd9, 0x82, 0x00) : GOOGLE_AMBER;
                        break;
                    case DANGER:
                        bg = isHovered ? GOOGLE_RED_HOVER : GOOGLE_RED;
                        break;
                    case NEUTRAL:
                        bg = isHovered ? BTN_NEUTRAL_HOVER : BTN_NEUTRAL_BG;
                        break;
                    case OUTLINE:
                        bg = isHovered ? new Color(0x3c, 0x40, 0x43, 100) : new Color(0, 0, 0, 0);
                        border = CARD_BORDER_FOCUS;
                        break;
                    case PRIMARY:
                    default:
                        bg = isHovered ? GOOGLE_BLUE_HOVER : GOOGLE_BLUE;
                        break;
                }
            }

            if (!isEnabled()) {
                bg = new Color(0x3c, 0x40, 0x43, 120);
                setForeground(TEXT_MUTED);
            } else {
                setForeground(TEXT_PRIMARY);
            }

            if (isPressed) {
                bg = bg.darker();
            }

            // Vẽ nền nút
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

            // Vẽ viền outline nếu có
            if (border != null && isEnabled()) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Ô nhập văn bản phong cách Google Meet Dark Mode
     */
    public static class MeetTextField extends JTextField {
        private String placeholder = "";
        private boolean isFocused = false;
        private int cornerRadius = 10;

        public MeetTextField(String placeholder, int columns) {
            super(columns);
            this.placeholder = placeholder;
            init();
        }

        public MeetTextField(int columns) {
            this("", columns);
        }

        public MeetTextField(String text) {
            super(text);
            init();
        }

        public MeetTextField() {
            this("", 0);
        }

        public void setPlaceholder(String p) {
            this.placeholder = p;
            repaint();
        }

        private void init() {
            setOpaque(false);
            setFont(fontPlain(13));
            setForeground(TEXT_PRIMARY);
            setCaretColor(GOOGLE_BLUE_LIGHT);
            setSelectedTextColor(Color.WHITE);
            setSelectionColor(GOOGLE_BLUE);
            setBorder(new EmptyBorder(8, 12, 8, 12));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);

            int width = getWidth();
            int height = getHeight();

            // Nền
            g2.setColor(INPUT_BG);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

            // Viền (xanh khi focus, xám khi bình thường)
            if (isFocused) {
                g2.setColor(CARD_BORDER_FOCUS);
                g2.setStroke(new BasicStroke(1.6f));
            } else {
                g2.setColor(INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

            g2.dispose();
            super.paintComponent(g);

            // Placeholder
            if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
                Graphics2D gText = (Graphics2D) g.create();
                setRenderingHints(gText);
                gText.setColor(TEXT_MUTED);
                gText.setFont(getFont());
                FontMetrics fm = gText.getFontMetrics();
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();
                gText.drawString(placeholder, 12, y);
                gText.dispose();
            }
        }
    }

    /**
     * Huy hiệu trạng thái hình viên thuốc bo tròn (Pill Badge)
     */
    public static class MeetBadge extends JPanel {
        private String text;
        private Color dotColor;
        private Color textColor;
        private Color bgColor;

        public MeetBadge(String text, Color dotColor, Color textColor, Color bgColor) {
            this.text = text;
            this.dotColor = dotColor;
            this.textColor = textColor;
            this.bgColor = bgColor;
            setOpaque(false);
            setBorder(new EmptyBorder(4, 12, 4, 12));
        }

        public void updateBadge(String text, Color dotColor, Color textColor, Color bgColor) {
            this.text = text;
            this.dotColor = dotColor;
            this.textColor = textColor;
            this.bgColor = bgColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);

            int width = getWidth();
            int height = getHeight();

            // Nền viên thuốc bo tròn
            g2.setColor(bgColor != null ? bgColor : new Color(0x3c, 0x40, 0x43));
            g2.fillRoundRect(0, 0, width, height, height, height);

            int textX = 14;
            // Vẽ chấm màu nếu có
            if (dotColor != null) {
                int dotSize = 8;
                int dotY = (height - dotSize) / 2;
                g2.setColor(dotColor);
                g2.fillOval(10, dotY, dotSize, dotSize);
                textX = 24;
            }

            // Vẽ chữ
            g2.setColor(textColor != null ? textColor : TEXT_PRIMARY);
            g2.setFont(fontBold(12));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, textX, textY);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(fontBold(12));
            int textWidth = fm.stringWidth(text);
            int extra = (dotColor != null) ? 36 : 24;
            return new Dimension(textWidth + extra, 28);
        }
    }

    /**
     * Avatar hình tròn đại diện thành viên tham gia Google Meet
     */
    public static class MeetAvatar extends JPanel {
        private String initials = "SV";
        private Color bgStart = new Color(0x1a, 0x73, 0xe8);
        private Color bgEnd = new Color(0x18, 0x5a, 0xbc);
        private int diameter = 80;

        public MeetAvatar(String initials, int diameter) {
            this.initials = initials;
            this.diameter = diameter;
            setOpaque(false);
            setPreferredSize(new Dimension(diameter, diameter));
        }

        public void setInitials(String initials) {
            this.initials = initials;
            repaint();
        }

        public void setGradient(Color start, Color end) {
            this.bgStart = start;
            this.bgEnd = end;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);

            int w = getWidth();
            int h = getHeight();
            int d = Math.min(w, h);
            int x = (w - d) / 2;
            int y = (h - d) / 2;

            // Gradient tròn
            GradientPaint gp = new GradientPaint(x, y, bgStart, x + d, y + d, bgEnd);
            g2.setPaint(gp);
            g2.fillOval(x, y, d, d);

            // Viền mỏng
            g2.setColor(new Color(255, 255, 255, 40));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x + 1, y + 1, d - 2, d - 2);

            // Chữ viết tắt
            g2.setColor(Color.WHITE);
            float fontSize = d * 0.40f;
            g2.setFont(fontBold(fontSize));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (d - fm.stringWidth(initials)) / 2;
            int textY = y + (d - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, textX, textY);

            g2.dispose();
        }
    }

    /**
     * Thanh tiến trình bo tròn phong cách Google Meet
     */
    public static class MeetProgressBar extends JProgressBar {
        private Color colorStart = GOOGLE_BLUE;
        private Color colorEnd = GOOGLE_GREEN_BRIGHT;

        public MeetProgressBar() {
            super(0, 100);
            setOpaque(false);
            setBorderPainted(false);
            setStringPainted(true);
            setForeground(Color.WHITE);
            setFont(fontBold(11));
            setPreferredSize(new Dimension(300, 20));
        }

        public void setBarColors(Color start, Color end) {
            this.colorStart = start;
            this.colorEnd = end;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);

            int width = getWidth();
            int height = getHeight();

            // Track background
            g2.setColor(CARD_BORDER);
            g2.fillRoundRect(0, 0, width, height, height, height);

            // Progress bar
            int progressWidth = (int) ((double) getValue() / getMaximum() * width);
            if (progressWidth > 0) {
                GradientPaint gp = new GradientPaint(0, 0, colorStart, progressWidth, 0, colorEnd);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, progressWidth, height, height, height);
            }

            // String painted
            if (isStringPainted()) {
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                String str = getValue() + "%";
                FontMetrics fm = g2.getFontMetrics();
                int strX = (width - fm.stringWidth(str)) / 2;
                int strY = (height - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(str, strX, strY);
            }

            g2.dispose();
        }
    }

    /**
     * Tùy biến thanh cuộn ScrollBar phong cách Google Meet mượt mà
     */
    public static class MeetScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(0x5f, 0x63, 0x68);
            this.trackColor = BG_DARK;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);
            g2.setColor(isThumbRollover() ? new Color(0x8a, 0xb4, 0xf8) : new Color(0x5f, 0x63, 0x68));
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(BG_DARK);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }

    /**
     * Tùy biến Bảng (JTable) phong cách Google Meet Dark Mode
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(CARD_BORDER);
        table.setFont(fontPlain(12));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_DARK);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(fontBold(12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    /**
     * Áp dụng thanh cuộn Meet cho JScrollPane
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        scrollPane.getVerticalScrollBar().setUI(new MeetScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new MeetScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
    }

    /**
     * Tùy biến JComboBox phong cách Google Meet Dark
     */
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(INPUT_BG);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(fontPlain(12));
        comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                if (isSelected) {
                    setBackground(TABLE_SELECTION);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(INPUT_BG);
                    setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        });
    }

    /**
     * TextArea phong cách Google Meet Dark bo tròn góc
     */
    public static class MeetTextArea extends JTextArea {
        private String placeholder = "";

        public MeetTextArea(String placeholder, int rows, int columns) {
            super(rows, columns);
            this.placeholder = placeholder;
            init();
        }

        public MeetTextArea(int rows, int columns) {
            this("", rows, columns);
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        private void init() {
            setBackground(INPUT_BG);
            setForeground(TEXT_PRIMARY);
            setCaretColor(GOOGLE_BLUE_LIGHT);
            setSelectedTextColor(Color.WHITE);
            setSelectionColor(GOOGLE_BLUE);
            setFont(fontPlain(13));
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(new EmptyBorder(8, 10, 8, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                setRenderingHints(g2);
                g2.setColor(TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, 10, fm.getAscent() + 8);
                g2.dispose();
            }
        }
    }
}
