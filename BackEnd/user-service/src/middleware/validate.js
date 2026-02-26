const { body, validationResult } = require('express-validator');

const handleValidationErrors = (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({
            success: false,
            message: 'Validation failed',
            errors: errors.array().map((err) => ({
                field: err.path,
                message: err.msg,
            })),
        });
    }
    next();
};

const registerRules = [
    body('firstName').trim().notEmpty().withMessage('First name is required')
        .isLength({ min: 2, max: 50 }).withMessage('First name must be between 2 and 50 characters'),
    body('lastName').trim().notEmpty().withMessage('Last name is required')
        .isLength({ min: 2, max: 50 }).withMessage('Last name must be between 2 and 50 characters'),
    body('email').trim().notEmpty().withMessage('Email is required')
        .isEmail().withMessage('Please provide a valid email address').normalizeEmail(),
    body('password').notEmpty().withMessage('Password is required')
        .isLength({ min: 6 }).withMessage('Password must be at least 6 characters')
        .matches(/\d/).withMessage('Password must contain at least one number'),
    body('role').optional().isIn(['PATIENT', 'CAREGIVER', 'PROVIDER', 'ADMIN'])
        .withMessage('Role must be one of: PATIENT, CAREGIVER, PROVIDER, ADMIN'),
];

const loginRules = [
    body('email').trim().notEmpty().withMessage('Email is required')
        .isEmail().withMessage('Please provide a valid email address').normalizeEmail(),
    body('password').notEmpty().withMessage('Password is required'),
];

module.exports = { registerRules, loginRules, handleValidationErrors };