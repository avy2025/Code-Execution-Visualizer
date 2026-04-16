package visualizer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the Flowchart.
 */
public class FlowchartNode {
    public enum NodeType {
        START, END, PROCESS, DECISION
    }

    private final String text;
    private final NodeType type;
    private final List<FlowchartNode> nextNodes;
    
    // For visual layout
    private Point position;
    private int width = 140;
    private int height = 60;

    public FlowchartNode(String text, NodeType type) {
        this.text = text;
        this.type = type;
        this.nextNodes = new ArrayList<>();
        this.position = new Point(0, 0);
    }

    public void addNextNode(FlowchartNode node) {
        if (node != null) {
            nextNodes.add(node);
        }
    }

    public String getText() { return text; }
    public NodeType getType() { return type; }
    public List<FlowchartNode> getNextNodes() { return nextNodes; }
    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}
