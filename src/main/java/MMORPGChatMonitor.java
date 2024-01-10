import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Paths;

public class MMORPGChatMonitor {
    private Settings settings;
    private ChatBoxes chatBoxes;
    private LogReader logReader;
    private DialSystemTray dialSystemTray;

    public MMORPGChatMonitor() {
        loadSettings();
        chatBoxes = new ChatBoxes(this);
        dialSystemTray = new DialSystemTray(this);
        dialSystemTray.initSystemTray();
        logReader = new LogReader(this);
    }

    public LogReader getLogReader() {
        return logReader;
    }

    public void startMonitoring() throws IOException, InterruptedException {
        logReader.startMonitoring();
    }

    public void stopMonitoring() {
        logReader.stopMonitoring();
        chatBoxes.destroyAllFrames();
    }

    public void saveSettings(Settings settings) {
        Gson gson = new Gson();
        String json = gson.toJson(settings);

        try (FileWriter writer = new FileWriter("config.json")) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        Gson gson = new Gson();
        File configFile = new File("config.json");

        if (!configFile.exists()) {
            // Create default settings
            Settings defaultSettings = new Settings();
            defaultSettings.setLogFilePath(Paths.get("defaultPath.log").toString());
            defaultSettings.setWindowWidth(200);
            defaultSettings.setWindowHeight(200);
            defaultSettings.setBoundingArea(new Rectangle(0, 0, 800, 600));
            defaultSettings.setHighlightColor("#E0FFEB");
            defaultSettings.setHighlightColorBad("#FFCCCC");
            defaultSettings.setBackgroundColor("#D8D8D8");
            defaultSettings.setBackgroundOpacity(0.5f);
            defaultSettings.setLookbackSeconds(3600); // 1 hour in seconds
            defaultSettings.setMsgSoundFilePath("");
            defaultSettings.setPlaySound(false);

            saveSettings(defaultSettings); // Save the default settings
            this.settings = defaultSettings;
        } else {
            try (Reader reader = new FileReader(configFile)) {
                this.settings = gson.fromJson(reader, Settings.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSettings(Settings settings) {
       this.settings = settings;
    }

    public Settings getSettings() {
        return settings;
    }

    public ChatBoxes getChatBoxes() {
        return chatBoxes;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MMORPGChatMonitor();
    }
}