package visualizer;

import java.util.List;

/**
 * Generates a flowchart structure from source code.
 */
public class FlowchartGenerator {

    public FlowchartNode generate(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new FlowchartNode("Empty Code", FlowchartNode.NodeType.START);
        }

        FlowchartNode start = new FlowchartNode("START", FlowchartNode.NodeType.START);
        FlowchartNode end = new FlowchartNode("END", FlowchartNode.NodeType.END);
        
        FlowchartNode current = start;
        
        // This is a naive implementation that handles sequential statements and simple IF blocks.
        // It uses index-based traversal to manage blocks.
        
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i).strip();
            
            if (line.isEmpty() || line.startsWith("//") || line.equals("}")) {
                i++;
                continue;
            }

            if (line.startsWith("if")) {
                FlowchartNode decision = new FlowchartNode(line, FlowchartNode.NodeType.DECISION);
                current.addNextNode(decision);
                
                // Find true branch
                int openBraceIdx = findOpenBrace(lines, i);
                int closeBraceIdx = findMatchingBrace(lines, openBraceIdx);
                
                if (openBraceIdx != -1 && closeBraceIdx != -1) {
                    List<String> trueLines = lines.subList(openBraceIdx + 1, closeBraceIdx);
                    FlowchartNode trueBranchStart = generateSubPath(trueLines);
                    decision.addNextNode(trueBranchStart);
                    
                    // The true branch should eventually lead back to the node after the if-block
                    // However, for simplicity in our basic renderer, we'll just track the decision.
                    // A more robust graph builder would use a join node.
                    
                    i = closeBraceIdx + 1;
                    current = decision; // For now, we continue from decision for the false branch path
                } else {
                    i++;
                }
            } else {
                FlowchartNode process = new FlowchartNode(line, FlowchartNode.NodeType.PROCESS);
                current.addNextNode(process);
                current = process;
                i++;
            }
        }
        
        current.addNextNode(end);
        return start;
    }

    private FlowchartNode generateSubPath(List<String> lines) {
        if (lines.isEmpty()) return null;
        
        FlowchartNode head = null;
        FlowchartNode current = null;
        
        for (String line : lines) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("//") || line.equals("}") || line.equals("{")) continue;
            
            FlowchartNode node = new FlowchartNode(line, FlowchartNode.NodeType.PROCESS);
            if (head == null) {
                head = node;
                current = node;
            } else {
                current.addNextNode(node);
                current = node;
            }
        }
        return head;
    }

    private int findOpenBrace(List<String> lines, int startIdx) {
        for (int i = startIdx; i < lines.size(); i++) {
            if (lines.get(i).contains("{")) return i;
        }
        return -1;
    }

    private int findMatchingBrace(List<String> lines, int openIdx) {
        if (openIdx == -1) return -1;
        int count = 0;
        for (int i = openIdx; i < lines.size(); i++) {
            if (lines.get(i).contains("{")) count++;
            if (lines.get(i).contains("}")) {
                count--;
                if (count == 0) return i;
            }
        }
        return -1;
    }
}
