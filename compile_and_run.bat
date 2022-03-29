@echo off
javac -d .\bin -cp .;.\lib\json.jar .\src\main\java\com\raverbury\cmp\*.java
cd bin
java -cp .;.\..\lib\json.jar com.raverbury.cmp.CustomMusicPlayer
cd ..