import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class DialSystemTray {

    private MMORPGChatMonitor mmorpgChatMonitor;
    private Settings backupSettings;
    private Settings simulationSettings;

    private JWindow overlayWindow;

    public DialSystemTray(MMORPGChatMonitor mmorpgChatMonitor) {
        this.mmorpgChatMonitor = mmorpgChatMonitor;
    }

    private CheckboxMenuItem logReaderItem;

    private Settings getSettings() {
        return this.mmorpgChatMonitor.getSettings();
    }

    private void openSettingsWindow() {
        JDialog settingsDialog = new JDialog((Frame) null, "Settings", false); // Set the dialog to be non-modal
        // settingsDialog.setLayout(new BoxLayout(settingsDialog.getContentPane(), BoxLayout.Y_AXIS));

        // Create a new JPanel with BoxLayout and set it as the content pane
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        settingsDialog.setContentPane(contentPane);

        int padding = 15; // Adjust this value as needed for padding
        contentPane.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));


        // Log File Path Panel
        JPanel logFilePathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblLogFilePath = new JLabel("Log File Path:");
        JTextField logFilePathField = new JTextField(getSettings().getLogFilePath(), 20);
        JButton chooseFileButton = new JButton("Choose File");
        chooseFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(settingsDialog) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                logFilePathField.setText(selectedFile.getAbsolutePath());
            }
        });
        logFilePathPanel.add(lblLogFilePath);
        logFilePathPanel.add(logFilePathField);
        logFilePathPanel.add(chooseFileButton);
        settingsDialog.add(logFilePathPanel);

        // Window Width Panel
        JPanel windowWidthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblWindowWidth = new JLabel("Window Width:");
        JTextField windowWidthField = new JTextField(String.valueOf(getSettings().getWindowWidth()), 5);
        windowWidthPanel.add(lblWindowWidth);
        windowWidthPanel.add(windowWidthField);
        settingsDialog.add(windowWidthPanel);

        // Window Height Panel
        JPanel windowHeightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblWindowHeight = new JLabel("Window Height:");
        JTextField windowHeightField = new JTextField(String.valueOf(getSettings().getWindowHeight()), 5);
        windowHeightPanel.add(lblWindowHeight);
        windowHeightPanel.add(windowHeightField);
        settingsDialog.add(windowHeightPanel);

        // Window Margin Panel
        JPanel windowMarginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblWindowMargin= new JLabel("Window Margin:");
        JTextField windowMarginField = new JTextField(String.valueOf(getSettings().getWindowMargin()), 5);
        windowMarginPanel.add(lblWindowMargin);
        windowMarginPanel.add(windowMarginField);
        settingsDialog.add(windowMarginPanel);

        // Bounding Box Panel
        JPanel boundingBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblBoundingBox = new JLabel("Bounding Box (x, y, width, height):");
        JTextField boundingBoxField = new JTextField(String.format("%d, %d, %d, %d",
                getSettings().getBoundingArea().x,
                getSettings().getBoundingArea().y,
                getSettings().getBoundingArea().width,
                getSettings().getBoundingArea().height),
                15);
        boundingBoxPanel.add(lblBoundingBox);
        boundingBoxPanel.add(boundingBoxField);
        settingsDialog.add(boundingBoxPanel);

        // Highlight Color Panel
        JPanel highlightColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHighlightColor = new JLabel("Highlight Color:");
        JPanel colorPreview = new JPanel();
        colorPreview.setBackground(Color.decode(getSettings().getHighlightColor()));
        colorPreview.setPreferredSize(new Dimension(25, 25));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JButton colorButton = new JButton("Choose Color");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(settingsDialog, "Choose Highlight Color",
                    Color.decode(getSettings().getHighlightColor()));
            if (newColor != null) {
                colorPreview.setBackground(newColor);
            }
        });
        highlightColorPanel.add(lblHighlightColor);
        highlightColorPanel.add(colorPreview);
        highlightColorPanel.add(colorButton);
        settingsDialog.add(highlightColorPanel);

        // Background Color Panel
        JPanel bgtColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblBgColor = new JLabel("Background Color:");
        JPanel bgColorPreview = new JPanel();
        bgColorPreview.setBackground(Color.decode(getSettings().getBackgroundColor()));
        bgColorPreview.setPreferredSize(new Dimension(25, 25));
        bgColorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JButton bgColorButton = new JButton("Choose Color");
        bgColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(settingsDialog, "Choose Background Color",
                    Color.decode(getSettings().getBackgroundColor()));
            if (newColor != null) {
                bgColorPreview.setBackground(newColor);
            }
        });
        bgtColorPanel.add(lblBgColor);
        bgtColorPanel.add(bgColorPreview);
        bgtColorPanel.add(bgColorButton);
        settingsDialog.add(bgtColorPanel);

        // TODO
        // Background Opacity
