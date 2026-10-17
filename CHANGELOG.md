# Changelog — Enchanter's Bench

## 0.0.0-beta.2 (sin publicar)

- NeoForge bumped 21.1.249 → 21.1.259. New branch `minecraft/1.21.1/neoforge-21.1.259/production`.
- Primer port de Enchanting Plus 5.0 (1.12.2) a NeoForge 1.21.1: mesa de encantamiento avanzada (bloque, block entity, menú, pantalla con sliders), cálculo de coste con config (baseCost, costFactor, treasureFactor, curseFactor, floatingBookPower, blacklists), mejora de mesa, libros decorativos con 7 variantes y libro flotante animado, eventos de API, red (encantar y slider), pestaña creativa, recetas, loot tables y 8 idiomas.
- `build` y `runServer` verificados (el servidor arranca limpio). **No probado in-game**: GUI, render del libro y flujo de encantar quedan pendientes de prueba en cliente.
- Cambios respecto al original: la variante decorativa "eplus" pasa a `bench`; los libros se registran en el atlas de bloques (`assets/minecraft/atlases/blocks.json`); sin dependencia de Bookshelf; JEI y CraftTweaker fuera de esta beta. Las texturas vanilla de la mesa no se redistribuyen.

## 0.0.0-beta.1 (sin publicar) — supersedida

- mismo contenido; renumerada a beta.2 tras el bump de NeoForge.
