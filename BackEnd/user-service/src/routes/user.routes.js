const express = require('express');
const router = express.Router();
const { getUserById, getUserByUsername, getUsersByRole, getAllUsers } = require('../controllers/user.controller');

router.get('/', getAllUsers);
router.get('/:id', getUserById);
router.get('/username/:username', getUserByUsername);
router.get('/role/:role', getUsersByRole);

module.exports = router;