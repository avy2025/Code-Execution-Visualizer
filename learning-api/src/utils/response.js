/**
 * Builds a standard success response envelope.
 * @param {object} data - Response payload
 * @returns {object}
 */
function successResponse(data) {
  return {
    success: true,
    timestamp: new Date().toISOString(),
    data,
  };
}

module.exports = {
  successResponse,
};
