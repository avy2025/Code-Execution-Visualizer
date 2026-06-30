const express = require('express');
const validate = require('../middleware/validate');
const { hintValidation } = require('../middleware/validators');
const { generateHint } = require('../controllers/hintController');

const router = express.Router();

/**
 * @openapi
 * /hint:
 *   post:
 *     summary: Generate a contextual learning hint
 *     tags: [Learning]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [sessionId, step, hintLevel]
 *             properties:
 *               sessionId:
 *                 type: string
 *               step:
 *                 $ref: '#/components/schemas/StepContext'
 *               hintLevel:
 *                 type: string
 *                 enum: [NUDGE, GUIDE, REVEAL]
 *               idleMillis:
 *                 type: integer
 *               proactive:
 *                 type: boolean
 *     responses:
 *       200:
 *         description: Mock hint generated
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
router.post('/hint', validate(hintValidation), generateHint);

module.exports = router;
