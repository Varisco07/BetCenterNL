const { spawn } = require('child_process');
const path = require('path');

console.log('🚀 Avvio BetCenterNL...\n');

// Avvia il backend API
console.log('📡 Avvio Backend API (porta 3001)...');
const backend = spawn('node', ['server.js'], {
    cwd: path.join(__dirname, 'BackEnd'),
    stdio: 'inherit'
});

// Avvia il frontend server
console.log('🌐 Avvio Frontend Server (porta 3000)...');
const frontend = spawn('node', ['server.js'], {
    cwd: path.join(__dirname, 'FrontEnd'),
    stdio: 'inherit'
});

// Gestione chiusura
process.on('SIGINT', () => {
    console.log('\n🛑 Chiusura server...');
    backend.kill();
    frontend.kill();
    process.exit();
});

backend.on('error', (err) => {
    console.error('❌ Errore Backend:', err.message);
});

frontend.on('error', (err) => {
    console.error('❌ Errore Frontend:', err.message);
});

console.log('\n✅ Server avviati!');
console.log('🌐 Frontend: http://localhost:3000');
console.log('📡 Backend API: http://localhost:3001');
console.log('\nPremi Ctrl+C per fermare i server.');