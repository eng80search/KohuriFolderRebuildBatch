@echo off

echo --batch-- Jarバッチファイルを実行します。
echo --batch-- startTime:  %date% %time%

REM  Java実行時には文字コードはUTF-8を使用する。
java -jar ./KohuriFolderRebuildBatch.jar

echo --batch-- endTime： %date% %time%
echo --batch-- Jarバッチファイルが終了しました。
pause > nul
exit
