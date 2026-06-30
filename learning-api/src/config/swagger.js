const swaggerJsdoc = require('swagger-jsdoc');
const config = require('./index');

const swaggerDefinition = {
  openapi: '3.0.3',
  info: {
    title: 'Learning API',
    version: '1.0.0',
    description:
      'REST backend for the Java Code Learning Platform. Currently returns deterministic mock responses; Gemini integration will be added later.',
  },
  servers: [
    {
      url: `http://localhost:${config.port}${config.apiPrefix}`,
      description: 'Local development server',
    },
  ],
  components: {
    schemas: {
      StepContext: {
        type: 'object',
        required: ['pc', 'line', 'phase', 'variables'],
        properties: {
          pc: { type: 'integer', example: 0 },
          line: { type: 'string', example: 'int x = 5;' },
          phase: {
            type: 'string',
            enum: ['STEP_START', 'STEP_END', 'ERROR', 'SESSION_COMPLETE'],
          },
          variables: {
            type: 'object',
            additionalProperties: { type: 'integer' },
            example: { x: 5 },
          },
          errorMessage: { type: 'string', nullable: true },
        },
      },
      SuccessResponse: {
        type: 'object',
        properties: {
          success: { type: 'boolean', example: true },
          timestamp: { type: 'string', format: 'date-time' },
          data: { type: 'object' },
        },
      },
      ErrorResponse: {
        type: 'object',
        properties: {
          success: { type: 'boolean', example: false },
          error: {
            type: 'object',
            properties: {
              code: { type: 'string' },
              message: { type: 'string' },
            },
          },
        },
      },
    },
  },
};

const options = {
  swaggerDefinition,
  apis: ['./src/routes/*.js'],
};

module.exports = swaggerJsdoc(options);
