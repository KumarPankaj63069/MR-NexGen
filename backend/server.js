/**
 * MR NexGen IT Services — Production REST API Backend Server
 * Website: https://www.mrnexgen.com
 */

const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const sqlite3 = require('sqlite3').verbose();

const app = express();
const PORT = process.env.PORT || 8080;
const JWT_SECRET = process.env.JWT_SECRET || 'mrnexgen_secure_production_secret_2026';

app.use(cors());
app.use(express.json());

// Initialize SQLite database
const dbPath = path.resolve(__dirname, 'mrnexgen.db');
const db = new sqlite3.Database(dbPath);

// Execute schema
const schemaSql = fs.readFileSync(path.resolve(__dirname, 'schema.sql'), 'utf-8');
db.exec(schemaSql, (err) => {
  if (err) console.error("Database schema init error:", err);
  else console.log("Database initialized successfully.");
});

// Utility: Hash password
function hashPassword(password, salt) {
  return crypto.createHash('sha256').update(password + salt).digest('hex');
}

// Authentication Middleware
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  if (!token) return res.status(401).json({ success: false, message: 'Authorization token required' });

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ success: false, message: 'Invalid or expired token' });
    req.user = user;
    next();
  });
}

// Admin Authorization Middleware
function requireAdmin(req, res, next) {
  if (!req.user || req.user.role !== 'ADMIN') {
    return res.status(403).json({ success: false, message: 'Admin privileges required' });
  }
  next();
}

// --- HEALTH CHECK ---
app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    company: 'MR NexGen IT Services',
    website: 'https://www.mrnexgen.com',
    timestamp: new Date().toISOString()
  });
});

// --- AUTH ENDPOINTS ---
app.post('/api/auth/register', (req, res) => {
  const { name, email, phone, password, company, city } = req.body;
  if (!name || !email || !password || !phone) {
    return res.status(400).json({ success: false, message: 'Name, email, phone, and password are required' });
  }

  const normalizedEmail = email.trim().toLowerCase();
  db.get('SELECT id FROM users WHERE email = ?', [normalizedEmail], (err, existing) => {
    if (existing) {
      return res.status(409).json({ success: false, message: 'Email already registered' });
    }

    const salt = crypto.randomBytes(8).toString('hex');
    const passwordHash = hashPassword(password, salt);
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const expiry = Date.now() + 15 * 60 * 1000;
    const userId = 'usr_' + crypto.randomBytes(6).toString('hex');

    const stmt = db.prepare(`
      INSERT INTO users (id, name, email, phone, company, city, password_hash, salt, role, is_verified, is_active, verification_otp, otp_expiry)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'USER', 0, 1, ?, ?)
    `);

    stmt.run([userId, name, normalizedEmail, phone, company || '', city || '', passwordHash, salt, otp, expiry], function (err) {
      if (err) return res.status(500).json({ success: false, message: 'Registration failed: ' + err.message });
      console.log(`[SIMULATED EMAIL] To: ${normalizedEmail} | OTP: ${otp}`);
      res.status(201).json({
        success: true,
        message: 'Registration successful! Verification code sent to email.',
        data: { userId, email: normalizedEmail, demoOtp: otp }
      });
    });
  });
});

app.post('/api/auth/verify-email', (req, res) => {
  const { email, otp } = req.body;
  const normalizedEmail = (email || '').trim().toLowerCase();

  db.get('SELECT * FROM users WHERE email = ?', [normalizedEmail], (err, user) => {
    if (!user) return res.status(404).json({ success: false, message: 'User not found' });
    if (user.is_verified) return res.json({ success: true, message: 'Already verified' });
    if (user.verification_otp !== otp) return res.status(400).json({ success: false, message: 'Invalid verification OTP' });
    if (Date.now() > user.otp_expiry) return res.status(400).json({ success: false, message: 'Verification OTP expired' });

    db.run('UPDATE users SET is_verified = 1, verification_otp = NULL WHERE id = ?', [user.id], (err) => {
      if (err) return res.status(500).json({ success: false, message: 'Verification failed' });
      res.json({ success: true, message: 'Email verified successfully. You may now log in.' });
    });
  });
});

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  const normalizedEmail = (email || '').trim().toLowerCase();

  db.get('SELECT * FROM users WHERE email = ?', [normalizedEmail], (err, user) => {
    if (!user) return res.status(401).json({ success: false, message: 'Invalid credentials' });
    const computedHash = hashPassword(password, user.salt);
    if (computedHash !== user.password_hash) return res.status(401).json({ success: false, message: 'Invalid credentials' });
    if (!user.is_active) return res.status(403).json({ success: false, message: 'Account is deactivated' });
    if (!user.is_verified) return res.status(403).json({ success: false, message: 'Please verify your email before logging in' });

    const token = jwt.sign(
      { id: user.id, email: user.email, name: user.name, role: user.role },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.json({
      success: true,
      message: 'Login successful',
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        company: user.company,
        city: user.city,
        role: user.role
      }
    });
  });
});

// --- SERVICES ENDPOINTS ---
app.get('/api/services', (req, res) => {
  db.all('SELECT * FROM services WHERE is_published = 1 ORDER BY is_featured DESC, title ASC', (err, rows) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, data: rows });
  });
});

