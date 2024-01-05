import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {
    private final SimpleDateFormat logTimestampFormat = new SimpleDateFormat("[EEE MMM dd HH:mm:ss yyyy]");
    private static Pattern NPC_TRANSACTION_MESSAGE = Pattern.compile("(\\d+ platinum|\\d+ gold|\\d+ silver|\\d+ copper)(\\s?(\\d+ platinum|\\d+ gold|\\d+ silver|\\d+ copper)){0,3}\\s?(per|for the) .+");
    private MMORPGChatMonitor mmorpgChatMonitor;
    private LogDatabase logDatabase;
    private long lastReadPosition = 0; // Position in the file
    private Date lookbackDate;
    private volatile boolean isMonitoring; // Flag to control the monitoring loop

    public LogReader(MMORPGChatMonitor mmorpgChatMonitor) {
        this.mmorpgChatMonitor = mmorpgChatMonitor;
        this.lookbackDate = new Date(System.currentTimeMillis() - getSettings().getLookbackSeconds() * 1000L);
        System.out.println("Lookback timestamp starting point: " + this.lookbackDate);
        this.isMonitoring = false;
        this.logDatabase = new LogDatabase();
    }
    public void stopMonitoring() {
        this.isMonitoring = false; // This will cause the loop in monitorLogFile to exit
        this.lastReadPosition = 0;
    }

    private Settings getSettings() {
        return mmorpgChatMonitor.getSettings();
    }

    public void startMonitoring() throws IOException, InterruptedException {
        // First, load the log file completely

        try (RandomAccessFile accessFile = new RandomAccessFile(Paths.get(getSettings().getLogFilePath()).toFile(), "r")) {
            loadLogFile(accessFile);
        } catch (FileNotFoundException e) {
            return;
        }
        this.isMonitoring = true;
        // Now, monitor the log file for new lines
        monitorLogFile();
    }

    private void monitorLogFile() throws IOException, InterruptedException {
        Path logFilePath = Paths.get(mmorpgChatMonitor.getSettings().getLogFilePath());
        WatchService watchService = FileSystems.getDefault().newWatchService();
        logFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        // Start an infinite loop to wait for events
        try (RandomAccessFile accessFile = new RandomAccessFile(logFilePath.toFile(), "r")) {
            while (isMonitoring) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path changed = (Path) event.context();
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY &&
                            changed.endsWith(logFilePath.getFileName())) {
                        accessFile.seek(lastReadPosition); // Go to the last read position
                        lastReadPosition = loadLogFile(accessFile); // Read new lines since last read
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
        this.isMonitoring = false;
    }

    private long loadLogFile(RandomAccessFile accessFile) throws IOException {
        System.out.println("At position: " + lastReadPosition);
        if (lastReadPosition == 0) {
            // On initial run, find the lookback point
            lastReadPosition = findLookbackPoint(accessFile);
            System.out.println("Starting at position: " + lastReadPosition);
        }

        // Move to the last read position
        accessFile.seek(lastReadPosition);

        String line;
        String name = null;
        while ((line = accessFile.readLine()) != null) {
            try {
                if (name != null) {
                    processDonation(line, name);
                    name = null;
                } else {
                    name = processLogLine(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Update the last read position for the next call
        lastReadPosition = accessFile.getFilePointer();
        return lastReadPosition;
    }

    private long findLookbackPoint(RandomAccessFile accessFile) throws IOException {
        final long totalLength = accessFile.length();
        long filePointer = totalLength;
        long lineStartPointer;

        while (filePointer > 0) {
            accessFile.seek(--filePointer);
            int readByte = accessFile.read();

            // Check for new line or start of the file
            if (readByte == '\n' || filePointer == 0) {
                lineStartPointer = filePointer == 0 ? 0 : filePointer + 1;
                accessFile.seek(lineStartPointer);
                String line = accessFile.readLine();

                if (line != null && !line.trim().isEmpty()) { // Check if line is not null and not empty
                    try {
                        boolean isOlder = isOlderThanLookback(line);
                        if (isOlder) {
                            // get starting position of line right after older
                            long pointer = accessFile.getFilePointer();
                            // get starting line for logging
                            line = accessFile.readLine();
                            System.out.println("Starting position: " + pointer + " at line: " + line);
                            return pointer;
                        }
                    } catch (ParseException e) {
                        // ignore line it doesn't contain a timestamp
                    }
                }
            }
        }
        return 0;
    }

    private boolean isOlderThanLookback(String line) throws ParseException {
        String timestampString = line.substring(0, line.indexOf("]") + 1);
        Date logDate = logTimestampFormat.parse(timestampString);
        return logDate.before(lookbackDate);
    }

    private boolean isNewerThanLookback(String line) throws ParseException {
        String timestampString = line.substring(0, line.indexOf("]") + 1);
        Date logDate = logTimestampFormat.parse(timestampString);
        return logDate.after(lookbackDate);
    }

    private void processDonation(String line, String name) {
        System.out.println(line);
        String timestamp = line.substring(1, 25); // Extracting the timestamp

        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            int pp = Integer.parseInt(matcher.group(1));
            int gp = Integer.parseInt(matcher.group(2));
            int sp = Integer.parseInt(matcher.group(3));
            int cp = Integer.parseInt(matcher.group(4));
            logDatabase.insertDonation(timestamp, name, pp, gp, sp, cp);
        }
    }


    private boolean isNpcTransactionMessage(String line) {
        return NPC_TRANSACTION_MESSAGE.matcher(line).find();
    }

    private String processLogLine(String line) {
        if (line.isEmpty()) {
            return null;
        }
        if (isNpcTransactionMessage(line)) {
            System.out.println("Ignoring merchant: " + line);
            return null; // Ignore this line
        }
        if (line.contains("adds some coins to the trade")) {
            System.out.println(line);
            int startIndex = line.indexOf(']') + 2;
            int endIndex = line.indexOf(" adds some coins to the trade");
            return Utils.capitalizeFirstLetter(line.substring(startIndex, endIndex).trim());
        }

        String other;
        String rawMessage;
        String message;
        JTextPane textPane;

        // Extract timestamp
        String timestamp = line.substring(1, 25); // Extracting the timestamp
        String humanizedTimestamp = Utils.humanizeTimestamp(timestamp);

        if (line.contains(" tells you, '")) {
            int startIndex = line.indexOf(']') + 2;
            int endIndex = line.indexOf(" tells you, '");
            other = Utils.capitalizeFirstLetter(line.substring(startIndex, endIndex).trim());
            if (other.contains(" ")) {
                return null;
            }
            rawMessage = line.substring(endIndex + 13, line.length() - 1);

            message = "<font color='blue'><b>" + other + ":</b> " + rawMessage + "</font>";
            if (!logDatabase.insertLog(timestamp, other, "You", rawMessage)) {
                return null;
            }
        } else if (line.contains("You told ")) {
            int startIndex = line.indexOf("You told ") + "You told ".length();
            int endIndex;
            if (line.contains(", '")) {
                endIndex = line.indexOf(", '");
                rawMessage = line.substring(endIndex + 3, line.length() - 1);
            } else { // Handle queued message
                endIndex = line.indexOf("'[queued], ");
                rawMessage = line.substring(endIndex + "'[queued], ".length(), line.length() - 1);
            }
            try {
                other = Utils.capitalizeFirstLetter(line.substring(startIndex, endIndex).trim());
            } catch (StringIndexOutOfBoundsException e) {
                other = "test";
                System.out.println(line);
                e.printStackTrace();
                System.exit(0);
            }

            message = "<font color='red'><b>You:</b> " + rawMessage + "</font>";
            if (!logDatabase.insertLog(timestamp, "You", other, rawMessage)) {
                return null;
            }
        } else {
            // If the line does not match the expected patterns
            return null;
        }

        textPane = mmorpgChatMonitor.getChatBoxes().putAndGetUserTextPane(other);

        // Format the message with the timestamp above and the message inline
        String formattedMessage = "<div data='" + timestamp + "' class='timestamp' style='text-align: center;'><strong>" + humanizedTimestamp + "</strong></div>" +
                "<div>" + message + "</div>";
        mmorpgChatMonitor.getChatBoxes().appendToPane(textPane, formattedMessage);
        mmorpgChatMonitor.getChatBoxes().getUserChatBox(other).setLocations(PortInference.findLocation(rawMessage));

        return null; // Return the receiver for any further processing
    }

    private static final Pattern pattern = Pattern.compile("(\\d+)\\sPP,\\s(\\d+)\\sGP,\\s(\\d+)\\sSP,\\s(\\d+)\\sCP");
}
