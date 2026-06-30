/**
 * Mock session analysis service. Gemini integration will replace this later.
 */
class AnalyzeService {
  /**
   * @param {object} payload - Validated analyze request body
   * @returns {Promise<object>}
   */
  async analyzeSession(payload) {
    return {
      sessionId: payload.sessionId,
      strengths: ['Clear variable initialization', 'Consistent stepping through the program'],
      weaknesses: ['Limited use of comments', 'No error-handling examples yet'],
      recommendation:
        'Practice predicting variable values before each step to strengthen mental modeling.',
      totalSteps: payload.totalSteps,
      variableCount: payload.variableCount,
    };
  }
}

module.exports = new AnalyzeService();
