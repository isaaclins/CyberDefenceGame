# Cyber Defence Game

Cyber Defence Game is a simple dungeon crawler game implemented in Java. The player can move around the game window and transition between different rooms.

## Features

- Player movement using keyboard keys (W, A, S, D)
- Mouse-controlled gun aiming
- Room transitions when the player reaches the edges of the current room

## Requirements

- Java Development Kit (JDK) 17 or higher

## Setup

1. Clone the repository to your local machine.
2. Ensure you have JDK 17 or higher installed on your system.
3. Navigate to the project directory.

## Running the Game

### Using the Batch Script

1. Open a terminal or command prompt.
2. Navigate to the project directory.
3. Run the `runGame.bat` script:

```sh
runGame.bat
```

### Manually

1. Open a terminal or command prompt.
2. Navigate to the project directory.
3. Compile the Java files:

```sh
javac -d out src/main/Game.java src/utils/Constants.java
```

4. Run the game:

```sh
java -cp out src.main.Game
```

## Controls

- **W**: Move up
- **A**: Move left
- **S**: Move down
- **D**: Move right
- **Mouse**: Aim the gun

## Project Structure

```
.idea/
src/
    entity/
        Enemy.java
        GameWindow.java
        Player.java
    main/
        Game.java
    utils/
        Constants.java
runGame.bat
README.md
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.


### Future Ideas:
![alt text](/img/image.png)
Game mirrors itself or atleast the enemies