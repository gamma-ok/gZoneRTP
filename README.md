# gZoneRTP - Plugin de Teletransporte por Zonas para Minecraft 1.8

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.8-green.svg)
![License](https://img.shields.io/badge/license-MIT-yellow.svg)

## Descripción

**gZoneRTP** es un plugin de teletransporte aleatorio (RTP) por zonas para servidores Minecraft 1.8, basado en selecciones de **WorldEdit**. Permite a los administradores definir zonas personalizadas y a los jugadores teletransportarse aleatoriamente dentro de ellas, con búsqueda de ubicaciones seguras, sistema de cooldown, delay de teletransporte cancelable por movimiento, y mensajes 100% configurables.

## Tabla de Contenidos

- [Características Principales](#características-principales)
- [Comandos](#comandos)
- [Archivos de Configuración](#archivos-de-configuración)
- [Dependencias](#dependencias)
- [Permisos](#permisos)
- [Instalación](#instalación)
- [Almacenamiento de Zonas](#almacenamiento-de-zonas)
- [Funcionamiento Interno](#funcionamiento-interno)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Reporte de Problemas](#reporte-de-problemas)
- [Licencia](#licencia)
- [Autor](#autor)
- [Agradecimientos](#agradecimientos)
- [Enlaces Útiles](#enlaces-útiles)

## Características Principales

### Gestión de Zonas

- Creación de zonas a partir de una selección activa de **WorldEdit** (`//pos1` y `//pos2`).
- Eliminación y listado de zonas registradas.
- Persistencia automática en `zones.yml`, independiente de la configuración general.

### Búsqueda de Ubicación Segura

- Evita teletransportar al jugador a lava, agua, cactus, fuego, telarañas u otros bloques peligrosos.
- Búsqueda del bloque sólido más alto en la columna, con verificación de espacio libre alrededor.
- Reintentos automáticos (hasta 15 intentos) antes de descartar la zona como inválida.
- Distingue y reporta el motivo exacto cuando no es posible teletransportar:
  - Zona **sin suelo** (vacío/aire).
  - Zona **sin espacio** (llena de bloques).

### Teletransporte con Delay

- Cuenta regresiva configurable antes de ejecutar el teletransporte.
- Cancelación automática si el jugador se mueve durante la cuenta regresiva.
- Sonido configurable al completar el teletransporte.

### Cooldown y Bypass

- Cooldown por jugador entre usos de RTP, configurable en segundos.
- Bypass total de cooldown y delay para jugadores con permiso específico u operadores (OP), que se teletransportan de forma instantánea y sin mensajes de espera.

### Mensajes Totalmente Configurables

- Todos los mensajes del plugin (ayuda, errores, confirmaciones, cooldown, countdown) se editan libremente desde `config.yml`.
- Soporte completo de códigos de color de Minecraft (`&`).
- Placeholders dinámicos como `{zone}`, `{zones}` y `{seconds}`.

## Comandos

| Comando | Descripción | Permiso |
|---|---|---|
| `/zrtp` | Muestra la ayuda del plugin (solo administradores) | `zonertp.admin` |
| `/zrtp <zona>` | Teletransporta al jugador a una zona específica | `zonertp.user` |
| `/zrtp random` | Teletransporta al jugador a una zona aleatoria entre todas las registradas | `zonertp.user` |
| `/zrtp create <nombre>` | Crea una nueva zona a partir de la selección activa de WorldEdit | `zonertp.admin` |
| `/zrtp delete <nombre>` | Elimina una zona existente | `zonertp.admin` |
| `/zrtp list` | Lista todas las zonas registradas | `zonertp.user` |
| `/zrtp reload` | Recarga la configuración y las zonas desde disco | `zonertp.admin` |
| `/zrtp help` | Muestra la ayuda del plugin | `zonertp.admin` |

**Alias:** `/zrtp`, `/rtpzone` (además de `/zonertp`)

## Archivos de Configuración

### Estructura de Archivos

```
/gZoneRTP/
├── config.yml   # Configuración general y mensajes
├── zones.yml    # Zonas registradas (gestionado automáticamente por el plugin)
└── plugin.yml   # Descriptor del plugin
```

### config.yml

```yaml
# ########################################
# gZoneRTP - Configuration File
# Versión: 1.0.0
# ########################################

# Tiempo de espera entre usos de RTP (en segundos)
cooldown:
  seconds: 5

# Tiempo de cuenta regresiva antes de teletransportar (en segundos)
teleport-delay:
  seconds: 3

# Sonido al completar el teletransporte
sound:
  enabled: true
  name: "ENDERMAN_TELEPORT"

# Mensajes - Todos son completamente editables
messages:
  prefix: "&8[&6&lZoneRTP&8] &r"

  # Mensajes de ayuda (soporta múltiples líneas)
  help:
    - "&6&lZoneRTP Help"
    - "&e/zrtp <zona> &7- &fTeletransporta a una zona específica"
    - "&e/zrtp random &7- &fTeletransporta a una zona aleatoria"
    - "&e/zrtp create <nombre> &7- &fCrea una zona con selección WorldEdit"
    - "&e/zrtp delete <nombre> &7- &fElimina una zona"
    - "&e/zrtp list &7- &fLista todas las zonas"
    - "&e/zrtp reload &7- &fRecarga configuración y zonas"

  # Mensajes de error
  no-permission: "&cNo tienes permiso para usar este comando."
  player-only: "&cEste comando solo puede ser usado por jugadores."
  invalid-usage: "&cUso incorrecto. Usa &f/zrtp help &cpara ayuda."

  # Mensajes de gestión de zonas
  zone-not-found: "&cLa zona &f{zone} &cno existe."
  zone-created: "&a¡Zona &f{zone} &acreada exitosamente!"
  zone-deleted: "&aZona &f{zone} &aeliminada."
  zone-list: "&aZonas disponibles: &f{zones}"
  zone-exists: "&cYa existe una zona con el nombre &f{zone}&c."
  no-selection: "&cNo tienes una selección activa de WorldEdit."
  no-zones: "&cNo hay zonas registradas."

  # Mensajes de RTP
  rtp-success: "&a¡Teletransportado a la zona &f{zone}&a!"
  rtp-no-space: "&cLa zona &f{zone} &cestá llena de bloques, no hay espacio para teletransportarte."
  rtp-no-ground: "&cLa zona &f{zone} &cno tiene suelo firme, no hay dónde caer."

  # Mensajes de cooldown
  cooldown: "&cDebes esperar &f{seconds}s &cantes de usar RTP nuevamente."

  # Mensajes de delay de teletransporte
  teleport-start: "&eTeletransportando a &f{zone} &een &f{seconds}s &e(no te muevas)"
  teleport-countdown: "&eTeletransportando en &f{seconds}s&e..."
  teleport-cancelled-moved: "&cTeletransporte cancelado porque te moviste."

  # Mensajes de administración
  reload-success: "&aConfiguración y zonas recargadas."
```

### Placeholders Disponibles en Mensajes

| Placeholder | Descripción | Usado en |
|---|---|---|
| `{zone}` | Nombre de la zona | La mayoría de los mensajes |
| `{zones}` | Lista de zonas separadas por coma | `zone-list` |
| `{seconds}` | Segundos restantes | `cooldown`, `teleport-start`, `teleport-countdown` |

## Dependencias

| Dependencia | Tipo | Versión | Enlace |
|---|---|---|---|
| Spigot/Bukkit | Obligatorio | 1.8 | [SpigotMC](https://www.spigotmc.org) |
| WorldEdit | Obligatorio | 6.x | [WorldEdit](https://dev.bukkit.org/projects/worldedit) |
| WorldGuard | Opcional | - | [WorldGuard](https://dev.bukkit.org/projects/worldguard) |

> El plugin se deshabilitará automáticamente al iniciar si WorldEdit no está instalado o no puede inicializarse correctamente.

## Permisos

| Permiso | Descripción | Default |
|---|---|---|
| `zonertp.user` | Permite utilizar los comandos básicos de RTP (teletransporte, lista) | `true` |
| `zonertp.admin` | Permite gestionar zonas (crear, eliminar, recargar, ver ayuda) | `op` |
| `zonertp.bypasscooldown` | Omite el cooldown y el delay de teletransporte, ejecutándolo de forma instantánea | `false` |

> Los jugadores con rango de **operador (OP)** obtienen automáticamente el bypass del delay de teletransporte, sin necesidad de asignar `zonertp.bypasscooldown` explícitamente.

## Instalación

1. Descarga el archivo `gZoneRTP.jar`.
2. Asegúrate de tener **WorldEdit** instalado en la carpeta `plugins/`.
3. Coloca `gZoneRTP.jar` en la carpeta `plugins/` de tu servidor.
4. Reinicia el servidor. Se generarán automáticamente `config.yml` y `zones.yml` dentro de `plugins/gZoneRTP/`.
5. Configura `config.yml` según tus necesidades y usa `/zrtp reload` para aplicar los cambios sin reiniciar el servidor.

## Almacenamiento de Zonas

Las zonas se guardan automáticamente en `zones.yml` cada vez que se crean o eliminan.

```yaml
zones:
  spawn_zone:
    world: world
    min:
      x: 100.0
      y: 60.0
      z: 100.0
    max:
      x: 200.0
      y: 90.0
      z: 200.0
```

## Funcionamiento Interno

### Búsqueda de Ubicación Segura

- Se generan coordenadas X/Z aleatorias dentro de los límites de la zona.
- Se busca el bloque sólido más alto en esa columna (de arriba hacia abajo).
- Se valida que el espacio sobre el bloque (pies y cabeza) esté libre y no contenga bloques peligrosos.
- Si el punto exacto no es válido, se prueban los 8 puntos adyacentes antes de descartar el intento.
- Este proceso se repite hasta 15 veces por cada solicitud de RTP antes de reportar un error.

### Diagnóstico de Zonas Inválidas

- Si en ninguno de los 15 intentos se encontró un bloque sólido, se asume que la zona no tiene suelo (por ejemplo, está ubicada en el vacío) y se muestra `rtp-no-ground`.
- Si se encontró suelo en al menos un intento pero nunca hubo espacio libre alrededor, se asume que la zona está llena de bloques y se muestra `rtp-no-space`.

### Delay y Cooldown

- Toda la búsqueda de ubicación segura corre de forma **asíncrona** para no afectar el rendimiento del servidor.
- El teletransporte real, el countdown y el envío de mensajes se ejecutan siempre en el **hilo principal**.
- Durante el countdown, si el jugador se mueve más de 0.5 bloques respecto a su posición inicial, el teletransporte se cancela automáticamente.
- Los jugadores con bypass (`zonertp.bypasscooldown` o rango OP) omiten tanto el cooldown como el countdown, y no reciben los mensajes asociados a la espera.

## Ejemplos de Uso

### Crear una zona

```
//pos1                  # Marcar primera esquina con WorldEdit
//pos2                  # Marcar segunda esquina con WorldEdit
/zrtp create spawn_zone # Crear la zona a partir de la selección
```

### Teletransportarse

```
/zrtp spawn_zone   # Teletransportarse a una zona específica
/zrtp random        # Teletransportarse a una zona aleatoria
```

### Administración

```
/zrtp list                # Listar todas las zonas registradas
/zrtp delete spawn_zone   # Eliminar una zona
/zrtp reload               # Recargar configuración y zonas
/zrtp help                 # Ver ayuda del plugin (o simplemente /zrtp)
```

## Reporte de Problemas

Si encuentras algún problema o tienes sugerencias:

1. Revisa la consola del servidor en busca de errores.
2. Verifica que WorldEdit esté instalado y actualizado correctamente.
3. Asegúrate de estar usando Minecraft 1.8.
4. Crea un [issue](https://github.com/gamma-ok/gZoneRTP/issues) en el repositorio con:
   - Versión del plugin
   - Versión del servidor y de WorldEdit
   - Descripción del problema
   - Logs/errores relevantes

## Licencia

Este proyecto está bajo la licencia **MIT**. Para más información, consulta el archivo [LICENSE](LICENSE).

## Autor

**gamma** — GitHub: [@gamma-ok](https://github.com/gamma-ok)

## Agradecimientos

- SpigotMC por la API
- WorldEdit por el sistema de selecciones
- A la comunidad de Minecraft por el soporte y feedback

## Enlaces Útiles

- [Documentación de Spigot](https://www.spigotmc.org/wiki/index/)
- [WorldEdit Wiki](https://worldedit.enginehub.org/en/latest/)