//        JPanel bgOpacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JLabel lblBgOpacityuMargin= new JLabel("Background Opacity:");
//        JTextField bgOpacityField = new JTextField(String.valueOf(getSettings().getBackgroundOpacity()), 5);
//        bgOpacityPanel.add(lblBgOpacityuMargin);
//        bgOpacityPanel.add(bgOpacityField);
//        settingsDialog.add(bgOpacityPanel);

        // Lookback Seconds Panel
        JPanel lookbackSecondsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblLookbackSeconds = new JLabel("Lookback Seconds:");
        // Increase the column size for more space
        JTextField lookbackSecondsField = new JTextField(String.valueOf(getSettings().getLookbackSeconds()), 10); // Adjusted column size
        lookbackSecondsPanel.add(lblLookbackSeconds);
        lookbackSecondsPanel.add(lookbackSecondsField);
        settingsDialog.add(lookbackSecondsPanel);

        // Simulation Toggle Button
        JButton toggleSimulationButton = new JButton("Start Simulation");
        toggleSimulationButton.addActionListener(e -> {
            mmorpgChatMonitor.getChatBoxes().destroyAllFrames();
            backupSettings = mmorpgChatMonitor.getSettings();
            simulationSettings = new Settings();
            simulationSettings.setLogFilePath(logFilePathField.getText());
            simulationSettings.setWindowHeight(Integer.parseInt(windowHeightField.getText()));
            simulationSettings.setWindowWidth(Integer.parseInt(windowWidthField.getText()));
            String[] bboxValues = boundingBoxField.getText().split(",");
            if (bboxValues.length == 4) {
                simulationSettings.setBoundingArea(new Rectangle(
                        Integer.parseInt(bboxValues[0].trim()),
                        Integer.parseInt(bboxValues[1].trim()),
                        Integer.parseInt(bboxValues[2].trim()),
                        Integer.parseInt(bboxValues[3].trim())
                ));
            }
            simulationSettings.setHighlightColorBad("#FFCCC"); // TODO add settings
            simulationSettings.setHighlightColor(Utils.toHexString(colorPreview.getBackground()));
            simulationSettings.setBackgroundColor(Utils.toHexString(bgColorPreview.getBackground()));
            // simulationSettings.setBackgroundOpacity(Float.parseFloat(bgOpacityField.getText()));
            simulationSettings.setLookbackSeconds(Integer.parseInt(lookbackSecondsField.getText()));
            simulationSettings.setWindowMargin(Integer.parseInt(windowMarginField.getText()));

            if (toggleSimulationButton.getText().equals("Start Simulation")) {
                logReaderItem.setState(false);
                mmorpgChatMonitor.stopMonitoring();
                mmorpgChatMonitor.setSettings(simulationSettings);
                if (mmorpgChatMonitor.getChatBoxes().getSimulationTimer() == null) {
                    mmorpgChatMonitor.getChatBoxes().simulateIncomingMessages(); // Initialize the timer if not already done
                }
                mmorpgChatMonitor.getChatBoxes().getSimulationTimer().start();
                if (overlayWindow == null) {
                    createOverlayWindow();
                }
                overlayWindow.setVisible(true);
                toggleSimulationButton.setText("Stop Simulation");
            } else {
                mmorpgChatMonitor.getChatBoxes().stopSimulation();
                mmorpgChatMonitor.setSettings(backupSettings);
                overlayWindow.dispose();
                overlayWindow = null;
                toggleSimulationButton.setText("Start Simulation");
            }
        });

        // Center the toggle button
        JPanel simulationButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        simulationButtonPanel.add(toggleSimulationButton);
        settingsDialog.add(simulationButtonPanel);

        // Save Button
        JPanel saveButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            mmorpgChatMonitor.getChatBoxes().stopSimulation();
            if (overlayWindow != null) {
                overlayWindow.dispose();
            }
            overlayWindow = null;
            toggleSimulationButton.setText("Stop Simulation");
            if (backupSettings != null) {
                mmorpgChatMonitor.setSettings(backupSettings);
            }
            getSettings().setLogFilePath(logFilePathField.getText());
            getSettings().setWindowHeight(Integer.parseInt(windowHeightField.getText()));
            getSettings().setWindowWidth(Integer.parseInt(windowWidthField.getText()));
            getSettings().setWindowMargin(Integer.parseInt(windowMarginField.getText()));
            String[] bboxValues = boundingBoxField.getText().split(",");
            if (bboxValues.length == 4) {
                getSettings().setBoundingArea(new Rectangle(
                        Integer.parseInt(bboxValues[0].trim()),
                        Integer.parseInt(bboxValues[1].trim()),
                        Integer.parseInt(bboxValues[2].trim()),
                        Integer.parseInt(bboxValues[3].trim())
                ));
            }
            getSettings().setHighlightColorBad(getSettings().getHighlightColorBad());
            getSettings().setHighlightColor(Utils.toHexString(colorPreview.getBackground()));
            getSettings().setBackgroundColor(Utils.toHexString(bgColorPreview.getBackground()));
            // getSettings().setBackgroundOpacity(Float.parseFloat(bgOpacityField.getText()));
            getSettings().setLookbackSeconds(Integer.parseInt(lookbackSecondsField.getText()));
            mmorpgChatMonitor.saveSettings(getSettings());
            logReaderItem.setState(false);
            mmorpgChatMonitor.stopMonitoring();
            settingsDialog.dispose();
        });
        saveButtonPanel.add(saveButton);
        contentPane.add(saveButtonPanel);

        settingsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mmorpgChatMonitor.getChatBoxes().stopSimulation();
                if (overlayWindow != null) {
                    overlayWindow.dispose();
                }
                overlayWindow = null;
                toggleSimulationButton.setText("Stop Simulation");
                if (backupSettings != null) {
                    mmorpgChatMonitor.setSettings(backupSettings);
                }
            }
        });

        settingsDialog.pack();
        settingsDialog.setLocationRelativeTo(null); // Center on screen
        settingsDialog.setVisible(true);
    }
    public void initSystemTray() {
        // Check if the system tray is supported on the platform
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported.");
            return;
        }

        // Create a popup menu for the system tray icon
        PopupMenu popup = new PopupMenu();

        // Create a CheckboxMenuItem for the Start Log Reader
        logReaderItem = new CheckboxMenuItem("Start Log Reader");
        logReaderItem.addItemListener(e -> {
            boolean checked = logReaderItem.getState();
            // Add your code here to handle the toggle
            if (checked) {
                // Start monitoring in a new thread
                new Thread(() -> {
                    try {
                        mmorpgChatMonitor.startMonitoring();
                    } catch (IOException | InterruptedException ex) {
                        // Handle exceptions here, e.g., logging or showing an error message
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                // Stop monitoring
                mmorpgChatMonitor.stopMonitoring();
            }
        });

        // Add the Log Reader item at the top
        popup.add(logReaderItem);

        // Add a separator
        popup.addSeparator();

        // Create and add other menu items
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.addActionListener(e -> openSettingsWindow());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        popup.add(settingsItem);
        popup.add(exitItem);

        // Assuming you have an image icon for the tray
        Image trayIconImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("sysicon.png"));
        TrayIcon trayIcon = new TrayIcon(trayIconImage, "MMORPG Teleporter", popup);
        trayIcon.setImageAutoSize(true);

        // Add the tray icon to the system tray
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added to the system tray.");
            e.printStackTrace();
        }
    }

    private void createOverlayWindow() {
        overlayWindow = new JWindow();
        overlayWindow.setSize(getSettings().getBoundingArea().width, getSettings().getBoundingArea().height);
        overlayWindow.setLocation(getSettings().getBoundingArea().x, getSettings().getBoundingArea().y);
        overlayWindow.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        overlayWindow.setAlwaysOnTop(true);

        // Custom painting
        overlayWindow.add(new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.YELLOW);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1); // Draw yellow rectangle
            }
        });
    }

}
