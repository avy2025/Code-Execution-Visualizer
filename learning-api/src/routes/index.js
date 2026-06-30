const express = require('express');
const explainRoutes = require('./explainRoutes');
const hintRoutes = require('./hintRoutes');
const quizRoutes = require('./quizRoutes');
const analyzeRoutes = require('./analyzeRoutes');
const sessionRoutes = require('./sessionRoutes');

const router = express.Router();

router.use(explainRoutes);
router.use(hintRoutes);
router.use(quizRoutes);
router.use(analyzeRoutes);
router.use(sessionRoutes);

module.exports = router;
