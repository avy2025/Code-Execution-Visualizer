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

        FlowchartNode start = new FlowchartNode("START", FlowchartNode.NodeType.START, -1);
        FlowchartNode end = new FlowchartNode("END", FlowchartNode.NodeType.END, -1);
        
        FlowchartNode current = start;
        
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i).strip();
            
            if (line.isEmpty() || line.startsWith("//") || line.equals("}")) {
                i++;
                continue;
            }

            if (line.startsWith("if") || line.startsWith("while")) {
                boolean isWhile = line.startsWith("while");
                FlowchartNode decision = new FlowchartNode(line, FlowchartNode.NodeType.DECISION, i);
                current.addNextNode(decision);
                
                int openBraceIdx = findOpenBrace(lines, i);
                int closeBraceIdx = findMatchingBrace(lines, openBraceIdx);
                
                if (openBraceIdx != -1 && closeBraceIdx != -1) {
                    List<String> bodyLines = lines.subList(openBraceIdx + 1, closeBraceIdx);
                    FlowchartNode bodyStart = generateSubPath(bodyLines, openBraceIdx + 1);
                    decision.addNextNode(bodyStart);
                    
                    if (isWhile) {
                        // Point the last node of the body back to the decision
                        FlowchartNode bodyLast = findLastNode(bodyStart);
                        if (bodyLast != null) bodyLast.addNextNode(decision);
                    }
                    
                    i = closeBraceIdx + 1;
                    current = decision; 
                } else {
                    i++;
                }
            } else {
                FlowchartNode process = new FlowchartNode(line, FlowchartNode.NodeType.PROCESS, i);
                current.addNextNode(process);
                current = process;
                i++;
            }
        }
        
        current.addNextNode(end);
        return start;
    }

    private FlowchartNode generateSubPath(List<String> lines, int baseIdx) {
        if (lines.isEmpty()) return null;
        
        FlowchartNode head = null;
        FlowchartNode current = null;
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.startsWith("//") || line.equals("}") || line.equals("{")) continue;
            
            FlowchartNode node = new FlowchartNode(line, FlowchartNode.NodeType.PROCESS, baseIdx + i);
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

    private FlowchartNode findLastNode(FlowchartNode node) {
        if (node == null) return null;
        if (node.getNextNodes().isEmpty()) return node;
        // Simple heuristic: follow the last branch
        FlowchartNode last = node;
        while (!last.getNextNodes().isEmpty()) {
            last = last.getNextNodes().get(last.getNextNodes().size() - 1);
        }
        return last;
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
