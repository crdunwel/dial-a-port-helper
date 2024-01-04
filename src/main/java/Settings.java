import java.awt.*;

public class Settings {
    private String logFilePath;
    private int windowWidth;
    private int windowHeight;
    private int windowMargin = 5; // Margin between windows
    private Rectangle boundingArea;
    private String highlightColor; // For Gson, use String to represent color
    private String backgroundColor;
    private float backgroundOpacity;
    private long lookbackSeconds;
    private String highlightColorBad;

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public Rectangle getBoundingArea() {
        return boundingArea;
    }

    public void setBoundingArea(Rectangle boundingArea) {
        this.boundingArea = boundingArea;
    }

    public String getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
    }

    public long getLookbackSeconds() {
        return lookbackSeconds;
    }

    public void setLookbackSeconds(long lookbackSeconds) {
        this.lookbackSeconds = lookbackSeconds;
    }

    public int getWindowMargin() {
        return windowMargin;
    }

    public void setWindowMargin(int windowMargin) {
        this.windowMargin = windowMargin;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public float getBackgroundOpacity() {
        return backgroundOpacity;
    }

    public void setBackgroundOpacity(float backgroundOpacity) {
        this.backgroundOpacity = backgroundOpacity;
    }

    public String getHighlightColorBad() {
        return highlightColorBad;
    }

    public void setHighlightColorBad(String hightlightColorBad) {
        this.highlightColorBad = hightlightColorBad;
    }
}