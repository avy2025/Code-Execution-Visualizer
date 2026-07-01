package visualizer.learning.api;

import visualizer.learning.api.dto.AnalyzeSessionRequest;
import visualizer.learning.api.dto.ExplainStepRequest;
import visualizer.learning.api.dto.GenerateHintRequest;
import visualizer.learning.api.dto.GenerateQuizRequest;
import visualizer.learning.api.dto.StepContextDto;

import java.util.List;
import java.util.Map;

/**
 * Lightweight manual JSON serialization and parsing for learning API DTOs.
 * Avoids external JSON library dependencies.
 */
final class LearningApiJson {

    private LearningApiJson() {
    }

    static String toJson(ExplainStepRequest request) {
        StringBuilder json = new StringBuilder(256);
        json.append('{');
        appendField(json, "sessionId", request.getSessionId());
        appendField(json, "sourceCode", request.getSourceCode());
        appendField(json, "language", request.getLanguage());
        appendField(json, "purpose", request.getPurpose().name());
        appendField(json, "step", stepContext(request.getStep()));
        appendField(json, "totalSteps", request.getTotalSteps());
        appendLastField(json, "variableCount", request.getVariableCount());
        json.append('}');
        return json.toString();
    }

    static String toJson(GenerateHintRequest request) {
        StringBuilder json = new StringBuilder(192);
        json.append('{');
        appendField(json, "sessionId", request.getSessionId());
        appendField(json, "step", stepContext(request.getStep()));
        appendField(json, "hintLevel", request.getHintLevel());
        appendField(json, "idleMillis", request.getIdleMillis());
        appendLastField(json, "proactive", request.isProactive());
        json.append('}');
        return json.toString();
    }

    static String toJson(GenerateQuizRequest request) {
        StringBuilder json = new StringBuilder(256);
        json.append('{');
        appendField(json, "sessionId", request.getSessionId());
        appendField(json, "sourceCode", request.getSourceCode());
        appendField(json, "parsedLines", stringArray(request.getParsedLines()));
        appendField(json, "language", request.getLanguage());
        appendLastField(json, "quizType", request.getQuizType().name());
        json.append('}');
        return json.toString();
    }

    static String toJson(AnalyzeSessionRequest request) {
        StringBuilder json = new StringBuilder(192);
        json.append('{');
        appendField(json, "sessionId", request.getSessionId());
        appendField(json, "metrics", metricsObject(request.getMetrics()));
        appendField(json, "totalSteps", request.getTotalSteps());
        appendLastField(json, "variableCount", request.getVariableCount());
        json.append('}');
        return json.toString();
    }

    static String extractDataObject(String responseBody) {
        int dataIndex = responseBody.indexOf("\"data\"");
        if (dataIndex < 0) {
            throw new IllegalArgumentException("Missing data field in response");
        }
        int start = responseBody.indexOf('{', dataIndex);
        if (start < 0) {
            throw new IllegalArgumentException("Missing data object in response");
        }
        return extractBalanced(responseBody, start);
    }

    static String getString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return "";
        }
        int colon = json.indexOf(':', keyIndex + pattern.length());
        if (colon < 0) {
            return "";
        }
        int valueStart = colon + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length()) {
            return "";
        }
        if (json.charAt(valueStart) == '"') {
            return readQuoted(json, valueStart + 1);
        }
        return "";
    }

    static int getInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return 0;
        }
        int colon = json.indexOf(':', keyIndex + pattern.length());
        if (colon < 0) {
            return 0;
        }
        int valueStart = colon + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        int valueEnd = valueStart;
        while (valueEnd < json.length() && "-0123456789".indexOf(json.charAt(valueEnd)) >= 0) {
            valueEnd++;
        }
        if (valueEnd == valueStart) {
            return 0;
        }
        return Integer.parseInt(json.substring(valueStart, valueEnd));
    }

    static boolean getBoolean(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return false;
        }
        int colon = json.indexOf(':', keyIndex + pattern.length());
        if (colon < 0) {
            return false;
        }
        return json.regionMatches(colon + 1, " true", 0, 5)
                || json.regionMatches(colon + 1, "true", 0, 4);
    }

    private static String stepContext(StepContextDto step) {
        StringBuilder json = new StringBuilder(128);
        json.append('{');
        appendField(json, "pc", step.getPc());
        appendField(json, "line", step.getLine());
        appendField(json, "phase", step.getPhase());
        appendField(json, "variables", integerMap(step.getVariables()));
        if (step.getErrorMessage() != null) {
            appendLastField(json, "errorMessage", step.getErrorMessage());
        } else {
            appendLastNullField(json, "errorMessage");
        }
        json.append('}');
        return json.toString();
    }

    private static String integerMap(Map<String, Integer> variables) {
        StringBuilder json = new StringBuilder(64);
        json.append('{');
        boolean first = true;
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(entry.getKey())).append("\":");
            json.append(entry.getValue());
        }
        json.append('}');
        return json.toString();
    }

    private static String metricsObject(Map<String, Object> metrics) {
        StringBuilder json = new StringBuilder(96);
        json.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof Number) {
                json.append(value);
            } else {
                json.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        json.append('}');
        return json.toString();
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder(64);
        json.append('[');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append('"').append(escape(values.get(i))).append('"');
        }
        json.append(']');
        return json.toString();
    }

    private static void appendField(StringBuilder json, String key, String value) {
        json.append('"').append(escape(key)).append("\":");
        if (value.startsWith("{") || value.startsWith("[")) {
            json.append(value).append(',');
        } else {
            json.append('"').append(escape(value)).append('"').append(',');
        }
    }

    private static void appendField(StringBuilder json, String key, int value) {
        json.append('"').append(key).append("\":").append(value).append(',');
    }

    private static void appendField(StringBuilder json, String key, long value) {
        json.append('"').append(key).append("\":").append(value).append(',');
    }

    private static void appendField(StringBuilder json, String key, boolean value) {
        json.append('"').append(key).append("\":").append(value).append(',');
    }

    private static void appendLastField(StringBuilder json, String key, String value) {
        json.append('"').append(escape(key)).append("\":");
        json.append('"').append(escape(value)).append('"');
    }

    private static void appendLastField(StringBuilder json, String key, int value) {
        json.append('"').append(key).append("\":").append(value);
    }

    private static void appendLastField(StringBuilder json, String key, boolean value) {
        json.append('"').append(key).append("\":").append(value);
    }

    private static void appendLastNullField(StringBuilder json, String key) {
        json.append('"').append(key).append("\":null");
    }

    private static String readQuoted(String json, int start) {
        StringBuilder value = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                value.append(next);
                i++;
                continue;
            }
            if (ch == '"') {
                break;
            }
            value.append(ch);
        }
        return value.toString();
    }

    private static String extractBalanced(String json, int start) {
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return json.substring(start, i + 1);
                }
            }
        }
        throw new IllegalArgumentException("Unbalanced JSON object");
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
