// =============================================
// BetCenterNL — AUDIO ENGINE (Web Audio API)
// Nessun file esterno richiesto — tutto generato
// =============================================

const AudioEngine = (() => {
  let ctx = null;
  let enabled = true;

  function getCtx() {
    if (!ctx) {
      try { ctx = new (window.AudioContext || window.webkitAudioContext)(); } catch(e) {}
    }
    // Resume if suspended (browser autoplay policy)
    if (ctx && ctx.state === 'suspended') ctx.resume();
    return ctx;
  }

  function tone(freq, type, duration, volume = 0.15, delay = 0) {
    if (!enabled) return;
    const ac = getCtx();
    if (!ac) return;
    try {
      const osc = ac.createOscillator();
      const gain = ac.createGain();
      osc.connect(gain);
      gain.connect(ac.destination);
      osc.type = type;
      osc.frequency.setValueAtTime(freq, ac.currentTime + delay);
      gain.gain.setValueAtTime(0, ac.currentTime + delay);
      gain.gain.linearRampToValueAtTime(volume, ac.currentTime + delay + 0.01);
      gain.gain.exponentialRampToValueAtTime(0.001, ac.currentTime + delay + duration);
      osc.start(ac.currentTime + delay);
      osc.stop(ac.currentTime + delay + duration + 0.05);
    } catch(e) {}
  }

  function noise(duration, volume = 0.06, delay = 0) {
    if (!enabled) return;
    const ac = getCtx();
    if (!ac) return;
    try {
      const bufSize = ac.sampleRate * duration;
      const buf = ac.createBuffer(1, bufSize, ac.sampleRate);
      const data = buf.getChannelData(0);
      for (let i = 0; i < bufSize; i++) data[i] = (Math.random() * 2 - 1);
      const src = ac.createBufferSource();
      src.buffer = buf;
      const gain = ac.createGain();
      src.connect(gain);
      gain.connect(ac.destination);
      gain.gain.setValueAtTime(volume, ac.currentTime + delay);
      gain.gain.exponentialRampToValueAtTime(0.001, ac.currentTime + delay + duration);
      src.start(ac.currentTime + delay);
    } catch(e) {}
  }

  // ── SOUND EFFECTS ──

  const sounds = {
    click() {
      tone(800, 'sine', 0.04, 0.08);
    },

    chip() {
      tone(600, 'triangle', 0.06, 0.1);
      tone(900, 'triangle', 0.04, 0.06, 0.03);
    },

    cardDeal() {
      noise(0.08, 0.12);
      tone(400, 'triangle', 0.05, 0.05, 0.02);
    },

    win() {
      // Ascending arpeggio
      [523, 659, 784, 1047].forEach((f, i) => {
        tone(f, 'sine', 0.15, 0.12, i * 0.07);
      });
    },

    bigWin() {
      // Full fanfare
      [523, 659, 784, 1047, 1319, 1568].forEach((f, i) => {
        tone(f, 'sine', 0.2, 0.15, i * 0.06);
      });
      setTimeout(() => {
        [1047, 1319, 1568, 2093].forEach((f, i) => {
          tone(f, 'sine', 0.25, 0.12, i * 0.05);
        });
      }, 400);
    },

    lose() {
      tone(300, 'sawtooth', 0.15, 0.1);
      tone(220, 'sawtooth', 0.2, 0.08, 0.1);
      tone(180, 'sawtooth', 0.25, 0.06, 0.2);
    },

    spin() {
      // Slot machine whirr
      for (let i = 0; i < 8; i++) {
        tone(200 + i * 30, 'sawtooth', 0.04, 0.04, i * 0.05);
      }
    },

    slotStop() {
      noise(0.06, 0.1);
      tone(500, 'square', 0.04, 0.06, 0.02);
    },

    jackpot() {
      // Epic jackpot sound
      const notes = [523,659,784,1047,1319,784,1047,1319,1568,2093];
      notes.forEach((f, i) => tone(f, 'sine', 0.3, 0.18, i * 0.08));
    },

    rouletteSpin() {
      // Ball rolling: descending wobble
      for (let i = 0; i < 20; i++) {
        const freq = 800 - i * 20;
        tone(freq, 'sine', 0.03, 0.04, i * 0.06);
      }
    },

    rouletteStop() {
      noise(0.1, 0.15);
      tone(440, 'triangle', 0.1, 0.1, 0.05);
    },

    diceRoll() {
      for (let i = 0; i < 6; i++) {
        noise(0.04, 0.08, i * 0.08);
        tone(150 + Math.random() * 100, 'square', 0.03, 0.04, i * 0.08 + 0.01);
      }
    },

    cashOut() {
      [784, 1047, 1319].forEach((f, i) => tone(f, 'sine', 0.12, 0.12, i * 0.06));
    },

    crash() {
      tone(200, 'sawtooth', 0.5, 0.15);
      tone(150, 'sawtooth', 0.4, 0.1, 0.1);
      noise(0.3, 0.12, 0.05);
    },

    mineExplosion() {
      noise(0.4, 0.18);
      tone(80, 'sawtooth', 0.5, 0.12);
      tone(60, 'sawtooth', 0.6, 0.08, 0.1);
    },

    gemReveal() {
      tone(1047, 'sine', 0.08, 0.1);
      tone(1319, 'sine', 0.06, 0.08, 0.04);
    },

    buttonHover() {
      tone(1200, 'sine', 0.02, 0.03);
    },

    notification() {
      tone(880, 'sine', 0.08, 0.1);
      tone(1100, 'sine', 0.06, 0.08, 0.1);
    },

    levelUp() {
      [523,659,784,1047,1319,1568,2093].forEach((f, i) =>
        tone(f, 'sine', 0.2, 0.15, i * 0.06)
      );
    },

    betPlaced() {
      tone(700, 'triangle', 0.06, 0.09);
      tone(900, 'triangle', 0.04, 0.07, 0.04);
    },

    countdown() {
      tone(440, 'sine', 0.1, 0.12);
    },

    countdownGo() {
      tone(880, 'sine', 0.15, 0.15);
      tone(1100, 'sine', 0.1, 0.12, 0.08);
    }
  };

  function toggle() {
    enabled = !enabled;
    const btn = document.getElementById('audio-toggle');
    if (btn) btn.textContent = enabled ? '🔊' : '🔇';
    showToast(enabled ? '🔊 Audio attivato' : '🔇 Audio disattivato', 'info', 1500);
  }

  function play(name) {
    if (sounds[name]) sounds[name]();
  }

  // Unlock audio context on first interaction
  document.addEventListener('click', () => { getCtx(); }, { once: true });

  return { play, toggle, isEnabled: () => enabled };
})();
