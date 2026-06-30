/**
 * Application error with HTTP status and machine-readable code.
 */
class AppError extends Error {
  /**
   * @param {string} message - Human-readable message
   * @param {number} statusCode - HTTP status code
   * @param {string} code - Machine-readable error code
   */
  constructor(message, statusCode = 500, code = 'INTERNAL_ERROR') {
    super(message);
    this.statusCode = statusCode;
    this.code = code;
    this.isOperational = true;
  }
}

module.exports = {
  AppError,
};
