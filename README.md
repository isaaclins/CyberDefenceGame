# Cyber Defence Game

A 2D top-down shooter game written in Java. Fight off waves of enemies, level up, and choose upgrades to survive as long as you can.

## Features

This project is currently under development. Here is a list of current and planned features:

### Core Gameplay

- **Gun Classes:** Choose between different gun classes like Shotgun, Sniper, and SMG, each with unique stats.
- **Enemy Variety:** Face off against different types of enemies: Small, Normal, Big, and Bosses.
- **XP & Leveling:** Gain experience from defeating enemies, level up, and become more powerful.
- **Upgrades:** When you level up, you can choose from a selection of upgrades to enhance your abilities.

### UI/UX

- **Main Menu:** A fully functional main menu with class selection.
- **In-Game HUD:** A dynamic HUD that displays health and ammo around the player.
- **Pause & Game Over:** Pause the game at any time, and see your stats when the game is over.

### Visuals

- **Particle Effects:** Visual effects for gunshots and enemy deaths.
- **Screen Shake:** Feel the impact of your shots with a screen shake effect.
- **Glow Effects:** Glowing effects for all game entities.

## How to Play

### Requirements

- Java Development Kit (JDK) installed.

### Running the Game

Use `make run` to compile and launch the game:

```bash
make run
```

### Building App Packages

Use `make build` to produce a runnable JAR, an app image, and the native installer for the current host OS:

```bash
make build
```

Generated artifacts are written to `dist/`:

- macOS: `.app` app image and `.dmg`
- Windows: app image and `.exe`
- Linux: app image and `.deb` or `.rpm` when the required packaging tool is installed

Native installers must be built on their target OS. For example, `.dmg` packaging must run on macOS and `.exe` packaging must run on Windows.

The Makefile respects `JAVA_HOME`, so shell helpers such as `setjava` can point the build at a specific JDK before running `make`.

You can also build specific package types directly:

```bash
make jar
make app-image
make package PACKAGE_TYPE=dmg
make build-mac
make build-windows
make build-linux
```

### GitHub Releases (CI)

GitHub Actions builds **Linux (.deb)**, **Windows (.exe)**, and **macOS (.dmg)** installers and uploads them in two cases:

1. **Published GitHub Release** — When you publish a release, installers are attached to that release’s tag. Use a tag like `v1.2.3`; the package version strips the leading `v` for `jpackage`.
2. **Pushes to the `release` branch** — Each push refreshes the rolling pre-release tagged **`rolling-release`**: the workflow moves that tag to the latest commit, clears previous assets on that pre-release, and uploads new installers (version `1.0.<run number>`). Download them from the repository’s **Releases** page.

Unsigned macOS builds may require **Right-click → Open** the first time you run the app.

## Controls

- **Movement:** `W`, `A`, `S`, `D`
- **Dash:** `Shift`
- **Shoot:** `Spacebar`
- **Reload:** `R`
- **Pause:** `Escape` or `P`
- **Menus:** Use the `Mouse` to navigate and click on buttons in the menu, game over, and upgrade screens.

## Contributing

Contributions are welcome! If you would like to contribute, please fork the repository and submit a pull request.
