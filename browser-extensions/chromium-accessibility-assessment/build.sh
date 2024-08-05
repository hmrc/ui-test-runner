#!/bin/bash
export FORCE_COLOR=true
set -euxo pipefail
npm ci
npm test
npm run build
