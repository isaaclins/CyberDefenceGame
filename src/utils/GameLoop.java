package src.utils;

import src.main.Game;
public class GameLoop implements Runnable {
    private final Game game;
    private boolean running = false;
    private Thread thread;

    public GameLoop(Game game) {
        this.game = game;
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this, "Game Thread");
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
                game.tick();
                delta--;
            }

            game.render();
        }
    }
}
