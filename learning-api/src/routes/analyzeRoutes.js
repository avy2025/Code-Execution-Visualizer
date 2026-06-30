const express = require('express');
const validate = require('../middleware/validate');
const { analyzeValidation } = require('../middleware/validators');
const { analyzeSession } = require('../controllers/analyzeController');

const router = express.Router();

/**
 * @openapi
 * /analyze:
 *   post:
 *     summary: Analyze a completed learning session
 *     tags: [Learning]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [sessionId, metrics, totalSteps, variableCount]
 *             properties:
 *               sessionId:
 *                 type: string
 *               metrics:
 *                 type: object
 *               totalSteps:
 *                 type: integer
 *               variableCount:
 *                 type: integer
 *     responses:
 *       200:
 *         description: Mock session analysis generated
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/SuccessResponse'
 *       400:
 *         description: Validation error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.post('/analyze', validate(analyzeValidation), analyzeSession);

module.exports = router;
