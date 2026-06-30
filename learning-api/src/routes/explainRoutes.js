const express = require('express');
const validate = require('../middleware/validate');
const { explainValidation } = require('../middleware/validators');
const { explainStep } = require('../controllers/explainController');

const router = express.Router();

/**
 * @openapi
 * /explain:
 *   post:
 *     summary: Explain a code execution step
 *     tags: [Learning]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [sessionId, sourceCode, language, purpose, step]
 *             properties:
 *               sessionId:
 *                 type: string
 *               sourceCode:
 *                 type: string
 *               language:
 *                 type: string
 *                 enum: [JAVA, PYTHON]
 *               purpose:
 *                 type: string
 *                 enum: [STEP_START, STEP_END, ERROR, SESSION_SUMMARY]
 *               step:
 *                 $ref: '#/components/schemas/StepContext'
 *               totalSteps:
 *                 type: integer
 *               variableCount:
 *                 type: integer
 *     responses:
 *       200:
 *         description: Mock explanation generated
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *       400:
 *         description: Validation error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.post('/explain', validate(explainValidation), explainStep);

module.exports = router;
