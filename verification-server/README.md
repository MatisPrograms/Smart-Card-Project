Vous devez avez les variables d'environnement suivantes:
- $JAVA_HOME: le chemin vers le dossier d'installation de Java
- $PATH_M2_REPO: le chemin vers le dossier `.m2/repository`

Pour lancer le projet, exécutez la commande suivante depuis le dossier racine du projet:

```shell
  $JAVA_HOME/bin/java \ 
    -classpath verification-server/target/classes:java-card/target/classes:$PATH_M2_REPO/Egate/api/1/api-1.jar:$PATH_M2_REPO/Egate/captransf/1/captransf-1.jar:$PATH_M2_REPO/Egate/converter/1/converter-1.jar:$PATH_M2_REPO/Egate/makeijc/1/makeijc-1.jar:$PATH_M2_REPO/Egate/offcardverifier/1/offcardverifier-1.jar:$PATH_M2_REPO/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:$PATH_M2_REPO/com/github/krraghavan/xeger/1.0.0-RELEASE/xeger-1.0.0-RELEASE.jar:$PATH_M2_REPO/dk/brics/automaton/automaton/1.11-8/automaton-1.11-8.jar \
    fr.polytech.unice.App \
    --pin 6969
```