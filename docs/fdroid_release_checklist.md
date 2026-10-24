# KFZ-A F-Droid-Release-Checkliste

## Version 1.0.5 / VersionCode 6

- Neue UI: österreichischer Landschafts-Header
- Neue UI: geschwungener Übergang zwischen Header und Inhalt
- Neue UI: verbesserte Anpassung an unterschiedliche Bildschirmgrößen
- F-Droid-Build-Referenz: `v1.0.5`
- Fastlane-Changelogs: `fastlane/metadata/android/de-DE/changelogs/6.txt` und `fastlane/metadata/android/en-US/changelogs/6.txt`

Die historischen Prüf- und Build-Ergebnisse der initialen Version bleiben unten erhalten.

## Ergebnis der Vorbereitung 1.0.5

- Header-Ressource als PNG geprüft und von `austrian_landscape_header.jpg` nach `austrian_landscape_header.png` korrigiert
- `:app:testDebugUnitTest`: erfolgreich
- `:app:lintDebug`: erfolgreich
- `:app:assembleDebug`: erfolgreich
- `:app:assembleRelease`: erfolgreich
- Java: `C:\Users\Mee\.jdks\ms-21.0.12.1` / OpenJDK `21.0.12.1`
- Emulatorprüfung: ausschließlich `emulator-5554`
- Light/Dark-Theme sowie 1080×2400 und 720×1600 geprüft
- Kennzeichensuche `EU` erfolgreich mit Ergebnis `BH Eisenstadt`, `Burgenland`, `District Authority`
- Zwei unabhängige Clean-Release-Builds im Clone erfolgreich
- Beide unsignierten APKs: 9,803,442 Bytes
- Beide SHA-256: `8E0861374643E8C157F1933BA772B6F0D7B7052A297273D41904713541CCDE31`
- Die beiden APKs sind byteidentisch
- Keine Signierung, kein Git-Befehl und keine Veröffentlichung durchgeführt

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
