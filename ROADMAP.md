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
  - [ ] Implement XP bar at the bottom of the player.

## UI/UX & Screens

- [ ] **Menu Screen**

  - [ ] Create a main menu screen.
  - [ ] Implement a modular class selection system. New gun classes should automatically appear as options.

- [ ] **Settings Page**

  - [ ] Create a settings page accessible from the menu.
  - [ ] Implement a toggle for auto-aim and manual-aim.

- [ ] **In-Game HUD**
  - [ ] All HUD elements must be positioned relative to the player.
  - [ ] **Ammo Display**:
    - [ ] Show small round circles for bullets in the magazine.
    - [ ] The circle for the bullet being fired should be visually distinct/in front.
  - [ ] **Health Display**:
    - [ ] Show X red circles on top of the player.
    - [ ] Filled circles represent current health, hollow circles represent lost health.

## Quality of Life (QOL) & Effects

- [ ] **Pause Mechanic**
  - [ ] Implement a pause and resume feature.
- [ ] **Visual Effects**
  - [ ] Add particle effects for gunshots.
  - [ ] Add particle effects for enemy deaths.
  - [ ] Implement a screen shake effect when the player shoots.
  - [ ] Add glow effect for entities (player, enemies, xps, bullets)
