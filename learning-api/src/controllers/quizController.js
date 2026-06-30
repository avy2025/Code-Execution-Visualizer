const quizService = require('../services/quizService');
const { successResponse } = require('../utils/response');

/**
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
async function generateQuiz(req, res, next) {
  try {
    const data = await quizService.generateQuiz(req.body);
    res.status(200).json(successResponse(data));
  } catch (error) {
    next(error);
  }
}

module.exports = {
  generateQuiz,
};
