import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ChatBoxes {
    private final int ANIMATION_DELAY = 10; // Delay in milliseconds between animation updates
    private final int ANIMATION_DURATION = 100; // Total duration of the animation in milliseconds

    private final Map<String, UserChatBox> chatboxes;

    private int nextWindowX;
    private int nextWindowY;
    private Timer simulationTimer;
    private MMORPGChatMonitor mmorpgChatMonitor;

    private LogDatabase logDatabase;

    public ChatBoxes(MMORPGChatMonitor mmorpgChatMonitor) {
        this.mmorpgChatMonitor = mmorpgChatMonitor;
        this.chatboxes = new LinkedHashMap<>();
        this.logDatabase = new LogDatabase();
        this.nextWindowX = getSettings().getBoundingArea().x;
        this.nextWindowY = getSettings().getBoundingArea().y;

        // Set up a timer to update the timestamp every second (1000 milliseconds)
        Timer timer = new Timer(30000, e -> {
            for (JTextPane tp : chatboxes.values().stream().map(UserChatBox::getUserTextPane).collect(Collectors.toList())) {
                HTMLDocument doc = (HTMLDocument) tp.getDocument();
                ElementIterator it = new ElementIterator(doc);
                Element elem;
                while ((elem = it.next()) != null) {
                    AttributeSet as = elem.getAttributes();
                    if (as.getAttribute(HTML.Attribute.CLASS) != null && as.getAttribute(HTML.Attribute.CLASS).equals("timestamp")) {
                        String timestampStr = (String) as.getAttribute(HTML.Attribute.DATA);
                        if (timestampStr != null) {
                            try {
                                Element strongElem = elem.getElement(0);
                                if (strongElem != null) {
                                    String humanizedTime = Utils.humanizeTimestamp(timestampStr);
                                    String updatedHTML = "<strong>" + humanizedTime + "</strong>";
                                    doc.setOuterHTML(strongElem, updatedHTML);
                                }
                            } catch (NumberFormatException | BadLocationException | IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        timer.start();
    }

    private Settings getSettings() {
        return mmorpgChatMonitor.getSettings();
    }

    public void destroyAllFrames() {
        chatboxes.values().stream().map(UserChatBox::getFrame).forEach(Window::dispose);
        chatboxes.clear();
        this.nextWindowX = getSettings().getBoundingArea().x;
        this.nextWindowY = getSettings().getBoundingArea().y;
    }

    public JTextPane createTextPaneForUser(String userName) {
        JTextPane textPane = new JTextPane();
        textPane.setDoubleBuffered(true);
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText("<html><body style='font-family: Arial, sans-serif;'></body></html>");

        // textPane.setBackground(Color.decode(getSettings().getBackgroundColor())); // Set the default background color
        // textPane.setOpaque(true);
        textPane.setBackground(Color.decode(getSettings().getBackgroundColor())); // Set the default background color

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(scrollPane);
        frame.setSize(getSettings().getWindowWidth(), getSettings().getWindowHeight());
        frame.setUndecorated(true); // Make the JFrame undecorated
        frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE); // Ensure no decoration

        // Set the window to always stay on top
        frame.setAlwaysOnTop(true);

        // Create custom title bar
        JPanel titleBar = new JPanel();
        titleBar.setPreferredSize(new Dimension(getSettings().getWindowWidth(), 20));
        titleBar.setBackground(Color.LIGHT_GRAY); // Choose color for the title bar
        JLabel titleLabel = new JLabel(userName);
        titleBar.add(titleLabel);

        // Mouse listener to track drag movements
        MouseAdapter ma = new MouseAdapter() {
            private Point initialClick;

            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                titleBar.getComponentAt(initialClick);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // get location of Window
                int thisX = frame.getLocation().x;
                int thisY = frame.getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
                int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                frame.setLocation(X, Y);
            }
        };

        titleBar.addMouseListener(ma);
        titleBar.addMouseMotionListener(ma);

        // Add title bar to the frame
        frame.add(titleBar, BorderLayout.NORTH);

        // Mouse listener for Alt+Click
        MouseListener altClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check if the Shift key is down when the mouse is clicked
                if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK && e.getButton() == MouseEvent.BUTTON3) {
                    chatboxes.get(userName).toggleHighlight();
                }
                // Check if the Shift key is down when the mouse is clicked
                else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) {
                    JFrame frameToDispose = getFrameFromComponent(e.getComponent());
                    if (frameToDispose != null) {
                        fadeOutAndDispose(frameToDispose);
                        chatboxes.remove(userName);
                        rearrangeFrames();
                        // chatboxes.remove(userName);
                    }
                }
            }
        };

        // Add the mouse listener to the components
        textPane.addMouseListener(altClickListener);
        scrollPane.addMouseListener(altClickListener);
        titleBar.addMouseListener(altClickListener);

        UserChatBox chatbox = new UserChatBox(userName, textPane, frame, titleBar);
        chatboxes.put(userName, chatbox);
        int tipperStatus = logDatabase.getTipperStatus(userName);
        if (tipperStatus != 0) {
            if (tipperStatus > 0) {
                chatbox.highlightGood();
            } else {
                chatbox.highlightBad();
            }
        }
        positionWindow(frame);

        return textPane;
    }

    private void positionWindow(JFrame frame) {
        // Calculate the number of windows that can fit horizontally within the bounding area
        int maxHorizontalWindows = getSettings().getBoundingArea().width / (getSettings().getWindowWidth() + getSettings().getWindowMargin());
        int maxVerticalWindows = getSettings().getBoundingArea().height / (getSettings().getWindowHeight() + getSettings().getWindowMargin());

        // If we exceed the bounding area's height, reset to the top edge
        if (nextWindowY + getSettings().getWindowHeight() > getSettings().getBoundingArea().y + getSettings().getBoundingArea().height) {
            frame.setLocation(nextWindowX, nextWindowY);
            frame.setVisible(false);
            return;
        }

        // Set the location for the new window
        frame.setLocation(nextWindowX, nextWindowY);

        // Increment the X position for the next window
        nextWindowX += getSettings().getWindowWidth() + getSettings().getWindowMargin();

        // If the next window position exceeds the horizontal window count, reset X and increment Y
        if (nextWindowX + getSettings().getWindowWidth() > getSettings().getBoundingArea().x +  getSettings().getBoundingArea().width) {
            nextWindowX = getSettings().getBoundingArea().x; // Reset to the left edge
            nextWindowY +=  getSettings().getWindowHeight() + getSettings().getWindowMargin(); // Move down
        }
        frame.setVisible(true);
    }

    public void appendToPane(JTextPane tp, String msg) {
        HTMLDocument doc = (HTMLDocument) tp.getDocument();
        try {
            // Insert the formatted message before the closing body tag to maintain the structure
            Element body = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY);
            doc.insertBeforeStart(body, msg);
            tp.setCaretPosition(tp.getDocument().getLength());
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void rearrangeFrames() {
        nextWindowX = getSettings().getBoundingArea().x;
        nextWindowY = getSettings().getBoundingArea().y;

        for (JFrame frame : chatboxes.values().stream().map(UserChatBox::getFrame).collect(Collectors.toList())) {
            Point startPosition = frame.getLocation();
            Point endPosition = new Point(nextWindowX, nextWindowY);

            // Animate the movement to the new position
            animateMovement(frame, startPosition, endPosition);

            positionWindow(frame);
        }
    }

    private void fadeOutAndDispose(JFrame frame) {
        final float[] opacity = {1.0f};
        final int delay = 10; // Milliseconds between each timer call
        final float opacityDecrease = 0.10f; // Decrease opacity by this amount each time

        ActionListener fadeOutListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity[0] -= opacityDecrease;
                if (opacity[0] <= 0) {
                    ((Timer) e.getSource()).stop();
                    frame.dispose();
                } else {
                    frame.setOpacity(opacity[0]);
                }
            }
        };

        new Timer(delay, fadeOutListener).start();
    }

    private void animateMovement(JFrame frame, Point startPosition, Point endPosition) {
        final int steps = ANIMATION_DURATION / ANIMATION_DELAY;
        final double dx = (double)(endPosition.x - startPosition.x) / steps;
        final double dy = (double)(endPosition.y - startPosition.y) / steps;

        new Timer(ANIMATION_DELAY, new ActionListener() {
            private int currentStep = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStep < steps) {
                    frame.setLocation((int) (startPosition.x + dx * currentStep),
                            (int) (startPosition.y + dy * currentStep));
                    currentStep++;
                } else {
                    ((Timer) e.getSource()).stop();
                    frame.setLocation(endPosition); // Ensure the frame ends exactly at the end position
                }
            }
        }).start();
    }

    public static JFrame getFrameFromComponent(Component component) {
        while (component != null) {
            if (component instanceof JFrame) {
                return (JFrame) component;
            }
            component = component.getParent();
        }
        return null; // Returns null if the component is not contained within a JFrame
    }

    public Timer getSimulationTimer() {
        return simulationTimer;
    }

    public void stopSimulation() {
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        destroyAllFrames();
    }

    public void simulateIncomingMessages() {
        final int messageInterval = 1000; // One message per 1000 milliseconds (1 second)
        final Random random = new Random();
        final String[] sampleUsers = {"User1", "User2", "User3", "User4", "User5", "User6", "User7", "User8", "User9", "User10", "User11", "User12", "User13", "User14", "User15", "User16"};
        final String[] sampleMessages = {
                "Hello, world!", "This is a test message.", "How are you?", "Can I get a port!",
                "Port WC > LS?", "Could I get a port BB to WC", "Port from NK to steamfont",
                "Port to Nek?", "Port from Nek?", "Hey there, pickup at surefall to WC?",
                "Pickup at LS to steamfont?", "Port to Ferrot?"
        };

        simulationTimer = new Timer(messageInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Choose a random user and message
                String user = sampleUsers[random.nextInt(sampleUsers.length)];
                String message = sampleMessages[random.nextInt(sampleMessages.length)];

                // Create a text pane for the user if it does not exist

                JTextPane textPane;
                if (!chatboxes.containsKey(user)) {
                    textPane = createTextPaneForUser(user);
                } else {
                    textPane = chatboxes.get(user).getUserTextPane();
                }

                // Append the message to the text pane
                appendToPane(textPane, "<p>" + message + "</p>");
                chatboxes.get(user).setLocations(PortInference.findLocation(message));
            }
        });
    }

    public JTextPane putAndGetUserTextPane(String user) {
        JTextPane textPane;
        if (!chatboxes.containsKey(user)) {
            textPane = createTextPaneForUser(user);
            if (getSettings().isPlaySound() && (getSettings().getMsgSoundFilePath() != null || !"".equals(mmorpgChatMonitor.getSettings().getMsgSoundFilePath()))) {
                SoundPlayer.playSound(mmorpgChatMonitor.getSettings().getMsgSoundFilePath());
            }
        } else {
            textPane = chatboxes.get(user).getUserTextPane();
        }
        return textPane;
    }

    public UserChatBox getUserChatBox(String user) {
        return chatboxes.get(user);
    }

    public class UserChatBox {
        private String userName;
        private JTextPane userTextPane;
        private JFrame frame;
        private JPanel titleBar;
        private JPanel locFooterBar = null;
        private int tipperStatus;

        private PortInference.PortLocationTuple locations;

        public UserChatBox(String userName, JTextPane userTextPane, JFrame frame, JPanel titleBar) {
            this.userName = userName;
            this.userTextPane = userTextPane;
            this.frame = frame;
            this.titleBar = titleBar;
        }

        public JTextPane getUserTextPane() {
            return userTextPane;
        }

        public void setUserTextPane(JTextPane userTextPane) {
            this.userTextPane = userTextPane;
        }

        public JFrame getFrame() {
            return frame;
        }

        public void setFrame(JFrame frame) {
            this.frame = frame;
        }

        public JPanel getTitleBar() {
            return titleBar;
        }

        public void setTitleBar(JPanel titleBar) {
            this.titleBar = titleBar;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getTipperStatus() {
            return tipperStatus;
        }

        public void setTipperStatus(int tipperStatus) {
            this.tipperStatus = tipperStatus;
        }

        public void toggleHighlight() {
            if (getTipperStatus() == 0) {
                highlightGood();
            } else if (getTipperStatus() > 0) {
                highlightBad();
            } else if (getTipperStatus() < 0) {
                noHighlight();
            }
        }

        public void noHighlight() {
            tipperStatus = 0;
            logDatabase.setTipperStatus(userName, 0);
            userTextPane.setBackground(Color.decode(getSettings().getBackgroundColor()));
            titleBar.setBackground(Color.decode(getSettings().getBackgroundColor()).darker());
        }

        public void highlightGood() {
            tipperStatus = 1;
            logDatabase.setTipperStatus(userName, 1);
            userTextPane.setBackground(Color.decode(getSettings().getHighlightColor()));
            titleBar.setBackground(Color.decode(getSettings().getHighlightColor()).darker());
        }

        public void highlightBad() {
            tipperStatus = -1;
            logDatabase.setTipperStatus(userName, -1);
            userTextPane.setBackground(Color.decode(getSettings().getHighlightColorBad()));
            titleBar.setBackground(Color.decode(getSettings().getHighlightColorBad()).darker());
        }

        public PortInference.PortLocationTuple getLocations() {
            return locations;
        }

        public void setLocations(PortInference.PortLocationTuple locations) {
            this.locations = locations;
            if (locFooterBar == null) {
                locFooterBar = new JPanel();
                locFooterBar.setPreferredSize(new Dimension(getSettings().getWindowWidth(), 20)); // Set the size of the footer bar
                locFooterBar.setBackground(Color.LIGHT_GRAY); // Choose color for the footer bar
                JLabel footerLabel = new JLabel(""); // Replace "Footer Text" with your desired text
                locFooterBar.add(footerLabel);
                frame.add(locFooterBar, BorderLayout.SOUTH);
            }
            JLabel retrievedLabel = (JLabel) locFooterBar.getComponent(0);
            if (retrievedLabel != null && locations != null) {
                retrievedLabel.setText(locations.toString());
            }
        }
    }
}
