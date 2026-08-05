# KFZ-A F-Droid-Metadaten

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
