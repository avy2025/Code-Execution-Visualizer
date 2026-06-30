const sessionService = require('../services/sessionService');
const { successResponse } = require('../utils/response');

/**
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
async function startSession(req, res, next) {
  try {
    const data = await sessionService.startSession(req.body);
    res.status(200).json(successResponse(data));
  } catch (error) {
    next(error);
  }
}

/**
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
async function endSession(req, res, next) {
  try {
    const data = await sessionService.endSession(req.body);
    res.status(200).json(successResponse(data));
  } catch (error) {
    next(error);
  }
}

module.exports = {
  startSession,
  endSession,
};
