const User = require('../models/User');

const getUserById = async (req, res) => {
    try {
        const user = await User.findById(req.params.id);
        if (!user) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }
        res.json({ success: true, data: user.toJSON() });
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
        res.json({ success: true, data: user.toJSON() });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const getUsersByRole = async (req, res) => {
    try {
        const users = await User.find({ role: req.params.role });
        res.json({ success: true, data: users.map(u => u.toJSON()) });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const getAllUsers = async (req, res) => {
    try {
        const users = await User.find({});
        res.json({ success: true, data: users.map(u => u.toJSON()) });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

module.exports = { getUserById, getUserByUsername, getUsersByRole, getAllUsers };