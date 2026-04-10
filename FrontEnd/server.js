/**
 * BetCenterNL — Server di sviluppo locale
 * Esegui: node server.js
 * Poi apri: http://localhost:3000
 */

const http = require('http');
const fs   = require('fs');
const path = require('path');

const PORT = 3000;

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css' : 'text/css; charset=utf-8',
  '.js'  : 'application/javascript; charset=utf-8',
  '.json': 'application/json',
  '.png' : 'image/png',
  '.jpg' : 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif' : 'image/gif',
  '.svg' : 'image/svg+xml',
  '.ico' : 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2':'font/woff2',
  '.ttf' : 'font/ttf',
};

const server = http.createServer((req, res) => {
  // CORS headers (permissive for local dev)
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Cache-Control', 'no-cache');

  let url = req.url.split('?')[0]; // strip query
  if (url === '/' || url === '') url = '/index.html';

  const filePath = path.join(__dirname, url);
  const ext = path.extname(filePath).toLowerCase();
  const mime = MIME[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, data) => {
    if (err) {
      if (err.code === 'ENOENT') {
        // SPA fallback — serve index.html for unknown paths
        fs.readFile(path.join(__dirname, 'index.html'), (err2, d2) => {
          if (err2) { res.writeHead(500); res.end('Server Error'); return; }
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          res.end(d2);
        });
      } else {
        res.writeHead(500);
        res.end('Server Error: ' + err.message);
      }
      return;
    }

    res.writeHead(200, { 'Content-Type': mime });
    res.end(data);
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log('\n╔══════════════════════════════════════╗');
  console.log('║        BetCenterNL — Server Locale     ║');
  console.log('╠══════════════════════════════════════╣');
  console.log(`║  🌐  http://localhost:${PORT}           ║`);
  console.log('║  ⌨️   Ctrl+C per fermare              ║');
  console.log('╚══════════════════════════════════════╝\n');
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error(`\n❌ Porta ${PORT} già in uso. Chiudi l'altra istanza e riprova.\n`);
  } else {
    console.error('Errore server:', err);
  }
  process.exit(1);
});
