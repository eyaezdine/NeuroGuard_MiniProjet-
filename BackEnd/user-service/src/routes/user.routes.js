const express = require('express');
const router = express.Router();
const {
	getUserById,
	getUserByUsername,
	getUsersByRole,
	getAllUsers,
	getPatientsByCaregiver,
	isCaregiverAssignedToPatient,
} = require('../controllers/user.controller');

router.get('/', getAllUsers);
router.get('/username/:username', getUserByUsername);
router.get('/role/:role', getUsersByRole);
router.get('/caregiver/:caregiverId/patients', getPatientsByCaregiver);
router.get('/caregiver/:caregiverId/patients/:patientId/assigned', isCaregiverAssignedToPatient);
router.get('/:id', getUserById);

module.exports = router;