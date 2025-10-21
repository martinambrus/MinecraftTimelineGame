# Core Assets Directory

This directory stores all runtime assets used by the Minecraft Timeline card game. Each subdirectory focuses on a specific asset type to simplify loading pipelines across every supported platform.

- `images/`
  - `cards/`: Card face artwork, icons, and card back textures.
  - `ui/`: User interface textures such as buttons, panels, and cursors.
- `sounds/`
  - `music/`: Background music tracks and ambient loops.
  - `sfx/`: Sound effects for interactions, feedback, and game events.
- `fonts/`: Bitmap or TrueType fonts used for rendering text.
- `data/`: JSON, XML, or other structured data files that describe cards, timelines, or configuration.

Organizing assets by type keeps loading code predictable and ensures compatibility across Desktop, Android, iOS, and HTML targets.
