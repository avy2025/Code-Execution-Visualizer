const analyzeService = require('../services/analyzeService');
const { successResponse } = require('../utils/response');

/**
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
async function analyzeSession(req, res, next) {
  try {
    const data = await analyzeService.analyzeSession(req.body);
    res.status(200).json(successResponse(data));
  } catch (error) {
    next(error);
  }
}

module.exports = {
  analyzeSession,
};
