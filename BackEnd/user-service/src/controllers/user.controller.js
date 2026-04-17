const User = require('../models/User');

const getUserById = async (req, res) => {
    try {
        const user = await User.findById(req.params.id);
        if (!user) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }
        // Return user object directly (not wrapped in success/data)
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
        // Return user object directly (not wrapped in success/data)
        res.json(user.toJSON());
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

const getUsersByRole = async (req, res) => {
    try {
        const users = await User.find({ role: req.params.role });
        // Return array of users directly (not wrapped in success/data)
        res.json(users.map(u => u.toJSON()));
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

module.exports = { getUserById, getUserByUsername, getUsersByRole };