// --- SERVICE REQUESTS ---
app.post('/api/service-requests', authenticateToken, (req, res) => {
  const { serviceId, serviceTitle, projectTitle, description, budget, deadline } = req.body;
  const randomSuffix = Math.floor(10000 + Math.random() * 90000).toString();
  const requestId = `MRN-SR-2026-${randomSuffix}`;
  const id = 'req_' + crypto.randomBytes(6).toString('hex');

  const stmt = db.prepare(`
    INSERT INTO service_requests (id, request_id, user_id, user_name, user_email, user_phone, service_id, service_title, project_title, description, budget, deadline, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
  `);

  stmt.run([id, requestId, req.user.id, req.user.name, req.user.email, req.user.phone || '', serviceId, serviceTitle, projectTitle, description, budget, deadline], function(err) {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.status(201).json({
      success: true,
      message: 'Service request created successfully',
      data: { id, requestId, status: 'PENDING' }
    });
  });
});

app.get('/api/service-requests', authenticateToken, (req, res) => {
  db.all('SELECT * FROM service_requests WHERE user_id = ? ORDER BY created_at DESC', [req.user.id], (err, rows) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, data: rows });
  });
});

// --- PORTFOLIO ---
app.get('/api/portfolio', (req, res) => {
  db.all('SELECT * FROM portfolio WHERE is_published = 1 ORDER BY is_featured DESC, created_at DESC', (err, rows) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, data: rows });
  });
});

// --- BLOGS ---
app.get('/api/blogs', (req, res) => {
  db.all('SELECT * FROM blogs WHERE is_published = 1 ORDER BY published_at DESC', (err, rows) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, data: rows });
  });
});

// --- CONTACT ---
app.post('/api/contact', (req, res) => {
  const { name, email, phone, subject, message } = req.body;
  const msgNum = Math.floor(1000 + Math.random() * 9000).toString();
  const messageId = `MRN-MSG-${msgNum}`;
  const id = 'msg_' + crypto.randomBytes(6).toString('hex');

  db.run(`
    INSERT INTO contact_messages (id, message_id, name, email, phone, subject, message, status)
    VALUES (?, ?, ?, ?, ?, ?, ?, 'NEW')
  `, [id, messageId, name, email, phone, subject, message], function (err) {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.status(201).json({ success: true, message: 'Message submitted successfully', messageId });
  });
});

// --- ADMIN ENDPOINTS ---
app.get('/api/admin/requests', authenticateToken, requireAdmin, (req, res) => {
  db.all('SELECT * FROM service_requests ORDER BY created_at DESC', (err, rows) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, data: rows });
  });
});

app.put('/api/admin/requests/:id/status', authenticateToken, requireAdmin, (req, res) => {
  const { status, rejectionReason, adminNotes } = req.body;
  db.run(`
    UPDATE service_requests
    SET status = ?, rejection_reason = ?, admin_notes = ?, updated_at = CURRENT_TIMESTAMP
    WHERE id = ?
  `, [status, rejectionReason || '', adminNotes || '', req.params.id], function(err) {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, message: 'Request status updated' });
  });
});

// --- APPLICATION VERSION POLICY & REMOTE UPDATE ---
app.get('/api/app/version', (req, res) => {
  db.get('SELECT * FROM app_versions WHERE id = ?', ['current'], (err, row) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    if (!row) {
      return res.json({
        success: true,
        data: {
          latestVersion: "1.0.0",
          latestVersionCode: 1,
          minimumSupportedVersionCode: 1,
          minimumSupportedVersion: "1.0.0",
          updateUrl: "market://details?id=com.mrnexgen.app",
          playStoreWebUrl: "https://play.google.com/store/apps/details?id=com.mrnexgen.app",
          forceUpdate: false,
          releaseNotes: "Initial official release of MR NexGen"
        }
      });
    }
    res.json({
      success: true,
      data: {
        latestVersion: row.latest_version,
        latestVersionCode: row.latest_version_code,
        minimumSupportedVersionCode: row.minimum_supported_version_code,
        minimumSupportedVersion: row.minimum_supported_version,
        updateUrl: row.update_url,
        playStoreWebUrl: row.play_store_web_url,
        forceUpdate: Boolean(row.force_update),
        releaseNotes: row.release_notes
      }
    });
  });
});

app.put('/api/admin/app/version', authenticateToken, requireAdmin, (req, res) => {
  const { latestVersion, latestVersionCode, minimumSupportedVersionCode, minimumSupportedVersion, updateUrl, forceUpdate, releaseNotes } = req.body;
  db.run(`
    INSERT INTO app_versions (id, latest_version, latest_version_code, minimum_supported_version_code, minimum_supported_version, update_url, play_store_web_url, force_update, release_notes, updated_at)
    VALUES ('current', ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
    ON CONFLICT(id) DO UPDATE SET
      latest_version = excluded.latest_version,
      latest_version_code = excluded.latest_version_code,
      minimum_supported_version_code = excluded.minimum_supported_version_code,
      minimum_supported_version = excluded.minimum_supported_version,
      update_url = excluded.update_url,
      play_store_web_url = excluded.play_store_web_url,
      force_update = excluded.force_update,
      release_notes = excluded.release_notes,
      updated_at = CURRENT_TIMESTAMP
  `, [
    latestVersion || '1.0.0',
    latestVersionCode || 1,
    minimumSupportedVersionCode || 1,
    minimumSupportedVersion || '1.0.0',
    updateUrl || 'market://details?id=com.mrnexgen.app',
    'https://play.google.com/store/apps/details?id=com.mrnexgen.app',
    forceUpdate ? 1 : 0,
    releaseNotes || ''
  ], function (err) {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, message: 'App version policy updated successfully' });
  });
});

app.listen(PORT, () => {
  console.log(`MR NexGen IT Services REST API running on port ${PORT}`);
});
