const { body } = require('express-validator');

const stepContextRules = [
  body('step').isObject().withMessage('step must be an object'),
  body('step.pc').isInt({ min: 0 }).withMessage('step.pc must be a non-negative integer'),
  body('step.line').isString().withMessage('step.line must be a string'),
  body('step.phase')
    .isIn(['STEP_START', 'STEP_END', 'ERROR', 'SESSION_COMPLETE'])
    .withMessage('step.phase is invalid'),
  body('step.variables').isObject().withMessage('step.variables must be an object'),
  body('step.errorMessage').optional({ nullable: true }).isString(),
];

const explainValidation = [
  body('sessionId').isString().notEmpty().withMessage('sessionId is required'),
  body('sourceCode').isString().withMessage('sourceCode must be a string'),
  body('language').isIn(['JAVA', 'PYTHON']).withMessage('language must be JAVA or PYTHON'),
  body('purpose')
    .isIn(['STEP_START', 'STEP_END', 'ERROR', 'SESSION_SUMMARY'])
    .withMessage('purpose is invalid'),
  ...stepContextRules,
  body('totalSteps').optional().isInt({ min: 0 }),
  body('variableCount').optional().isInt({ min: 0 }),
];

const hintValidation = [
  body('sessionId').isString().notEmpty().withMessage('sessionId is required'),
  ...stepContextRules,
  body('hintLevel')
    .isIn(['NUDGE', 'GUIDE', 'REVEAL'])
    .withMessage('hintLevel must be NUDGE, GUIDE, or REVEAL'),
  body('idleMillis').optional().isInt({ min: 0 }),
  body('proactive').optional().isBoolean(),
];

const quizValidation = [
  body('sessionId').isString().notEmpty().withMessage('sessionId is required'),
  body('sourceCode').isString().withMessage('sourceCode must be a string'),
  body('parsedLines').isArray().withMessage('parsedLines must be an array'),
  body('parsedLines.*').isString().withMessage('each parsed line must be a string'),
  body('language').isIn(['JAVA', 'PYTHON']).withMessage('language must be JAVA or PYTHON'),
  body('quizType')
    .isIn(['PREVIEW', 'POST_RUN'])
    .withMessage('quizType must be PREVIEW or POST_RUN'),
];

const analyzeValidation = [
  body('sessionId').isString().notEmpty().withMessage('sessionId is required'),
  body('metrics').isObject().withMessage('metrics must be an object'),
  body('totalSteps').isInt({ min: 0 }).withMessage('totalSteps must be a non-negative integer'),
  body('variableCount').isInt({ min: 0 }).withMessage('variableCount must be a non-negative integer'),
];

const sessionStartValidation = [
  body('sessionId').isString().notEmpty().withMessage('sessionId is required'),
  body('sourceCode').isString().withMessage('sourceCode must be a string'),
  body('language').isIn(['JAVA', 'PYTHON']).withMessage('language must be JAVA or PYTHON'),
  body('parsedLines').isArray().withMessage('parsedLines must be an array'),
  body('parsedLines.*').isString().withMessage('each parsed line must be a string'),
];

const sessionEndValidation = [
  body('sessionId').isString().notEmpty().withMessage('sessionId is required'),
  body('totalSteps').isInt({ min: 0 }).withMessage('totalSteps must be a non-negative integer'),
  body('variableCount').isInt({ min: 0 }).withMessage('variableCount must be a non-negative integer'),
  body('metrics').optional().isObject(),
];

module.exports = {
  explainValidation,
  hintValidation,
  quizValidation,
  analyzeValidation,
  sessionStartValidation,
  sessionEndValidation,
};
