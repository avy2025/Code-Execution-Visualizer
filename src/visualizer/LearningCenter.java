package visualizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * LearningCenter provides tips on improving code and fun memorization techniques.
 */
public class LearningCenter extends JPanel {

    public LearningCenter() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JEditorPane content = new JEditorPane();
        content.setEditable(false);
        content.setContentType("text/html");
        
        String htmlContent = "<html>" +
            "<body style='font-family: \"Segoe UI\", Tahoma, sans-serif; padding: 25px; color: #2d3436; background-color: #ffffff;'>" +
            "    <div style='text-align: center; margin-bottom: 30px;'>" +
            "        <h1 style='color: #0984e3; margin-bottom: 5px;'>🎓 Code Mastery Hub</h1>" +
            "        <p style='color: #636e72; font-size: 14px;'>Master the art of writing and remembering clean code.</p>" +
            "    </div>" +
            "    " +
            "    <div style='background-color: #f1f2f6; padding: 15px; border-radius: 12px; margin-bottom: 25px; border-left: 6px solid #0984e3;'>" +
            "        <h2 style='color: #2d3436; margin-top: 0;'>🚀 How to Improve Your Code</h2>" +
            "        <ul style='list-style-type: none; padding-left: 0;'>" +
            "            <li style='margin-bottom: 12px;'><b>✨ Clean Naming:</b> Use descriptive names. <code>calculateTotal()</code> is better than <code>doIt()</code>.</li>" +
            "            <li style='margin-bottom: 12px;'><b>🧩 One Job:</b> Each function should do exactly one thing. If it's doing more, break it down!</li>" +
            "            <li style='margin-bottom: 12px;'><b>♻️ DRY Principle:</b> <i>Don't Repeat Yourself.</i> Reusable code is happy code.</li>" +
            "            <li style='margin-bottom: 12px;'><b>📝 Self-Documenting:</b> Aim for code that is so clear it doesn't need many comments.</li>" +
            "        </ul>" +
            "    </div>" +
            "    " +
            "    <div style='background-color: #fff9db; padding: 15px; border-radius: 12px; margin-bottom: 25px; border-left: 6px solid #f1c40f;'>" +
            "        <h2 style='color: #2d3436; margin-top: 0;'>🧠 Fun Memorization Tricks</h2>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #d35400;'>🎭 Code Storytelling:</b> Imagine your variables are characters. <i>\"The variable 'x' is a courier carrying a message through the loop forest...\"</i>" +
            "        </div>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #d35400;'>🦆 Rubber Ducking:</b> Explain your code's logic to an object (like a duck). If you can teach it, you won't forget it!" +
            "        </div>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #d35400;'>✍️ The 5x Rule:</b> Write a difficult line or logic 5 times by hand. Muscle memory is a real thing for developers." +
            "        </div>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #d35400;'>🎨 Visual Mind-Mapping:</b> Draw your logic flow using colors and shapes. Your brain remembers pictures better than text." +
            "        </div>" +
            "    </div>" +
            "    " +
            "    <div style='text-align: center; color: #b2bec3; font-size: 12px; margin-top: 40px; border-top: 1px solid #dfe6e9; padding-top: 20px;'>" +
            "        \"The best way to predict the future is to invent it.\" — Alan Kay" +
            "    </div>" +
            "</body>" +
            "</html>";
            
        content.setText(htmlContent);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
}
