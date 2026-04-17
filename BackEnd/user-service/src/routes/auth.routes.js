const express = require('express');
const router = express.Router();
const { register, login } = require('../controllers/auth.controller');
const { registerRules, loginRules, handleValidationErrors } = require('../middleware/validate');

router.post('/register', registerRules, handleValidationErrors, register);
router.post('/login', loginRules, handleValidationErrors, login);

module.exports = router;