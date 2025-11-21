#!/bin/bash
set -e

echo "=== Git Sync Helper ==="
echo "1. Adding all changes..."
git add .

echo "2. Committing changes..."
if ! git diff-index --quiet HEAD --; then
    git commit -m "WIP: Auto-sync $(date '+%Y-%m-%d %H:%M:%S')"
else
    echo "No changes to commit."
fi

echo "3. Pulling updates (rebase)..."
# Pulls changes from remote and reapplies your commits on top
git pull --rebase origin $(git branch --show-current)

echo "4. Pushing changes..."
git push origin $(git branch --show-current)

echo "=== Sync Complete ==="
