# Game Development Roadmap

## Core Gameplay Mechanics

- [ ] **Gun Classes**

  - [x] Implement a base Gun class.
  - [x] Create Shotgun class with attributes: wide spread, high knockback, multiple bullets per shot.
  - [x] Create Sniper class with attributes: high damage, high bullet speed, low fire rate, small magazine.
  - [x] Create SMG class with attributes: high fire rate, larger magazine, moderate spread.
  - [x] Each class should have distinct:
    - [x] Bullet size
    - [x] Bullet damage
    - [x] Bullet speed
    - [x] Reload speed
    - [x] Magazine size
    - [x] Spread
    - [x] Knockback
    - [x] Bullets shot with one shot

- [x] **Enemy System**

  - [x] Implement a base Enemy class.
  - [x] Create Small enemy type: fast, low health, low damage.
  - [x] Create Normal enemy type: medium stats.
  - [x] Create Big enemy type: slow, high health, high damage.
  - [x] Create Boss enemy type: unique patterns, very high health and damage.
  - [x] Each enemy type should have distinct:
    - [x] Speed
    - [x] Health
    - [x] Damage
    - [x] Color & Size
    - [x] XP Drop amount

- [x] **XP & Leveling System**
  - [x] Create XP drop entity (small rectangles).
  - [x] Implement XP gain on enemy kill.
  - [x] If player is in radius of XP, the player will automatically pick it up.
  - [x] If player is not in radius of XP, the XP will move SLIGHTLY towards the player (if in radius).
  - [x] Create a leveling system with incrementally harder levels.
  - [x] Display player level number inside the player rectangle.
  - [x] Implement XP bar at the bottom of the player.

## UI/UX & Screens

- [x] **Menu Screen**

  - [x] Create a main menu screen.
  - [x] Implement a modular class selection system. New gun classes should automatically appear as options.

- [x] **In-Game HUD**

  - [x] All HUD elements must be positioned relative to the player.
  - [x] **Ammo Display**:

    - [x] Show small, slightly opaque round circles circling AROUND THE PLAYER for bullets in the magazine.

  - [x] **Health Display**:
    - [x] Show X red circles on top of the player.
    - [x] Filled circles represent current health, hollow circles represent lost health.

## Quality of Life (QOL) & Effects

- [ ] **Visual Effects**

  - [ ] Add particle effects for gunshots.
  - [ ] Add particle effects for enemy deaths.
  - [ ] Implement a screen shake effect when the player shoots.
  - [ ] Add glow effect for entities (player, enemies, xps, bullets)

## Game Flow & State

- [ ] **Pause & Resume**

  - [ ] Pressing `ESC` or `P` should pause the game.
  - [ ] A "Paused" message should appear on the screen.
  - [ ] All game movement and logic should stop.
  - [ ] Pressing the key again should resume the game.

- [ ] **Game Over Screen**

  - [ ] When player health reaches zero, the game should end.
  - [ ] Display a "Game Over" screen with statistics (time survived, enemies defeated, level reached).
  - [ ] Option to return to the main menu.

- [ ] **Enemy Wave System**
  - [ ] Spawn enemies in progressively difficult waves.
  - [ ] Display current wave number on the HUD.
  - [ ] Implement a brief pause between waves.

## Player Progression & Upgrades

- [ ] **Level-Up Upgrade System**
  - [ ] On level up, pause the game and present a choice of 3 random upgrades.
  - [ ] Create a modular system for applying upgrades to player/gun stats.
  - [ ] **Potential Upgrades:**
    - [ ] `+` Bullet Damage
    - [ ] `+` Fire Rate
    - [ ] `+` Reload Speed
    - [ ] `+` Magazine Size
    - [ ] `+` Player Speed
    - [ ] `+` Max Health
    - [ ] `+` Health Regeneration
    - [ ] `+` XP Gain
    - [ ] `+` Bullet Knockback
    - [ ] `+` Projectile Piercing

## Audio

- [ ] **Sound Effects**

  - [ ] Gunshots (unique per class)
  - [ ] Reloading
  - [ ] Player Hit
  - [ ] Enemy Hit
  - [ ] Enemy Defeated
  - [ ] XP Pickup
  - [ ] Level Up
  - [ ] UI Button Clicks

- [ ] **Music**
  - [ ] Main Menu Theme
  - [ ] Gameplay Loop
