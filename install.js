const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

console.log('🔧 Installazione BetCenterNL...\n');

// Crea cartella data se non esiste
const dataDir = path.join(__dirname, 'BackEnd', 'data');
if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
    console.log('📁 Creata cartella data/');
}

// Installa dipendenze backend
console.log('📦 Installazione dipendenze backend...');
const backendInstall = spawn('npm', ['install'], {
    cwd: path.join(__dirname, 'BackEnd'),
    stdio: 'inherit',
    shell: true
});

backendInstall.on('close', (code) => {
    if (code === 0) {
        console.log('✅ Backend installato con successo!\n');
        
        // Installa dipendenze frontend
        console.log('📦 Installazione dipendenze frontend...');
        const frontendInstall = spawn('npm', ['install'], {
            cwd: path.join(__dirname, 'FrontEnd'),
            stdio: 'inherit',
            shell: true
        });
        
        frontendInstall.on('close', (code) => {
            if (code === 0) {
                console.log('✅ Frontend installato con successo!\n');
                console.log('🚀 Installazione completata!');
                console.log('\nPer avviare il progetto:');
                console.log('  npm start');
                console.log('\nPoi apri: http://localhost:3000');
            } else {
                console.error('❌ Errore installazione frontend');
            }
        });
    } else {
        console.error('❌ Errore installazione backend');
    }
});