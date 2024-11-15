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

    private final Map<UMLClass, Rectangle> classBounds = new HashMap<>();
    private UMLClass selectedClass = null;

    private int targetOffsetX = 0, targetOffsetY = 0;
    private double canvasOffsetX = 0, canvasOffsetY = 0;
    private int initialClickX, initialClickY;

    private final Map<UMLClass, Color> classColors = new HashMap<>();
    private final Color[] colorPalette = {
            new Color(0xFF8080),
            new Color(0x699DFF),
            new Color(0xFFED91),
            //new Color(0xFFAC73),
            new Color(0xE37DFF)
    };

    public UMLRenderer(UMLVisualizer visualizer) {
        this.visualizer = visualizer;
        JFrame frame = new JFrame("UML Visualizer");
        frame.setSize(960, 540);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        frame.add(this);
        frame.setVisible(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClickX = e.getX();
                initialClickY = e.getY();

                int clickX = (int) ((e.getX() - canvasOffsetX));
                int clickY = (int) ((e.getY() - canvasOffsetY));

                boolean foundClass = false;
                for (Map.Entry<UMLClass, Rectangle> entry : classBounds.entrySet()) {
                    UMLClass umlClass = entry.getKey();
                    Rectangle bounds = entry.getValue();

                    if (bounds.contains(clickX, clickY)) {
                        selectedClass = getTopLevelClass(umlClass);
                        foundClass = true;
                        repaint();
                        break;
                    }
                }
                if(!foundClass) {
                    selectedClass = null;
                }
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
        g2d.translate(canvasOffsetX, canvasOffsetY);

        classBounds.clear();

        int offset = 0;
        int selectedClassOffset = 0;
        for (UMLClass umlClass : visualizer.getCompiledClasses()) {
            if (umlClass.getParentClass() == null) {
                if(selectedClass == null || !selectedClass.equals(umlClass)) {
                    drawClassHierarchy(g2d, umlClass, offset, 0);
                } else {
                    selectedClassOffset = offset;
                }
                offset += getMaxTextWidth(g2d, umlClass) + 50;
            }
        }
        if(selectedClass != null) {
            g2d.translate(-canvasOffsetX, -canvasOffsetY);
            g2d.setColor(new Color(0x40000000, true));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.translate(canvasOffsetX, canvasOffsetY);
            drawClassHierarchy(g2d, selectedClass, selectedClassOffset, 0);
        }
    }

    private void drawClassHierarchy(Graphics2D g2d, UMLClass umlClass, int x, int y) {
        Color classColor = assignColor(umlClass);

        boolean selected = umlClass.equals(selectedClass);

        int height = renderClass(g2d, umlClass, x, y, classColor, selected);

        classBounds.put(umlClass, new Rectangle(x, y, getMaxTextWidth(g2d, umlClass) + 20, height));

        int spacing = 40;

        if(selected) {
            int totalWidth = 0;
            for (UMLClass subclass : umlClass.getSubclasses()) {
                totalWidth += getMaxTextWidth(g2d, subclass) + spacing * 2;
            }
            totalWidth -= spacing * 2;

            int currentX = x - (totalWidth - getMaxTextWidth(g2d, umlClass)) / 2;
            for (UMLClass subclass : umlClass.getSubclasses()) {
                int subclassWidth = getMaxTextWidth(g2d, subclass);
                drawClassHierarchy(g2d, subclass, currentX, y + height + spacing);
                currentX += subclassWidth + spacing * 2;
            }
        }
    }

    private int renderClass(Graphics2D g2d, UMLClass umlClass, int x, int y, Color color, boolean selected) {
        int padding = 10;
        int maxWidth = getMaxTextWidth(g2d, umlClass) + padding * 2;

        int lineHeight = g2d.getFontMetrics().getHeight();
        int boxHeight = (umlClass.getFields().size() + umlClass.getMethods().size() + 1) * lineHeight + padding * 4;

        g2d.setColor(Color.white);
        g2d.fillRect(x, y, maxWidth, boxHeight);

        // Fill class name area
        int classNameHeight = lineHeight + padding;
        g2d.setColor(color);
        g2d.fillRect(x, y, maxWidth, classNameHeight);

        // Draw class name
        g2d.setFont(g2d.getFont().deriveFont(umlClass.isAbstract() ? Font.ITALIC : Font.PLAIN));
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

        return boxHeight;
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
}
