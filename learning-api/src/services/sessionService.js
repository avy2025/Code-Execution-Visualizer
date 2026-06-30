/**
 * In-memory session tracking for mock session lifecycle endpoints.
 * No database is used.
 */
class SessionService {
  constructor() {
    /** @type {Map<string, object>} */
    this.sessions = new Map();
  }

  /**
   * @param {object} payload
   * @returns {Promise<object>}
   */
  async startSession(payload) {
    const record = {
      sessionId: payload.sessionId,
      sourceCode: payload.sourceCode,
      language: payload.language,
      parsedLines: payload.parsedLines,
      startedAt: new Date().toISOString(),
      endedAt: null,
      status: 'ACTIVE',
    };

    this.sessions.set(payload.sessionId, record);

    return {
      sessionId: payload.sessionId,
      status: 'ACTIVE',
      message: 'Learning session started.',
      lineCount: payload.parsedLines.length,
    };
  }

  /**
   * @param {object} payload
   * @returns {Promise<object>}
   */
  async endSession(payload) {
    const existing = this.sessions.get(payload.sessionId);
    const endedAt = new Date().toISOString();

    if (existing) {
      existing.status = 'ENDED';
      existing.endedAt = endedAt;
      existing.totalSteps = payload.totalSteps;
      existing.variableCount = payload.variableCount;
      existing.metrics = payload.metrics || {};
    } else {
      this.sessions.set(payload.sessionId, {
        sessionId: payload.sessionId,
        status: 'ENDED',
        startedAt: null,
        endedAt,
        totalSteps: payload.totalSteps,
        variableCount: payload.variableCount,
        metrics: payload.metrics || {},
      });
    }

    return {
      sessionId: payload.sessionId,
      status: 'ENDED',
      message: 'Learning session ended.',
      totalSteps: payload.totalSteps,
      variableCount: payload.variableCount,
    };
  }
}

module.exports = new SessionService();
