const explainService = require('../services/explainService');
const { successResponse } = require('../utils/response');

/**
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
async function explainStep(req, res, next) {
  try {
    const data = await explainService.explainStep(req.body);
    res.status(200).json(successResponse(data));
  } catch (error) {
    next(error);
  }
}

module.exports = {
  explainStep,
};
