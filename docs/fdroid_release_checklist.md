# KFZ-A F-Droid-Release-Checkliste

## Build-Verifizierung

- [x] `JAVA_HOME` auf `D:\Android Studio\jbr` gesetzt
- [x] `java -version` bestätigt OpenJDK `21.0.8`
- [x] `./gradlew clean`
- [x] `./gradlew :app:testDebugUnitTest`
- [x] `./gradlew :app:assembleDebug`
- [x] `./gradlew :app:assembleRelease`
- [x] Zwei Clean-Release-Builds durchgeführt
- [x] SHA-256 der Release-APKs identisch
- [x] Release-APK ist `app/build/outputs/apk/release/app-release-unsigned.apk`
- [x] Release bleibt unsigniert; keine Schlüssel oder Secrets hinzugefügt

## Projekt- und Metadatenprüfung

- [x] Paket `de.herk.kfza`
- [x] versionName `1.0`
- [x] versionCode `1`
- [x] minSdk `26`
- [x] targetSdk `36`
- [x] MIT-Lizenz vorhanden
- [x] Deutsche und englische F-Droid-Metadaten vollständig
- [x] Changelog für VersionCode `1` vorhanden
- [x] Icons und Screenshot vorhanden und validiert
- [x] UTF-8 validiert
- [x] Keine privaten Namen oder E-Mail-Adressen
- [x] Keine unbeabsichtigten projektspezifischen Referenzen
- [x] Keine unbeabsichtigten deutschsprachigen Referenzen außerhalb der erforderlichen deutschen Metadaten
- [x] Keine Änderungen an Anwendungscode, Ressourcen, Datensätzen, Paketnamen oder Git-Konfiguration
- [x] Kein Commit und kein Push ausgeführt
