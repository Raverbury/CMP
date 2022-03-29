@echo off
javac -d ./build ./*.java
cd ./build
jar cmf ./CustomMusicPlayer.mf ./CustomMusicPlayer.jar ./*
move ./CustomMusicPlayer.jar ./..