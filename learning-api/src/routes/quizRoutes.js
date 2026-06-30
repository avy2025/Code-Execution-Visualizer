const express = require('express');
const validate = require('../middleware/validate');
const { quizValidation } = require('../middleware/validators');
const { generateQuiz } = require('../controllers/quizController');

const router = express.Router();

/**
 * @openapi
 * /quiz:
 *   post:
 *     summary: Generate a quiz question for the current session
 *     tags: [Learning]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [sessionId, sourceCode, parsedLines, language, quizType]
 *             properties:
 *               sessionId:
 *                 type: string
 *               sourceCode:
 *                 type: string
 *               parsedLines:
 *                 type: array
 *                 items:
 *                   type: string
 *               language:
 *                 type: string
 *                 enum: [JAVA, PYTHON]
 *               quizType:
 *                 type: string
 *                 enum: [PREVIEW, POST_RUN]
 *     responses:
 *       200:
 *         description: Mock quiz generated
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
router.post('/quiz', validate(quizValidation), generateQuiz);

module.exports = router;
