/**
 * Mock quiz service. Gemini integration will replace this later.
 */
class QuizService {
  /**
   * @param {object} payload - Validated quiz request body
   * @returns {Promise<object>}
   */
  async generateQuiz(payload) {
    return {
      quizId: `${payload.sessionId}-${payload.quizType.toLowerCase()}`,
      question: 'What will be the value of x after this line executes?',
      options: ['5', '10', '105', '0'],
      answerIndex: 2,
      explanation:
        'When the condition is true, x is increased by 100, so the final value is 105.',
      quizType: payload.quizType,
      sessionId: payload.sessionId,
    };
  }
}

module.exports = new QuizService();
