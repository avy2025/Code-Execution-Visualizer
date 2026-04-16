package visualizer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Custom JPanel to render the flowchart with a premium aesthetic.
 */
public class FlowchartPanel extends JPanel {

    private FlowchartNode root;
    private final int VERTICAL_GAP = 60;
    private final int HORIZONTAL_GAP = 180;

    public FlowchartPanel() {
        setBackground(new Color(252, 253, 255));
    }

    public void setFlowchart(FlowchartNode root) {
        this.root = root;
        calculateLayout();
        repaint();
    }

    private void calculateLayout() {
        if (root == null) return;
        
        // Simple vertical layout
        Set<FlowchartNode> visited = new HashSet<>();
        calculateLayoutNode(root, getWidth() / 2, 50, visited);
        
        // Adjust panel size if needed
        int maxWidth = 0;
        int maxHeight = 0;
        for (FlowchartNode node : visited) {
            maxWidth = Math.max(maxWidth, node.getPosition().x + node.getWidth());
            maxHeight = Math.max(maxHeight, node.getPosition().y + node.getHeight());
        }
        setPreferredSize(new Dimension(maxWidth + 100, maxHeight + 100));
        revalidate();
    }

    private void calculateLayoutNode(FlowchartNode node, int x, int y, Set<FlowchartNode> visited) {
        if (node == null || visited.contains(node)) return;
        visited.add(node);
        
        node.setPosition(new Point(x - node.getWidth() / 2, y));
        
        List<FlowchartNode> next = node.getNextNodes();
        if (next.isEmpty()) return;
        
        if (next.size() == 1) {
            calculateLayoutNode(next.get(0), x, y + node.getHeight() + VERTICAL_GAP, visited);
        } else if (next.size() == 2) {
            // Decision branch: True to the left, False/Continue straight or right
            calculateLayoutNode(next.get(0), x - HORIZONTAL_GAP / 2, y + node.getHeight() + VERTICAL_GAP, visited);
            calculateLayoutNode(next.get(1), x + HORIZONTAL_GAP / 2, y + node.getHeight() + VERTICAL_GAP, visited);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (root == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Set<FlowchartNode> visited = new HashSet<>();
        drawNodeRecursive(g2, root, visited);
    }

    private void drawNodeRecursive(Graphics2D g2, FlowchartNode node, Set<FlowchartNode> visited) {
        if (node == null || visited.contains(node)) return;
        visited.add(node);

        // Draw connections first (behind nodes)
        for (FlowchartNode next : node.getNextNodes()) {
            drawConnection(g2, node, next);
            drawNodeRecursive(g2, next, visited);
        }

        // Draw node
        drawNode(g2, node);
    }

    private void drawNode(Graphics2D g2, FlowchartNode node) {
        int x = node.getPosition().x;
        int y = node.getPosition().y;
        int w = node.getWidth();
        int h = node.getHeight();

        Color color1, color2, textColor;
        Shape shape;

        switch (node.getType()) {
            case START:
            case END:
                color1 = new Color(52, 152, 219);
                color2 = new Color(41, 128, 185);
                textColor = Color.WHITE;
                shape = new RoundRectangle2D.Double(x, y, w, h, h, h); // Pill shape
                break;
            case DECISION:
                color1 = new Color(241, 196, 15);
                color2 = new Color(243, 156, 18);
                textColor = new Color(60, 60, 60);
                shape = createDiamond(x, y, w, h);
                break;
            case PROCESS:
            default:
                color1 = Color.WHITE;
                color2 = new Color(248, 249, 250);
                textColor = new Color(44, 62, 80);
                shape = new RoundRectangle2D.Double(x, y, w, h, 12, 12);
                break;
        }

        // Shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fill(getTranslatedShape(shape, 3, 3));

        // Background Gradient
        GradientPaint gp = new GradientPaint(x, y, color1, x, y + h, color2);
        g2.setPaint(gp);
        g2.fill(shape);

        // Border
        g2.setColor(new Color(0, 0, 0, 40));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(shape);

        // Text
        g2.setColor(textColor);
        g2.setFont(new Font("Inter", Font.BOLD, 12));
        drawCenteredString(g2, node.getText(), x, y, w, h);
    }

    private void drawConnection(Graphics2D g2, FlowchartNode from, FlowchartNode to) {
        Point p1 = new Point(from.getPosition().x + from.getWidth() / 2, from.getPosition().y + from.getHeight());
        Point p2 = new Point(to.getPosition().x + to.getWidth() / 2, to.getPosition().y);

        g2.setColor(new Color(189, 195, 199));
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        if (p1.x == p2.x) {
            // Straight line
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            drawArrowHead(g2, p2.x, p2.y, Math.PI / 2);
        } else {
            // Elbow line
            Path2D.Double path = new Path2D.Double();
            path.moveTo(p1.x, p1.y);
            int midY = p1.y + (p2.y - p1.y) / 2;
            path.lineTo(p1.x, midY);
            path.lineTo(p2.x, midY);
            path.lineTo(p2.x, p2.y);
            g2.draw(path);
            drawArrowHead(g2, p2.x, p2.y, Math.PI / 2);
            
            // Branch labels for Decision
            if (from.getType() == FlowchartNode.NodeType.DECISION) {
                g2.setFont(new Font("Inter", Font.BOLD, 10));
                g2.setColor(new Color(127, 140, 141));
                if (to.getPosition().x < from.getPosition().x) {
                    g2.drawString("TRUE", p1.x - 40, midY - 5);
                } else {
                    g2.drawString("FALSE", p1.x + 10, midY - 5);
                }
            }
        }
    }

    private void drawArrowHead(Graphics2D g2, int x, int y, double angle) {
        int size = 8;
        Path2D.Double arrow = new Path2D.Double();
        arrow.moveTo(x, y);
        arrow.lineTo(x - size, y - size);
        arrow.lineTo(x + size, y - size);
        arrow.closePath();
        g2.fill(arrow);
    }

    private Shape createDiamond(int x, int y, int w, int h) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x + w / 2.0, y);
        path.lineTo(x + w, y + h / 2.0);
        path.lineTo(x + w / 2.0, y + h);
        path.lineTo(x, y + h / 2.0);
        path.closePath();
        return path;
    }

    private Shape getTranslatedShape(Shape s, int dx, int dy) {
        if (s instanceof RoundRectangle2D) {
            RoundRectangle2D r = (RoundRectangle2D) s;
            return new RoundRectangle2D.Double(r.getX() + dx, r.getY() + dy, r.getWidth(), r.getHeight(), r.getArcWidth(), r.getArcHeight());
        }
        if (s instanceof Path2D) {
            Path2D p = (Path2D) ((Path2D) s).clone();
            p.transform(java.awt.geom.AffineTransform.getTranslateInstance(dx, dy));
            return p;
        }
        return s;
    }

    private void drawCenteredString(Graphics2D g2, String text, int x, int y, int w, int h) {
        FontMetrics fm = g2.getFontMetrics();
        // Simple word wrap or truncation
        if (text.length() > 20) text = text.substring(0, 17) + "...";
        
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + ((h - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, tx, ty);
    }
}
