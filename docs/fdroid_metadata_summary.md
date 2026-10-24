# KFZ-A F-Droid-Metadaten

## Version 1.0.5 / VersionCode 6

Die Release-Metadaten für Version 1.0.5 verwenden den später anzulegenden Tag `v1.0.5` als Build-Referenz. Die Changelogs beschreiben ausschließlich den neuen österreichischen Landschafts-Header, den geschwungenen Übergang zwischen Header und Inhalt sowie die verbesserte Anpassung an unterschiedliche Bildschirmgrößen.

Die beiden unabhängigen unsignierten Clean-Release-APKs wurden mit Java 21 im Clone gebaut. Beide sind 9,803,442 Bytes groß und haben den identischen SHA-256-Hash `8E0861374643E8C157F1933BA772B6F0D7B7052A297273D41904713541CCDE31`.

## Metadaten

Die vollständigen deutschen und englischen F-Droid-Metadaten liegen unter:

- `fastlane/metadata/android/de-DE/`
- `fastlane/metadata/android/en-US/`

Beide Sprachverzeichnisse enthalten Titel, Kurzbeschreibung, Vollbeschreibung, den Changelog für VersionCode `1`, ein `512 × 512`-Pixel-Icon sowie den finalen Screenshot unter `images/phoneScreenshots/1.png`.

## Build-Verifizierung

- Java: `D:\Android Studio\jbr`
- Version: OpenJDK `21.0.8`
- Paket: `de.herk.kfza`
- versionName: `1.0`
- versionCode: `1`
- minSdk: `26`
- targetSdk: `36`
- `clean`: erfolgreich
- `:app:testDebugUnitTest`: erfolgreich
- `:app:assembleDebug`: erfolgreich
- `:app:assembleRelease`: erfolgreich
- Release-APK: `app/build/outputs/apk/release/app-release-unsigned.apk`
- Signierung: unsigniert; es sind keine Signaturdaten konfiguriert
- Zwei Clean-Release-Builds: identischer SHA-256-Hash

## Inhaltsprüfung

Die Textdateien sind UTF-8-kodiert. Die Metadaten enthalten keine privaten Namen oder E-Mail-Adressen oder unbeabsichtigte projektspezifische Verweise. Die englischen Metadaten enthalten keine unbeabsichtigten deutschsprachigen Verweise; deutsche Inhalte sind auf das erforderliche `de-DE`-Metadatenverzeichnis beschränkt. Die Lizenzangabe ist MIT.
