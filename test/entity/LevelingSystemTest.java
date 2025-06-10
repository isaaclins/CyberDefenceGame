package test.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import src.entity.LevelingSystem;

public class LevelingSystemTest {

    @Test
    public void testAddXp() {
        LevelingSystem levelingSystem = new LevelingSystem();
        levelingSystem.addXp(50);
        assertEquals(50, levelingSystem.getXp());
        assertEquals(1, levelingSystem.getLevel());
    }

    @Test
    public void testLevelUp() {
        LevelingSystem levelingSystem = new LevelingSystem();
        levelingSystem.addXp(120);
        assertEquals(2, levelingSystem.getLevel());
        assertEquals(20, levelingSystem.getXp());
        assertEquals(150, levelingSystem.getXpToNextLevel());
    }

    @Test
    public void testMultipleLevelUps() {
        LevelingSystem levelingSystem = new LevelingSystem();
        levelingSystem.addXp(250); // 100 to level 2, 150 for next
        assertEquals(3, levelingSystem.getLevel());
        assertEquals(0, levelingSystem.getXp());
        assertEquals(225, levelingSystem.getXpToNextLevel());
    }
}
