@echo off
echo Compilando proyecto...
javac -cp "lib/*;src" -d build/classes src/Project/Project.java src/Gui/interfazG.java src/Graphics/*.java src/Analyzer/*.java src/Environment/*.java src/Expression/*.java src/Instruction/*.java src/Types/*.java src/Utils/*.java src/Abstract/*.java src/Reports/*.java

echo Ejecutando aplicacion...
java -cp "lib/*;build/classes" Project.Project

pause
