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
        setBackground(new Color(30, 30, 30));
        
        JEditorPane content = new JEditorPane();
        content.setEditable(false);
        content.setContentType("text/html");
        content.setBackground(new Color(30, 30, 30));
        
        String htmlContent = "<html>" +
            "<body style='font-family: \"Inter\", sans-serif; padding: 25px; color: #d4d4d4; background-color: #1e1e1e;'>" +
            "    <div style='text-align: center; margin-bottom: 30px;'>" +
            "        <h1 style='color: #ae0b05; margin-bottom: 5px; font-size: 28px;'>🎓 Code Mastery Hub</h1>" +
            "        <p style='color: #888888; font-size: 14px;'>Master the art of writing and remembering clean code.</p>" +
            "    </div>" +
            "    " +
            "    <div style='background-color: #252526; padding: 20px; border-radius: 12px; margin-bottom: 25px; border-left: 6px solid #ae0b05;'>" +
            "        <h2 style='color: #ffffff; margin-top: 0; font-size: 18px;'>🚀 How to Improve Your Code</h2>" +
            "        <ul style='list-style-type: none; padding-left: 0;'>" +
            "            <li style='margin-bottom: 15px;'><b>✨ Clean Naming:</b> Use descriptive names. <code>calculateTotal()</code> is better than <code>doIt()</code>.</li>" +
            "            <li style='margin-bottom: 15px;'><b>🧩 One Job:</b> Each function should do exactly one thing. If it's doing more, break it down!</li>" +
            "            <li style='margin-bottom: 15px;'><b>♻️ DRY Principle:</b> <i>Don't Repeat Yourself.</i> Reusable code is happy code.</li>" +
            "            <li style='margin-bottom: 15px;'><b>📝 Self-Documenting:</b> Aim for code that is so clear it doesn't need many comments.</li>" +
            "        </ul>" +
            "    </div>" +
            "    " +
            "    <div style='background-color: #2d2d30; padding: 20px; border-radius: 12px; margin-bottom: 25px; border-left: 6px solid #f1c40f;'>" +
            "        <h2 style='color: #ffffff; margin-top: 0; font-size: 18px;'>🧠 Fun Memorization Tricks</h2>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #f1c40f;'>🎭 Code Storytelling:</b> Imagine your variables are characters. <i>\"The variable 'x' is a courier carrying a message...\"</i>" +
            "        </div>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #f1c40f;'>🦆 Rubber Ducking:</b> Explain your code's logic to an object (like a duck). If you can teach it, you won't forget it!" +
            "        </div>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #f1c40f;'>✍️ The 5x Rule:</b> Write a difficult line or logic 5 times by hand. Muscle memory is real." +
            "        </div>" +
            "        <div style='margin-bottom: 15px;'>" +
            "            <b style='color: #f1c40f;'>🎨 Visual Mind-Mapping:</b> Draw your logic flow using colors and shapes. Your brain remembers pictures better." +
            "        </div>" +
            "    </div>" +
            "    " +
            "    <div style='text-align: center; color: #555555; font-size: 12px; margin-top: 40px; border-top: 1px solid #333333; padding-top: 20px; font-style: italic;'>" +
            "        \"The best way to predict the future is to invent it.\" — Alan Kay" +
            "    </div>" +
            "</body>" +
            "</html>";
             
        content.setText(htmlContent);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        add(scrollPane, BorderLayout.CENTER);
    }
}
