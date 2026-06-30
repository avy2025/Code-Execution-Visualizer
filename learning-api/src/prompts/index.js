/**
 * Placeholder prompt templates for future Gemini integration.
 * These are not used by mock services yet.
 */
const explainPrompt = {
  system:
    'You are a patient programming tutor explaining code execution one step at a time to a beginner.',
  userTemplate:
    'Explain the following {{purpose}} step in {{language}}:\nLine: {{line}}\nVariables: {{variables}}',
};

const hintPrompt = {
  system:
    'Provide a short, progressive hint without giving the full answer unless the level is REVEAL.',
  userTemplate:
    'Hint level: {{hintLevel}}\nLine: {{line}}\nVariables: {{variables}}',
};

const quizPrompt = {
  system: 'Generate one multiple-choice question about the student code.',
  userTemplate: 'Language: {{language}}\nCode:\n{{sourceCode}}',
};

const analyzePrompt = {
  system: 'Summarize learning strengths, weaknesses, and one recommendation.',
  userTemplate: 'Session metrics: {{metrics}}',
};

module.exports = {
  explainPrompt,
  hintPrompt,
  quizPrompt,
  analyzePrompt,
};
