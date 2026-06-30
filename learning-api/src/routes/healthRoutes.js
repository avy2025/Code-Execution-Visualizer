const express = require('express');
const { healthCheck } = require('../controllers/healthController');

const router = express.Router();

/**
 * @openapi
 * /health:
 *   get:
 *     summary: Health check
 *     tags: [System]
 *     servers:
 *       - url: http://localhost:8080
 *     responses:
 *       200:
 *         description: Service is running
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 status:
 *                   type: string
 *                   example: UP
 */
router.get('/health', healthCheck);

module.exports = router;
