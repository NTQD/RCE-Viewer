@echo off
echo STARTING > git_log.txt
git init >> git_log.txt 2>&1
git remote remove origin >> git_log.txt 2>&1
git remote add origin https://github.com/NTQD/RCE-Viewer.git >> git_log.txt 2>&1
git add . >> git_log.txt 2>&1
git commit -m "project completed !" >> git_log.txt 2>&1
git branch -M main >> git_log.txt 2>&1
git push -u origin main >> git_log.txt 2>&1
echo DONE >> git_log.txt
