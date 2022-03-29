@echo off
javac -d .\bin -cp .;.\lib\json.jar .\src\main\java\com\raverbury\cmp\*.java
cd bin
jar cmf .\manifest.mf .\..\CustomMusicPlayer_2_0.jar .\com\raverbury\cmp\*.class
:: .\..\lib\json.jar
cd ..
