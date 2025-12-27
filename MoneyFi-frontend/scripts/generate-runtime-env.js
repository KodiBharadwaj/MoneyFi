const fs = require('fs');
const path = require('path');

const env = {
  BASE_URL: process.env.BASE_URL,
  GOOGLE_CLIENT_ID: process.env.GOOGLE_CLIENT_ID,
  GITHUB_CLIENT_ID: process.env.GITHUB_CLIENT_ID,
  GEMINI_API_KEY: process.env.GEMINI_API_KEY,
};

if (!env.BASE_URL) {
  process.exit(1);
}

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
