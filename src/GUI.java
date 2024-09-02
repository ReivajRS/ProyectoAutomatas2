import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GUI extends JFrame {
    private JTextArea codeArea, tokensArea;
    private JButton scannerButton;
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem menuFileOpen, menuFileSave;
    private JFileChooser fileChooser;
    private final int WIDTH = 1280, HEIGHT = 720;
    private final Rectangle CODE_PANEL_DIMENSIONS = new Rectangle(50, 50, 600, 500);
    private final Rectangle TOKENS_PANEL_DIMENSIONS = new Rectangle(800, 50, 300, 500);
    private final Rectangle SCANNER_BUTTON_DIMENSIONS = new Rectangle(670, 100, 100, 30);

    public GUI() {
        makeInterface();
        setVisible(true);
    }

    private void makeInterface() {
        setTitle("Compiler");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuFile = new JMenu("File");
        menuBar.add(menuFile);
        menuFileOpen = new JMenuItem("Open");
        menuFileSave = new JMenuItem("Save");
        menuFile.add(menuFileOpen);
        menuFile.add(menuFileSave);

        fileChooser = new JFileChooser();

        JLabel codeLabel = new JLabel("Code", JLabel.CENTER);
        codeLabel.setForeground(Color.WHITE);
        codeLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        codeArea = new JTextArea();
        JScrollPane codeScroll = new JScrollPane(codeArea);
        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setBounds(CODE_PANEL_DIMENSIONS);
        codePanel.setBackground(Color.DARK_GRAY);
        codePanel.add(codeLabel, BorderLayout.NORTH);
        codePanel.add(codeScroll, BorderLayout.CENTER);

        codeArea.setTabSize(2);
        codeArea.setFont(new Font("Courier New", Font.PLAIN, 20));

        JLabel tokensLabel = new JLabel("Tokens", JLabel.CENTER);
        tokensLabel.setForeground(Color.WHITE);
        tokensLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        tokensArea = new JTextArea();
        JScrollPane tokensScroll = new JScrollPane(tokensArea);
        JPanel tokensPanel = new JPanel(new BorderLayout());
        tokensPanel.setBounds(TOKENS_PANEL_DIMENSIONS);
        tokensPanel.setBackground(Color.DARK_GRAY);
        tokensArea.setEditable(false);
        tokensPanel.add(tokensLabel, BorderLayout.NORTH);
        tokensPanel.add(tokensScroll, BorderLayout.CENTER);

        tokensArea.setTabSize(2);
        tokensArea.setFont(new Font("Courier New", Font.PLAIN, 20));

        scannerButton = new JButton("Scanner");
        scannerButton.setBounds(SCANNER_BUTTON_DIMENSIONS);
        scannerButton.setFont(new Font("Dialog", Font.BOLD, 16));

        add(codePanel);
        add(tokensPanel);
        add(scannerButton);
    }

    public void showTokens(String[] inputArray, Token[] tokens, Boolean[] reservedWords) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < inputArray.length; i++) {
            stringBuilder.append(inputArray[i]).append(", ")
                    .append(reservedWords[i] ? Token.RW + " " : "")
                    .append(tokens[i])
                    .append("\n");
        }
        tokensArea.setText(stringBuilder.toString());
    }

    public void showTokens2(ArrayList<String> strings, ArrayList<Token> tokens, Boolean[] reservedWords) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            stringBuilder.append(strings.get(i)).append(" , ")
                    .append(reservedWords[i] ? Token.RW + " " : "")
                    .append(tokens.get(i))
                    .append("\n");
        }
        tokensArea.setText(stringBuilder.toString());
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public JTextArea getCodeArea() {
        return codeArea;
    }

    public JTextArea getTokensArea() {
        return tokensArea;
    }

    public JButton getScannerButton() {
        return scannerButton;
    }

    public JMenuItem getMenuFileOpen() {
        return menuFileOpen;
    }

    public JMenuItem getMenuFileSave() {
        return menuFileSave;
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }
}
