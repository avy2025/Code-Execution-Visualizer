const hintService = require('../services/hintService');
const { successResponse } = require('../utils/response');

/**
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
async function generateHint(req, res, next) {
  try {
    const data = await hintService.generateHint(req.body);
    res.status(200).json(successResponse(data));
  } catch (error) {
    next(error);
  }
}

module.exports = {
  generateHint,
};
