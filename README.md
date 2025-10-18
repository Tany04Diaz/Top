# Top — Sistema de ranking para facciones
Versión: 1.0-RELEASE
Compatibilidad: Paper / Spigot 1.21.10
Autor: TanyDiaz (AkorpuzZ)
Dependencias: Vault (requerido); PlaceholderAPI (recomendado); SaberFactions (opcional); DecentHolograms (opcional).
## Descripción
Plugin que calcula y expone un ranking de facciones basado en valores configurables (bloques, spawners, items, saldo de Vault). Provee una expansión para PlaceholderAPI para usar en hologramas, tab, chat o scoreboards y permite actualizaciones periódicas o forzadas del snapshot de datos.
## Características
- 	Cálculo de valor total de facciones (claims, spawners, items, balances).
- 	Placeholders dinámicos via PlaceholderAPI: %top_<n>name%, %top<n>totalvalue%, %top<n>_rankline%.
- 	Snapshot periódico y comando manual de actualización.
- 	Integración opcional con DecentHolograms y SaberFactions.
- 	Evita accesos asíncronos a objetos de Bukkit; actualizaciones seguras en hilo principal.
## Instalación
1. 	Copia el JAR en la carpeta plugins/.
2. 	Instala Vault y, si vas a usar placeholders, PlaceholderAPI.
3. 	(Opcional) Instala SaberFactions y DecentHolograms para integración completa.
4. 	Inicia el servidor y verifica en consola que aparezcan los mensajes de activación y registro del placeholder.
## Configuración (config.yml — ejemplo) 
```
values:
  blocks:
    DIAMOND_BLOCK: 1000
    EMERALD_BLOCK: 1200
  spawners:
    ZOMBIE: 500
    SKELETON: 600
  items:
    NETHER_STAR: 2500

holograms:
  max-lines: 10
  update-interval-seconds: 60
```
## Comandos
- 	/top — Mostrar top de facciones en chat.
- 	/topplayers — Mostrar top de jugadores (si está implementado).
- 	/topreload — Recargar configuración y valores.
-	/topupdate — Forzar actualización inmediata del snapshot.
## Permisos
- 	top.use (default: true) — Permite ver el top básico y usar el comando /top.
- 	top.players (default: true) — Permite usar /topplayers si esa funcionalidad está disponible.
- 	top.update (default: op) — Permite forzar una actualización inmediata con /topupdate.
- 	top.reload (default: op) — Permite recargar la configuración y los valores con /topreload.
- 	top.admin (default: op) — Acceso completo a comandos y acciones administrativas del plugin.
- 	top.placeholder.manage (default: op) — Permite registrar, desregistrar o administrar la expansión de PlaceholderAPI.
## Ejemplos de asignación:
- 	Para el equipo de staff, asigna top.update y top.reload para que puedan forzar y recargar sin ser operadores.
- 	Para jugadores normales, deja top.use y top.players en true para que puedan consultar los rankings.
-  Mantén top.admin y top.placeholder.manage restringidos a operadores o grupos de confianza.
## Recomendaciones:
- 	Usa un sistema de permisos (LuckPerms, PermissionsEx, etc.) para gestionar permisos por grupos en lugar de asignarlos individualmente.
- 	No concedas permis
## Placeholders disponibles
- 	%top_<n>_name% — Nombre de la facción en la posición n.
- 	%top_<n>_totalvalue% — Valor total de la facción n (formato numérico con 2 decimales).
- 	%top_<n>_rankline% — Línea formateada: n. Nombre - $valor.
Sustituir <n> por índice 1..max-lines.
## Buenas prácticas y rendimiento
- 	Si el cálculo es costoso, aumenta holograms.update-interval-seconds para reducir carga.
- 	Registra listeners sobre eventos relevantes (claim/unclaim, cambios de saldo) y llama a /topupdate o placeholder.updateSnapshot(...) desde el hilo principal para actualizaciones inmediatas.
- 	Evita operaciones pesadas en el hilo principal; separar lectura mínima (main) y procesamiento en background es la estrategia recomendada para servidores con muchas facciones.
## Compatibilidad y notas técnicas
- 	Probado en Paper 1.21.10.
- 	No utiliza NMS directamente, por lo que es más resistente a cambios de versión.
- 	Añadir softdepend en plugin.yml para PlaceholderAPI, Vault y SaberFactions mejora el orden de carga.
## Depuración rápida
- 	Mensaje de éxito al iniciar: TopPlaceholder registrado correctamente con PlaceholderAPI.
- 	Mensaje de actualización: TopPlaceholder snapshot actualizado (N facciones).
- 	Si ves errores tipo Tile is null, asynchronous access? significa que alguna lectura se ejecutó en un hilo asíncrono; revisar que FactionScanner.getValidFactions() corra en hilo principal o use el patrón main-read / async-process.
## Contribuciones y soporte
Reporta bugs, solicita mejoras o contribuye con PRs. Incluye versión del servidor, logs relevantes y config.yml mínimo que reproduzca el problema
  
## Downloads
[Descarga](https://github.com/Tany04Diaz/Top/releases)
## License

[MIT](https://choosealicense.com/licenses/mit/)

