const { validationResult } = require('express-validator');
const { AppError } = require('../utils/errors');

/**
 * Runs express-validator chains and forwards validation errors.
 * @param {import('express-validator').ValidationChain[]} validations
 * @returns {import('express').RequestHandler[]}
 */
function validate(validations) {
  return [
    ...validations,
    (req, res, next) => {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        const message = errors
          .array()
          .map((error) => error.msg)
          .join('; ');
        return next(new AppError(message, 400, 'VALIDATION_ERROR'));
      }
      return next();
    },
  ];
}

module.exports = validate;
