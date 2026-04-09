@echo off
cmd /c "mvn install:install-file -Dfile=frankenjfx-1.0.jar -DgroupId=frankenjfx -DartifactId=frankenjfx -Dversion=1.0 -Dpackaging=jar"
cmd /c "mvn install:install-file -Dfile=frankenjfx-natives-win32-1.0.jar -DgroupId=frankenjfx -DartifactId=natives-win -Dversion=1.0 -Dpackaging=jar"
