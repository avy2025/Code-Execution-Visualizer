/**
 * Mock hint service. Gemini integration will replace this later.
 */
class HintService {
  /**
   * @param {object} payload - Validated hint request body
   * @returns {Promise<object>}
   */
  async generateHint(payload) {
    return {
      text: 'Remember that variables must be initialized before use.',
      level: payload.hintLevel,
      sessionId: payload.sessionId,
      proactive: Boolean(payload.proactive),
    };
  }
}

module.exports = new HintService();
