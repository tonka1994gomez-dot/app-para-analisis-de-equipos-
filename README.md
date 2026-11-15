# FOOTBALL INTELLIGENCE AI — Android

Proyecto Android nativo con Kotlin y Jetpack Compose. Interfaz oscura en español.

## Estado de esta versión (0.2.0)
- Consulta API-Football para recuperar los últimos cinco partidos disponibles de dos equipos.
- Calcula forma reciente, goles, probabilidades 1X2, más de 2.5 goles, ambos anotan y marcador probable mediante Poisson.
- Requiere una clave de API-Football introducida dentro de la app y guardada localmente.
- El sistema de licencias todavía no está implementado.

## Compilar en GitHub
Abre **Actions**, ejecuta **Compilar APK** y descarga el artefacto **app-debug-apk** cuando el proceso termine correctamente.

## Compilar en Android Studio
Abre la carpeta raíz, sincroniza Gradle y selecciona **Build > Build Bundle(s) / APK(s) > Build APK(s)**. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Precisión y seguridad
Las probabilidades son estimaciones, no garantías ni recomendaciones de apuesta. La disponibilidad de datos depende del plan y cobertura de API-Football. No se incluye ninguna clave privada en el APK.

## Próxima etapa
Queda pendiente implementar licencias permanentes, administración y revocación de códigos, y vinculación a un solo dispositivo.
