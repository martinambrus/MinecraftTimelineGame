# Asset Directory Structure

This folder stores all runtime assets for the Minecraft Timeline card game. The sub-directories serve the following purposes:

- `images/` – Textures, sprites, and other raster artwork used for cards, UI elements, and effects.
- `sounds/` – Music tracks and sound effects.
- `fonts/` – Bitmap fonts, TTF files, and associated metadata.
- `data/` – JSON, CSV, or other structured files describing cards, decks, localization, and configuration.

Place any additional supporting files alongside these directories when needed. The Gradle projects reference this root folder directly, so assets added here will automatically be available to every platform.
