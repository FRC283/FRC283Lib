::This bat file starts up an executable jar file found inside this folder to start a communication with a PhantomDriver on a robot
::To run this inside eclipse, open with system editor

@echo off

::Switch the working directory to the directory of this .bat file
::Explanation ->
::/d switch drive as well (if necessary)
::%0 is the full path and name of the batch file
::~ is modifiers. The modifiers are 'd' use drive, and 'p', path only
::\.. is the folder above that
::\src is the source folder of the project
cd /d %~dp0\..\src

::Note that the ; is the path separator for windows on these commands

::Compile the relevant files. Include the paths to all needed dependencies on the classpath
::javac -classpath .;"C:\Users\Benjamin\wpilib\java\current\lib\ntcore.jar";"C:\Users\Benjamin\wpilib\java\current\lib\wpilib.jar";"C:\Users\Benjamin\wpilib\java\current\lib\wpiutil.jar" ".\frc283\lib\term\PhantomTerminal.java"
javac -classpath .;"C:\Users\Benjamin\wpilib\java\current\lib\*" ".\frc283\lib\term\PhantomTerminal.java"

::Execute PhantomTerminal with the classpath as the src folder and the ntcore jar
::java -classpath .;"C:\Users\Benjamin\wpilib\java\current\lib\ntcore.jar";"C:\Users\Benjamin\wpilib\java\current\lib\wpilib.jar";"C:\Users\Benjamin\wpilib\java\current\lib\wpiutil.jar" frc283.lib.term.PhantomTerminal
java -classpath .;"C:\Users\Benjamin\wpilib\java\current\lib\*" frc283.lib.term.PhantomTerminal

pause