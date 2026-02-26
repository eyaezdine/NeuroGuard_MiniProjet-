require('dotenv').config();

const express = require('express');
const cors = require('cors');
const os = require('os');
const { Eureka } = require('eureka-js-client');
const connectDB = require('./src/config/db');
const authRoutes = require('./src/routes/auth.routes');
const userRoutes = require('./src/routes/user.routes');

const app = express();
const PORT = process.env.PORT || 3001;
let server;

// ─── Middleware ──────────────────────────────────────────────────────
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ─── Health check ───────────────────────────────────────────────────
app.get('/api/health', (req, res) => {
    res.status(200).json({
        success: true,
        service: 'user-service',
        status: 'running',
        timestamp: new Date().toISOString(),
    });
});

// ─── Routes ─────────────────────────────────────────────────────────
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);

// ─── 404 handler ────────────────────────────────────────────────────
app.use((req, res) => {
    res.status(404).json({
        success: false,
        message: `Route ${req.method} ${req.originalUrl} not found`,
    });
});

// ─── Global error handler ───────────────────────────────────────────
app.use((err, req, res, next) => {
    console.error('Unhandled error:', err);
    res.status(500).json({
        success: false,
        message: 'Internal server error',
    });
});

// ─── Start server ───────────────────────────────────────────────────
const startServer = async () => {
    try {
        await connectDB();
        server = app.listen(PORT, () => {
            console.log(`\n User Service running on http://localhost:${PORT}`);
            console.log(` Health check:       http://localhost:${PORT}/api/health`);
            console.log(`Register:           POST http://localhost:${PORT}/api/auth/register`);
            console.log(` Login:              POST http://localhost:${PORT}/api/auth/login\n`);
        });
    } catch (error) {
        console.error('Failed to start server:', error);
        process.exit(1);
    }
};

startServer();
// ---------------------------------------------------------------------
// Eureka Client Configuration
// ---------------------------------------------------------------------
const client = new Eureka({
  instance: {
    app: process.env.EUREKA_SERVICE_NAME || 'user-microservice',
    hostName: os.hostname(),
    ipAddr: '127.0.0.1', // or get from network interface
    statusPageUrl: `http://${process.env.EUREKA_INSTANCE_HOST}:${PORT}/info`, // optional health endpoint
    port: {
      $: PORT,
      '@enabled': true,
    },
    vipAddress: process.env.EUREKA_SERVICE_NAME || 'user-microservice',
    dataCenterInfo: {
      '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
      name: 'MyOwn',
    },
  },
  eureka: {
    host: process.env.EUREKA_HOST || 'localhost',
    port: process.env.EUREKA_PORT || 8761,
    servicePath: '/eureka/apps/',
    maxRetries: 5,
    requestRetryDelay: 2000,
  },
});

// Start Eureka client and register service
client.start((error) => {
  if (error) {
    console.error('❌ Failed to register with Eureka:', error.message);
  } else {
    console.log('✅ Registered with Eureka server');
  }
});

// Graceful shutdown: deregister from Eureka before exiting
const gracefulShutdown = () => {
  console.log('🛑 Shutting down gracefully...');
  client.stop((error) => {
    if (error) {
      console.error('❌ Error during Eureka deregistration:', error.message);
    }
    if (!server) {
      process.exit(0);
    }
    server.close(() => {
      console.log('👋 HTTP server closed');
      process.exit(0);
    });
  });
};

process.on('SIGTERM', gracefulShutdown);
process.on('SIGINT', gracefulShutdown);