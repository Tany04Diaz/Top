## Top — Sistema de ranking para facciones
Versión: 1.0-RELEASE
Compatibilidad: Paper / Spigot 1.21.10
Autor: TanyDiaz (AkorpuzZ)
Dependencias: Vault (requerido); PlaceholderAPI (recomendado); SaberFactions (opcional); DecentHolograms (opcional).
## Descripción
Plugin que calcula y expone un ranking de facciones basado en valores configurables (bloques, spawners, items, saldo de Vault). Provee una expansión para PlaceholderAPI para usar en hologramas, tab, chat o scoreboards y permite actualizaciones periódicas o forzadas del snapshot de datos.
## Características
• 	Cálculo de valor total de facciones (claims, spawners, items, balances).
• 	Placeholders dinámicos via PlaceholderAPI: , , , etc.
• 	Snapshot periódico y comando manual de actualización.
• 	Integración opcional con DecentHolograms y SaberFactions.
• 	Evita accesos asíncronos a objetos de Bukkit; actualizaciones seguras en hilo principal
## Instalación
1. 	Copia el JAR en la carpeta .
2. 	Instala Vault y, si vas a usar placeholders, PlaceholderAPI.
3. 	(Opcional) Instala SaberFactions y DecentHolograms para integración completa.
4. 	Inicia el servidor y verifica en consola que aparezcan los mensajes de activación y registro del placeholder.
## Configuración (config.yml — ejemplo
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
• 	/top — Mostrar top de facciones en chat.
• 	/topplayers — Mostrar top de jugadores (si está implementado).
• 	/topreload — Recargar configuración y valores.
• 	/topupdate — Forzar actualización inmediata del snapshot.
## Placeholders disponibles
• 	 — Nombre de la facción en la posición n.
• 	 — Valor total de la facción n (formato numérico con 2 decimales).
• 	 — Línea formateada: .
Sustituir  por índice 1..max-lines.
## Buenas prácticas y rendimiento
• 	Si el cálculo es costoso, aumenta  para reducir carga.
• 	Registra listeners sobre eventos relevantes (claim/unclaim, cambios de saldo) y llama a  o  desde el hilo principal para actualizaciones inmediatas.
• 	Evita operaciones pesadas en el hilo principal; separar lectura mínima (main) y procesamiento en background es la estrategia recomendada para servidores con muchas facciones.
## Compatibilidad y notas técnicas
• 	Probado en Paper 1.21.10.
• 	No utiliza NMS directamente, por lo que es más resistente a cambios de versión.
• 	Añadir  en  para PlaceholderAPI, Vault y SaberFactions mejora el orden de carga.
## Depuración rápida
• 	Mensaje de éxito al iniciar: 
• 	Mensaje de actualización: 
• 	Si ves errores tipo  significa que alguna lectura se ejecutó en un hilo asíncrono; revisar que  corra en hilo principal o use el patrón main-read / async-process.
## Contribuciones y soporte
Reporta bugs, solicita mejoras o contribuye con PRs. Incluye versión del servidor, logs relevantes y  mínimo que reproduzca el problema.
  
## Downloads
[Descarga](https://github.com/Tany04Diaz/Top/releases)
## License

[MIT](https://choosealicense.com/licenses/mit/)
