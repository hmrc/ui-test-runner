const fs = require('fs');
const path = require('path');

const packageJsonPath = path.resolve(__dirname, 'package.json');
const manifestJsonPath = path.resolve(__dirname, 'src/manifest.json');

const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));

const manifestJson = JSON.parse(fs.readFileSync(manifestJsonPath, 'utf8'));

manifestJson.version = packageJson.version;

fs.writeFileSync(manifestJsonPath, JSON.stringify(manifestJson, null, 2), 'utf8');

console.log(`Updated manifest.json to version ${packageJson.version}`);