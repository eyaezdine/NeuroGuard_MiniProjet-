const User = require('../models/User');

const ensureNumericId = async (user) => {
    if (Number.isInteger(user.numericId)) {
        return user;
    }

    const lastUser = await User.findOne({}, { numericId: 1 }).sort({ numericId: -1 });
    user.numericId = (lastUser?.numericId || 0) + 1;
    await user.save({ validateBeforeSave: false });
    return user;
};

const ensureNumericIds = async (users) => {
    const missingUsers = users.filter((u) => !Number.isInteger(u.numericId));
    if (missingUsers.length === 0) {
        return users;
    }

    const lastUser = await User.findOne({}, { numericId: 1 }).sort({ numericId: -1 });
    let nextId = (lastUser?.numericId || 0) + 1;

    for (const user of missingUsers) {
        user.numericId = nextId;
        nextId += 1;
        await user.save({ validateBeforeSave: false });
    }

    return users;
};

const getUserById = async (req, res) => {
    try {
        const numericId = Number(req.params.id);
        const user = Number.isInteger(numericId)
            ? await User.findOne({ numericId })
            : await User.findById(req.params.id);

        if (!user) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }

        await ensureNumericId(user);
        res.json(user.toJSON());
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const getUserByUsername = async (req, res) => {
    try {
        // username is treated as email
        const user = await User.findOne({ email: req.params.username });
        if (!user) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }

        await ensureNumericId(user);
        res.json(user.toJSON());
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const getUsersByRole = async (req, res) => {
    try {
        const users = await User.find({ role: req.params.role });
        await ensureNumericIds(users);
        res.json(users.map((u) => u.toJSON()));
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const getAllUsers = async (req, res) => {
    try {
        const users = await User.find({});
        await ensureNumericIds(users);
        res.json({ success: true, data: users.map(u => u.toJSON()) });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const getPatientsByCaregiver = async (req, res) => {
    try {
        // Until a dedicated assignment module exists, caregivers can access all patients.
        const users = await User.find({ role: 'PATIENT' });
        await ensureNumericIds(users);
        res.json(users.map((u) => u.toJSON()));
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const isCaregiverAssignedToPatient = async (req, res) => {
    try {
        const { patientId } = req.params;
        const numericId = Number(patientId);
        const patient = Number.isInteger(numericId)
            ? await User.findOne({ numericId })
            : await User.findById(patientId);

        const assigned = !!patient && patient.role === 'PATIENT';
        res.json(assigned);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

module.exports = {
    getUserById,
    getUserByUsername,
    getUsersByRole,
    getAllUsers,
    getPatientsByCaregiver,
    isCaregiverAssignedToPatient,
};