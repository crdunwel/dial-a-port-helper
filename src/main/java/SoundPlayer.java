import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SoundPlayer {
    private static final Map<String, Clip> clipCache = new HashMap<>();
    private static final Map<String, byte[]> mp3Cache = new HashMap<>();

    public static void playSound(String filePath) {
        try {
            if (filePath.endsWith(".mp3")) {
                playMP3(filePath);
            } else {
                playOtherFormats(filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playOtherFormats(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Clip clip = clipCache.get(filePath);
        if (clip == null) {
            File soundFile = new File(filePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clipCache.put(filePath, clip);
        }
        clip.setFramePosition(0); // rewind to the beginning
        clip.start();
    }

    private static void playMP3(String filePath) {
        new Thread(() -> {
            byte[] mp3Data = mp3Cache.get(filePath);
            if (mp3Data == null) {
                try {
                    File file = new File(filePath);
                    mp3Data = new byte[(int) file.length()];
                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(mp3Data);
                    }
                    mp3Cache.put(filePath, mp3Data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            InputStream is = new ByteArrayInputStream(mp3Data);
            BufferedInputStream bis = new BufferedInputStream(is);
            Player player = null;
            try {
                player = new Player(bis);
                player.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            } finally {
                if (player != null) {
                    player.close(); // Close the player manually
                }
                try {
                    bis.close(); // Close BufferedInputStream
                    is.close(); // Close ByteArrayInputStream
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}