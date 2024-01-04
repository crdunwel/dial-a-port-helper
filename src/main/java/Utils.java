import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String toHexString(Color color) {
        // Format the color components as hexadecimal strings
        String red = Integer.toHexString(color.getRed());
        String green = Integer.toHexString(color.getGreen());
        String blue = Integer.toHexString(color.getBlue());

        // Pad with zero if necessary to ensure each component is two characters
        if (red.length() == 1) {
            red = "0" + red;
        }
        if (green.length() == 1) {
            green = "0" + green;
        }
        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        // Concatenate the components with a '#' prefix
        return "#" + red + green + blue;
    }

    public static String humanizeTimestamp(String timestamp) {
        SimpleDateFormat logTimestampFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
        try {
            Date messageDate = logTimestampFormat.parse(timestamp);
            long difference = new Date().getTime() - messageDate.getTime();

            long seconds = difference / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "Less than 1 minute ago";
            } else if (minutes < 60) {
                return minutes + " minutes ago";
            } else if (hours < 24) {
                return hours + " hours ago";
            } else if (days < 7) {
                return days + " days ago";
            } else {
                // For periods longer than a week, return the date in a readable format
                return new SimpleDateFormat("MMM dd, yyyy").format(messageDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp; // Fallback to original timestamp if parsing fails
    }

    public static Color getColorWithOpacity(String hexColor, float opacity) {
        // Parse the hexadecimal color string
        int color = Integer.parseInt(hexColor.substring(1), 16);

        // Extract the red, green, and blue components
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Convert the opacity from a float to an integer alpha value
        int alpha = (int) (opacity * 255);

        System.out.println(new Color(red, green, blue, alpha).getAlpha());

        // Create and return the new color with alpha
        return new Color(red, green, blue, alpha);
    }
}
