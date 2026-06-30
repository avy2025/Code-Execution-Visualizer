/**
 * Mock explanation service. Gemini integration will replace this later.
 */
class ExplainService {
  /**
   * @param {object} payload - Validated explain request body
   * @returns {Promise<object>}
   */
  async explainStep(payload) {
    const pc = payload.purpose === 'SESSION_SUMMARY' ? -1 : payload.step.pc;

    return {
      text: 'A variable named x is created and initialized with value 5.',
      pc,
      purpose: payload.purpose,
      sessionId: payload.sessionId,
    };
  }
}

module.exports = new ExplainService();
