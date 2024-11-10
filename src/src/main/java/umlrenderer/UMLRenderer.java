package umlrenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

class UMLRenderer extends JPanel {
    private final UMLVisualizer visualizer;
    private final JFrame frame;

    private int targetOffsetX = 0, targetOffsetY = 0;
    private double canvasOffsetX = 0, canvasOffsetY = 0;
    private double zoomFactor = 1.0;
    private int initialClickX, initialClickY;

    private final Map<UMLClass, Color> classColors = new HashMap<>();
    private final Color[] colorPalette = {
            new Color(0xFF8080),
            new Color(0x699DFF),
            new Color(0xFFED91),
            new Color(0xFFAC73),
            new Color(0xE37DFF)
    };

    public UMLRenderer(UMLVisualizer visualizer) {
        this.visualizer = visualizer;
        this.frame = new JFrame("UML Visualizer");
        this.frame.setSize(960, 540);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.frame.setResizable(false);

        this.frame.add(this);

        this.frame.setVisible(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClickX = e.getX();
                initialClickY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                targetOffsetX += e.getX() - initialClickX;
                targetOffsetY += e.getY() - initialClickY;
                initialClickX = e.getX();
                initialClickY = e.getY();
            }
        });

        addMouseWheelListener(e -> {
            double mouseX = e.getX();
            double mouseY = e.getY();

            double zoomChange = e.getPreciseWheelRotation() * 0.1;
            double newZoomFactor = Math.max(0.5, Math.min(2.0, zoomFactor - zoomChange));

            if (newZoomFactor != zoomFactor) {
                zoomFactor = newZoomFactor;
            }

            repaint();
        });

        Timer timer = new Timer(16, e -> {
            canvasOffsetX += (targetOffsetX - canvasOffsetX) * 0.1;
            canvasOffsetY += (targetOffsetY - canvasOffsetY) * 0.1;
            repaint();
        });
        timer.start();
    }

    private Color assignColor(UMLClass umlClass) {
        if (classColors.containsKey(umlClass)) return classColors.get(umlClass);

        UMLClass topLevelClass = getTopLevelClass(umlClass);
        if (!classColors.containsKey(topLevelClass)) {
            classColors.put(topLevelClass, colorPalette[classColors.size() % colorPalette.length]);
        }

        return classColors.putIfAbsent(umlClass, classColors.get(topLevelClass));
    }

    private UMLClass getTopLevelClass(UMLClass umlClass) {
        return umlClass.getParentClass() == null ? umlClass : getTopLevelClass(umlClass.getParentClass());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(zoomFactor, zoomFactor);
        g2d.translate(canvasOffsetX, canvasOffsetY);

        int offset = 0;
        for (UMLClass umlClass : visualizer.getCompiledClasses()) {
            if (umlClass.getParentClass() == null) {
                drawClassHierarchy(g2d, umlClass, offset, 0);
                offset += getMaxTextWidth(g2d, umlClass) + 50;
            }
        }
    }

    private int drawClassHierarchy(Graphics2D g2d, UMLClass umlClass, int x, int y) {
        Color classColor = assignColor(umlClass);
        int width = getMaxTextWidth(g2d, umlClass) + 20;
        int subY = y + 100;

        for (UMLClass subclass : umlClass.getSubclasses()) {
            drawArrow(g2d, x + width / 2, y + 40, x + width / 2, subY + 50);
            subY = drawClassHierarchy(g2d, subclass, x, subY + 50);
        }
        renderClass(g2d, umlClass, x, y, classColor);
        return subY;
    }

    private int renderClass(Graphics2D g2d, UMLClass umlClass, int x, int y, Color color) {
        int padding = 10;
        int maxWidth = getMaxTextWidth(g2d, umlClass) + padding * 2;
        int lineHeight = g2d.getFontMetrics().getHeight();
        int boxHeight = (umlClass.getFields().size() + umlClass.getMethods().size() + 1) * lineHeight + padding * 4;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y, maxWidth, boxHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, maxWidth, boxHeight);

        int classNameHeight = lineHeight + padding;
        g2d.setColor(color);
        g2d.fillRect(x + 1, y + 1, maxWidth - 2, classNameHeight - 2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(umlClass.getClassName(), x + padding, y + lineHeight);

        int textY = y + classNameHeight + padding * 2;
        for (String field : umlClass.getFields()) {
            g2d.drawString(field, x + padding, textY);
            textY += lineHeight;
        }

        textY += padding;
        for (String method : umlClass.getMethods()) {
            g2d.drawString(method, x + padding, textY);
            textY += lineHeight;
        }

        return maxWidth;
    }

    private int getMaxTextWidth(Graphics2D g2d, UMLClass umlClass) {
        int maxWidth = g2d.getFontMetrics().stringWidth(umlClass.getClassName());

        for (String field : umlClass.getFields()) {
            maxWidth = Math.max(maxWidth, g2d.getFontMetrics().stringWidth(field));
        }
        for (String method : umlClass.getMethods()) {
            maxWidth = Math.max(maxWidth, g2d.getFontMetrics().stringWidth(method));
        }

        return maxWidth;
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
    }
}
