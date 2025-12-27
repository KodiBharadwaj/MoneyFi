const fs = require('fs');
const path = require('path');

const env = {
  BASE_URL: process.env.BASE_URL,
  GOOGLE_CLIENT_ID: process.env.GOOGLE_CLIENT_ID
};

if (!env.BASE_URL) {
  console.error('❌ BASE_URL is not defined in Netlify env vars');
  process.exit(1);
}

// ⚠️ IMPORTANT: your actual Angular output path
const outputDir = path.join(
  __dirname,
  '..',
  'dist',
  'finance-app',
  'browser',
  'assets',
  'env'
);

fs.mkdirSync(outputDir, { recursive: true });

const outputPath = path.join(outputDir, 'runtime-env.json');
fs.writeFileSync(outputPath, JSON.stringify(env, null, 2));

console.log('✅ runtime-env.json generated at:', outputPath);
