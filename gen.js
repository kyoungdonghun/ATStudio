/**
 * gen.js — Generate dummy WAV audio files for ATStudio sample data.
 * Usage: node gen.js
 * Output: uploads/tracks/audio/ 디렉토리에 10개 WAV 파일 생성
 */
const fs = require('fs');
const path = require('path');

const outDir = path.join(__dirname, 'uploads', 'tracks', 'audio');
fs.mkdirSync(outDir, { recursive: true });

// Also create thumbnail dir
const thumbDir = path.join(__dirname, 'uploads', 'tracks', 'thumbnail');
fs.mkdirSync(thumbDir, { recursive: true });

const SAMPLE_RATE = 44100;
const NUM_CHANNELS = 1;
const BITS_PER_SAMPLE = 16;
const BYTE_RATE = SAMPLE_RATE * NUM_CHANNELS * (BITS_PER_SAMPLE / 8);
const BLOCK_ALIGN = NUM_CHANNELS * (BITS_PER_SAMPLE / 8);

function generateWav(durationSec, frequencyHz, filename) {
  const numSamples = SAMPLE_RATE * durationSec;
  const dataSize = numSamples * NUM_CHANNELS * (BITS_PER_SAMPLE / 8);
  const fileSize = 36 + dataSize;

  const buffer = Buffer.alloc(44 + dataSize);

  // RIFF header
  buffer.write('RIFF', 0);
  buffer.writeUInt32LE(fileSize, 4);
  buffer.write('WAVE', 8);

  // fmt chunk
  buffer.write('fmt ', 12);
  buffer.writeUInt32LE(16, 16);          // chunk size
  buffer.writeUInt16LE(1, 20);           // PCM
  buffer.writeUInt16LE(NUM_CHANNELS, 22);
  buffer.writeUInt32LE(SAMPLE_RATE, 24);
  buffer.writeUInt32LE(BYTE_RATE, 28);
  buffer.writeUInt16LE(BLOCK_ALIGN, 32);
  buffer.writeUInt16LE(BITS_PER_SAMPLE, 34);

  // data chunk
  buffer.write('data', 36);
  buffer.writeUInt32LE(dataSize, 40);

  // Generate sine wave samples
  const amplitude = 16000;
  for (let i = 0; i < numSamples; i++) {
    const sample = Math.round(amplitude * Math.sin(2 * Math.PI * frequencyHz * i / SAMPLE_RATE));
    buffer.writeInt16LE(sample, 44 + i * 2);
  }

  const filePath = path.join(outDir, filename);
  fs.writeFileSync(filePath, buffer);
  console.log(`Created: ${filePath} (${durationSec}s, ${frequencyHz}Hz, ${(buffer.length / 1024 / 1024).toFixed(1)}MB)`);
  return `uploads/tracks/audio/${filename}`;
}

// 10 tracks with varying durations and frequencies
const tracks = [
  { duration: 30,  freq: 440,  name: 'track01_piano_c.wav' },
  { duration: 45,  freq: 523,  name: 'track02_synth_high.wav' },
  { duration: 60,  freq: 330,  name: 'track03_bass_low.wav' },
  { duration: 90,  freq: 392,  name: 'track04_ambient_g.wav' },
  { duration: 120, freq: 261,  name: 'track05_lofi_mid.wav' },
  { duration: 30,  freq: 587,  name: 'track06_edm_d.wav' },
  { duration: 45,  freq: 349,  name: 'track07_acoustic_f.wav' },
  { duration: 180, freq: 466,  name: 'track08_cinematic_bb.wav' },
  { duration: 60,  freq: 293,  name: 'track09_hiphop_d.wav' },
  { duration: 150, freq: 415,  name: 'track10_rnb_ab.wav' },
];

console.log('Generating dummy WAV files...\n');
tracks.forEach(t => generateWav(t.duration, t.freq, t.name));
console.log('\nDone! Files are in: ' + outDir);
