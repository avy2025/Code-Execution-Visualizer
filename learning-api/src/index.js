const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const swaggerUi = require('swagger-ui-express');

const config = require('./config');
const swaggerSpec = require('./config/swagger');
const apiRoutes = require('./routes');
const healthRoutes = require('./routes/healthRoutes');
const requestLogger = require('./middleware/requestLogger');
const { notFoundHandler, errorHandler } = require('./middleware/errorHandler');

const app = express();

app.use(helmet());
app.use(cors({ origin: config.corsOrigin }));
app.use(express.json({ limit: '1mb' }));
app.use(requestLogger);

app.use(healthRoutes);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));
app.use(config.apiPrefix, apiRoutes);

app.use(notFoundHandler);
app.use(errorHandler);

app.listen(config.port, () => {
  console.log(`Learning API listening on http://localhost:${config.port}`);
  console.log(`Swagger UI available at http://localhost:${config.port}/api-docs`);
  console.log(`Health check available at http://localhost:${config.port}/health`);
});

module.exports = app;
