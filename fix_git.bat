@echo off
echo STARTING FIX > fix_log.txt
rmdir /s /q "Remote-Command-Execution\.git" >> fix_log.txt 2>&1
git rm --cached Remote-Command-Execution >> fix_log.txt 2>&1
git add . >> fix_log.txt 2>&1
git commit --amend -m "project completed !" >> fix_log.txt 2>&1
git push -u origin main --force >> fix_log.txt 2>&1
echo DONE FIX >> fix_log.txt
