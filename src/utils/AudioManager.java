package src.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

import src.entity.Gun;
import src.entity.SMG;
import src.entity.Shotgun;
import src.entity.Sniper;

public class AudioManager {
    private static final AudioFormat FORMAT = new AudioFormat(22_050f, 16, 1, true, false);

    private final Map<String, ClipPool> clipPools = new HashMap<>();
    private final Map<String, Long> lastPlaybackTimes = new HashMap<>();

    private boolean enabled = true;

    public AudioManager() {
        initialize();
    }

    public void playShot(Gun gun) {
        if (gun instanceof Shotgun) {
            play("shotgunShot", 0);
            return;
        }
        if (gun instanceof Sniper) {
            play("sniperShot", 0);
            return;
        }
        play("smgShot", 95);
    }

    public void playReloadComplete(Gun gun) {
        if (gun instanceof Shotgun) {
            play("shotgunReload", 40);
            return;
        }
        if (gun instanceof Sniper) {
            play("sniperReload", 40);
            return;
        }
        play("smgReload", 40);
    }

    public void playReloadProgress(Gun gun) {
        if (gun instanceof Shotgun) {
            play("shotgunReloadInsert", 35);
            return;
        }
        if (gun instanceof Sniper) {
            play("sniperReloadInsert", 35);
            return;
        }
        play("smgReloadInsert", 35);
    }

    public void playPlayerHit() {
        play("playerHit", 100);
    }

    public void playEnemyHit() {
        play("enemyHit", 45);
    }

    public void playEnemyDefeated() {
        play("enemyDefeat", 35);
    }

    public void playXpPickup() {
        play("xpPickup", 60);
    }

    public void playLevelUp() {
        play("levelUp", 0);
    }

    public void playUiClick() {
        play("uiClick", 30);
    }

    public void playWaveStart() {
        play("waveStart", 0);
    }

    public void close() {
        for (ClipPool pool : clipPools.values()) {
            pool.close();
        }
        clipPools.clear();
    }

    private void initialize() {
        try {
            register("smgShot", buildSignal(85, 980, 420, 0.40, 0.28, 0.10), 6);
            register("shotgunShot", buildSignal(135, 300, 120, 0.60, 0.55, 0.25), 3);
            register("sniperShot", buildSignal(180, 560, 160, 0.52, 0.08, 0.35), 2);

            register("smgReload", buildSignal(95, 460, 760, 0.24, 0.02, 0.02), 2);
            register("shotgunReload", buildSignal(130, 220, 310, 0.26, 0.08, 0.05), 2);
            register("sniperReload", buildSignal(160, 330, 520, 0.24, 0.03, 0.03), 2);
            register("smgReloadInsert", buildSignal(45, 760, 980, 0.12, 0.01, 0.04), 3);
            register("shotgunReloadInsert", buildSignal(60, 210, 270, 0.15, 0.02, 0.06), 2);
            register("sniperReloadInsert", buildSignal(70, 340, 440, 0.13, 0.01, 0.03), 2);

            register("playerHit", buildSignal(110, 170, 90, 0.46, 0.38, 0.15), 2);
            register("enemyHit", buildSignal(70, 680, 260, 0.20, 0.24, 0.05), 4);
            register("enemyDefeat", buildSignal(120, 240, 80, 0.26, 0.30, 0.08), 3);
            register("xpPickup", buildSignal(90, 700, 980, 0.18, 0.01, 0.02), 3);
            register("levelUp", buildMelody(new double[] { 440, 554, 660, 880 }, 70, 0.20), 2);
            register("waveStart", buildMelody(new double[] { 220, 330, 440 }, 90, 0.18), 2);
            register("uiClick", buildSignal(50, 920, 700, 0.12, 0.01, 0.02), 2);
        } catch (IllegalArgumentException | LineUnavailableException e) {
            enabled = false;
            close();
        }
    }

    private void play(String cueId, long minIntervalMillis) {
        if (!enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastPlayed = lastPlaybackTimes.get(cueId);
        if (lastPlayed != null && now - lastPlayed < minIntervalMillis) {
            return;
        }

        ClipPool pool = clipPools.get(cueId);
        if (pool == null) {
            return;
        }

        lastPlaybackTimes.put(cueId, now);
        pool.play();
    }

    private void register(String cueId, byte[] pcmData, int polyphony) throws LineUnavailableException {
        ClipPool pool = new ClipPool();
        for (int i = 0; i < polyphony; i++) {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, pcmData, 0, pcmData.length);
            pool.add(clip);
        }
        clipPools.put(cueId, pool);
    }

    private byte[] buildSignal(int durationMillis, double startFrequency, double endFrequency, double amplitude,
            double noiseMix, double squareMix) {
        int sampleCount = (int) (FORMAT.getSampleRate() * durationMillis / 1000.0);
        byte[] data = new byte[sampleCount * 2];
        double phase = 0.0;
        Random random = new Random(durationMillis + (long) startFrequency + ((long) endFrequency * 31));
        double sampleRate = FORMAT.getSampleRate();

        for (int i = 0; i < sampleCount; i++) {
            double progress = sampleCount <= 1 ? 1.0 : i / (double) (sampleCount - 1);
            double frequency = startFrequency + ((endFrequency - startFrequency) * progress);
            phase += (Math.PI * 2 * frequency) / sampleRate;

            double envelope = Math.pow(1.0 - progress, 2.3);
            double sine = Math.sin(phase);
            double square = sine >= 0 ? 1.0 : -1.0;
            double noise = (random.nextDouble() * 2.0) - 1.0;
            double baseMix = Math.max(0.0, 1.0 - noiseMix - squareMix);
            double sample = ((baseMix * sine) + (squareMix * square) + (noiseMix * noise)) * amplitude * envelope;

            short value = (short) (Math.max(-1.0, Math.min(1.0, sample)) * Short.MAX_VALUE);
            data[i * 2] = (byte) (value & 0xFF);
            data[(i * 2) + 1] = (byte) ((value >> 8) & 0xFF);
        }

        return data;
    }

    private byte[] buildMelody(double[] frequencies, int noteDurationMillis, double amplitude) {
        List<byte[]> noteData = new ArrayList<>();
        int totalLength = 0;
        for (double frequency : frequencies) {
            byte[] note = buildSignal(noteDurationMillis, frequency, frequency * 1.05, amplitude, 0.01, 0.02);
            noteData.add(note);
            totalLength += note.length;
        }

        byte[] melody = new byte[totalLength];
        int offset = 0;
        for (byte[] note : noteData) {
            System.arraycopy(note, 0, melody, offset, note.length);
            offset += note.length;
        }
        return melody;
    }

    private static final class ClipPool {
        private final List<Clip> clips = new ArrayList<>();
        private int nextIndex;

        private void add(Clip clip) {
            clips.add(clip);
        }

        private void play() {
            if (clips.isEmpty()) {
                return;
            }

            Clip selectedClip = null;
            for (int i = 0; i < clips.size(); i++) {
                int index = (nextIndex + i) % clips.size();
                Clip clip = clips.get(index);
                if (!clip.isRunning()) {
                    selectedClip = clip;
                    nextIndex = (index + 1) % clips.size();
                    break;
                }
            }

            if (selectedClip == null) {
                selectedClip = clips.get(nextIndex);
                selectedClip.stop();
                nextIndex = (nextIndex + 1) % clips.size();
            }

            selectedClip.setFramePosition(0);
            selectedClip.start();
        }

        private void close() {
            for (Clip clip : clips) {
                clip.stop();
                clip.close();
            }
            clips.clear();
        }
    }
}
