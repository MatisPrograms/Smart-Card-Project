﻿# Smart-Card-Project

Voici les étapes a faire pour pouvoir run le projet

## Pour le serveur de vérification: 
```
JAVA_HOME=/path/to/your/java/home
IDEA_RT_JAR=/path/to/idea_rt.jar
PROJECT_HOME=/path/to/Smart-Card-Project
M2_REPO=/path/to/.m2/repository

$JAVA_HOME/bin/java \
  -javaagent:$IDEA_RT_JAR=39381:/path/to/intellij/bin \
  -Dfile.encoding=UTF-8 \
  -Dsun.stdout.encoding=UTF-8 \
  -Dsun.stderr.encoding=UTF-8 \
  -classpath "$PROJECT_HOME/verification-server/target/classes:\
$PROJECT_HOME/java-card/target/classes:\
$M2_REPO/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:\
$M2_REPO/com/github/krraghavan/xeger/1.0.0-RELEASE/xeger-1.0.0-RELEASE.jar:\
$M2_REPO/dk/brics/automaton/automaton/1.11-8/automaton-1.11-8.jar:\
$M2_REPO/Egate/api/1/api-1.jar:\
$M2_REPO/Egate/captransf/1/captransf-1.jar:\
$M2_REPO/Egate/converter/1/converter-1.jar:\
$M2_REPO/Egate/makeijc/1/makeijc-1.jar:\
$M2_REPO/Egate/offcardverifier/1/offcardverifier-1.jar" \
  fr.polytech.unice.App --pin 6969
```

## Et pour distributeur :
```
JAVA_HOME=/path/to/your/java/home
IDEA_RT_JAR=/path/to/idea_rt.jar
PROJECT_HOME=/path/to/Smart-Card-Project
M2_REPO=/path/to/.m2/repository

$JAVA_HOME/bin/java \
  -javaagent:$IDEA_RT_JAR=33517:/path/to/intellij/bin \
  -Dfile.encoding=UTF-8 \
  -Dsun.stdout.encoding=UTF-8 \
  -Dsun.stderr.encoding=UTF-8 \
  -classpath "$PROJECT_HOME/vending-machine/target/classes:\
$PROJECT_HOME/verification-server/target/classes:\
$PROJECT_HOME/java-card/target/classes:\
$M2_REPO/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:\
$M2_REPO/com/github/krraghavan/xeger/1.0.0-RELEASE/xeger-1.0.0-RELEASE.jar:\
$M2_REPO/dk/brics/automaton/automaton/1.11-8/automaton-1.11-8.jar:\
$M2_REPO/Egate/api/1/api-1.jar:\
$M2_REPO/Egate/captransf/1/captransf-1.jar:\
$M2_REPO/Egate/converter/1/converter-1.jar:\
$M2_REPO/Egate/makeijc/1/makeijc-1.jar:\
$M2_REPO/Egate/offcardverifier/1/offcardverifier-1.jar" \
  fr.polytech.unice.App
```
