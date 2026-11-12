# FOOTBALL INTELLIGENCE AI — Android

Proyecto inicial Android nativo con Kotlin + Jetpack Compose. Interfaz oscura en español.

## Estado de esta versión
- Selector de dos equipos.
- Pantallas iniciales de análisis, jugadores e historial.
- Interfaz adaptable y navegación inferior.
- Etiqueta visible de MODO DEMO: no presenta estadísticas inventadas como si fueran datos reales.
- Permiso de internet preparado para la futura conexión a fuentes deportivas.

## Requisitos
- Android Studio Ladybug o posterior.
- JDK 17.
- Android SDK 35.
- Gradle se descarga automáticamente desde Android Studio si no está instalado.

## Abrir y compilar
1. Descomprime el proyecto.
2. Abre la carpeta `FOOTBALL_INTELLIGENCE_AI` desde Android Studio.
3. Espera a que termine la sincronización de Gradle.
4. Conecta un teléfono Android con depuración USB o inicia un emulador.
5. Pulsa Run para probar la app.
6. Para generar APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
7. APK de depuración: `app/build/outputs/apk/debug/app-debug.apk`.

También puedes compilar desde una terminal en la carpeta del proyecto con:
`gradlew assembleDebug` (Windows, después de que Android Studio genere/instale el wrapper).

## Próxima etapa: datos deportivos gratuitos
La app todavía no consulta datos deportivos ni genera predicciones reales. Antes de activar pronósticos se integrará una fuente gratuita y se validarán sus límites, cobertura y atribución. La disponibilidad de lesiones, xG y estadísticas avanzadas depende del proveedor y la competición.

Arquitectura prevista:
- `SportsDataProvider`: interfaz para proveedores.
- Repositorio: normalización de equipos, partidos y jugadores.
- Motor estadístico: forma reciente, Elo y Poisson/xG cuando existan datos suficientes.
- Registro de fuentes y fecha de consulta.
- Persistencia local para historial.

## Seguridad y precisión
No incluir claves privadas de API dentro del APK. Si el proveedor requiere una clave, se utilizará un backend intermedio o una configuración segura. Lesiones y sanciones se mostrarán con fuente, fecha y nivel de confirmación. Los pronósticos serán probabilidades, nunca garantías.

## Compilar en GitHub (sin Android Studio)
1. Sube todo el contenido de esta carpeta a un repositorio de GitHub (rama `main`).
2. Entra a la pestaña **Actions**; el flujo "Compilar APK" se ejecuta solo al subir cambios, o con **Run workflow**.
3. Cuando termine (marca verde), abre la ejecución y descarga `app-debug-apk` en la sección **Artifacts**.
4. Descomprime y pasa `app-debug.apk` al teléfono para instalarlo.
