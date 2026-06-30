const express = require('express');
const validate = require('../middleware/validate');
const {
  sessionStartValidation,
  sessionEndValidation,
} = require('../middleware/validators');
const { startSession, endSession } = require('../controllers/sessionController');

const router = express.Router();

/**
 * @openapi
 * /session/start:
 *   post:
 *     summary: Start a learning session
 *     tags: [Session]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [sessionId, sourceCode, language, parsedLines]
 *             properties:
 *               sessionId:
 *                 type: string
 *               sourceCode:
 *                 type: string
 *               language:
 *                 type: string
 *                 enum: [JAVA, PYTHON]
 *               parsedLines:
 *                 type: array
 *                 items:
 *                   type: string
 *     responses:
 *       200:
 *         description: Session started
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/SuccessResponse'
 */
router.post('/session/start', validate(sessionStartValidation), startSession);

/**
 * @openapi
 * /session/end:
 *   post:
 *     summary: End a learning session
 *     tags: [Session]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [sessionId, totalSteps, variableCount]
 *             properties:
 *               sessionId:
 *                 type: string
 *               totalSteps:
 *                 type: integer
 *               variableCount:
 *                 type: integer
 *               metrics:
 *                 type: object
 *     responses:
 *       200:
 *         description: Session ended
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/SuccessResponse'
 */
router.post('/session/end', validate(sessionEndValidation), endSession);

module.exports = router;
