#!/bin/bash
echo "Compiling Java files..."
javac -d out src/main/Game.java
echo "Running DungeonGame..."
java -cp out src.main.Game
echo "Game closed."
