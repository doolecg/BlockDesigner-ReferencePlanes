# Reference Planes 1.2.0

A tidier right-click menu with every number in one Properties window, and the settings for new pictures in BlockDesigner's Settings window.

**Needs BlockDesigner 0.4.24 or later** (plugin API 6). Older BlockDesigners keep 1.1.6 until BlockDesigner itself is updated.

## New
- **Properties window, redone:** position, rotation and scale under **Transform**, UV offset and scale and opacity under **Picture**, each with its unit. **Reset rotation**, **Reset aspect ratio** and **Reset UV** are at the top of their sections. A field that isn't a number says so, and Apply waits until it is.
- **Add reference image…** can be given a key in Settings › Keybinds.

## Changed
- **Settings for new pictures are in the Settings window**, on Reference Planes' page (the tab's **Open settings…** button opens it): height in blocks, opacity in %, how blocks cover them, and whether one added in an axis view shows only there, each with a line of help. The height has an exact value box next to its slider. Your settings are kept.
- **Shorter right-click menu:** Properties…, Opacity, Show in, Draw (now showing the current choice), the flips, aligning to the view and Replace picture…. The position, rotation, scale and UV fields moved into Properties.

---

# Reference Planes 1.1.6

Kept up to date with BlockDesigner 0.4.23: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.14 or later** (plugin API 4). BlockDesigner 0.4.16 and later update to it by themselves.

## Changed
- Built against the BlockDesigner 0.4.23 plugin API.

---

# Reference Planes 1.1.5

Kept up to date with BlockDesigner 0.4.22: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.14 or later** (plugin API 4). BlockDesigner 0.4.16 and later update to it by themselves.

## Changed
- Built against the BlockDesigner 0.4.22 plugin API.

---

# Reference Planes 1.1.4

Kept up to date with BlockDesigner 0.4.18: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.14 or later** (plugin API 4). BlockDesigner 0.4.16 and later update to it by themselves.

## Changed
- Built against the BlockDesigner 0.4.18 plugin API.
- README in the same format as BlockDesigner's.

---

# Reference Planes 1.1.3

Kept up to date with BlockDesigner 0.4.17: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.14 or later** (plugin API 4). BlockDesigner 0.4.16 and later update to it by themselves.

## Changed
- Built against the BlockDesigner 0.4.17 plugin API.

---

# Reference Planes 1.1.2

Reference Planes now updates itself.

**Needs BlockDesigner 0.4.14 or later** (plugin API 4); automatic updates need BlockDesigner 0.4.16 or later. Install this version once by hand: download `reference-planes-1.1.2.jar` below, then in BlockDesigner open **Plugins › Manage plugins… › Install…** and pick it (it replaces the older version).

## New
- **Updates itself.** Its manifest now links this repository as its release source, so BlockDesigner 0.4.16 and later install new releases of it automatically.

## Changed
- Built against the BlockDesigner 0.4.16 plugin API.

---

# Reference Planes 1.1.1

Kept up to date with BlockDesigner 0.4.15: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.14 or later** (plugin API 4); BlockDesigner 0.4.15 is recommended. Install: download `reference-planes-1.1.1.jar` below, then in BlockDesigner open **Plugins › Manage plugins… › Install…** and pick it (it replaces the older version).

## Changed
- Built against the BlockDesigner 0.4.15 plugin API.
- In BlockDesigner 0.4.14 and later it has its own tab on the right: it shows the plugin is running and has buttons for everything it adds.

---

# Reference Planes 1.1.0

Settings for new pictures, in the plugin's own tab on the right.

**Needs BlockDesigner 0.4.14 or later** (plugin API 4). Install: download `reference-planes-1.1.0.jar` below, then in BlockDesigner open **Plugins › Manage plugins… › Install…** and pick it (it replaces 1.0.0).

## New
- **Reference Planes tab** on the right: shows the plugin is running, has an **Add reference image…** button, and holds its settings.
- **Settings for new pictures:** their height in blocks, opacity, whether they're drawn behind, among or in front of blocks, and whether a picture added in an axis view shows only in that view. Each picture can still be changed from its right-click menu.

---

# Reference Planes 1.0.0

The first release of Reference Planes: reference images in the scene, like Blender's.

**Needs BlockDesigner 0.4.12 or later** (plugin API 3). Install: download `reference-planes-1.0.0.jar` below, then in BlockDesigner open **Plugins › Manage plugins… › Install…** and pick it.

## New
- **Add a picture** (Plugins › Add reference image…, or drop a PNG, JPEG, GIF or BMP on the window). The plane keeps the picture's proportions.
- **Views:** added in an orthographic axis view, it faces that view and shows only there; otherwise in every view.
- **Layers panel:** a row per picture with a **REFERENCE** tag, eye and lock buttons.
- **Move, Rotate, Scale** with G, R and S; Ctrl snaps to whole blocks, 15° and 0.1× steps.
- **Right-click menu:** position, rotation and scale, UV offset and scale, opacity, which views it shows in, drawn behind, among or in front of blocks, flip, align to view, replace picture, reset aspect ratio, and a Properties… window.
- **Saved with the project**, pictures included.

---
