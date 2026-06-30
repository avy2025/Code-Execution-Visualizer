/**
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 */
function healthCheck(req, res) {
  res.status(200).json({
    status: 'UP',
  });
}

module.exports = {
  healthCheck,
};
