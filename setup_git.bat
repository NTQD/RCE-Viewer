@echo off
git init
git remote remove origin
git remote add origin https://github.com/NTQD/RCE-Viewer.git
git add .
git commit -m "project completed !"
git branch -M main
git push -u origin main
pause
