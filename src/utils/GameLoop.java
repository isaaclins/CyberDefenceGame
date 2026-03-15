package src.utils;

public class GameLoop implements Runnable {
    private static final double NS_PER_TICK = 1_000_000_000.0 / 60.0;
    private static final long TARGET_FRAME_TIME_NS = 1_000_000_000L / 60L;

    private final src.main.Game game;
    private volatile boolean running = false;
    private Thread thread;

    public GameLoop(src.main.Game game) {
        this.game = game;
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this, "Game Thread");
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        if (thread == null) {
            return;
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double delta = 0;

        while (running) {
            long frameStart = System.nanoTime();
            delta += (frameStart - lastTime) / NS_PER_TICK;
            lastTime = frameStart;

            int updates = 0;
            while (delta >= 1 && updates < 5) {
                game.tick();
                delta--;
                updates++;
            }

            game.render();

            long frameTime = System.nanoTime() - frameStart;
            long sleepNanos = TARGET_FRAME_TIME_NS - frameTime;
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            } else {
                Thread.yield();
            }
        }
    }
}
