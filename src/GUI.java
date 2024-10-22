import Utilities.Token;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GUI extends JFrame {
    private JTextArea codeArea, tokensArea, parserArea, semanticArea, intermediateArea;
    private JButton scannerButton, parserButton, semanticButton, intermediateButton;
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem menuFileOpen, menuFileSave;
    private JFileChooser fileChooser;
    private final int WIDTH = 1500, HEIGHT = 800;
    private final Rectangle CODE_PANEL_DIMENSIONS = new Rectangle(50, 50, 600, 500);
    private final Rectangle TOKENS_PANEL_DIMENSIONS = new Rectangle(800, 50, 300, 500);
    private final Rectangle PARSER_PANEL_DIMENSIONS = new Rectangle(1170, 50, 300, 150);
    private final Rectangle SEMANTIC_PANEL_DIMENSIONS = new Rectangle(1170, 250, 300, 150);
    private final Rectangle INTERMEDIATE_PANEL_DIMENSIONS = new Rectangle(1170, 450, 300, 300);
    private final Rectangle SCANNER_BUTTON_DIMENSIONS = new Rectangle(670, 100, 120, 30);
    private final Rectangle PARSER_BUTTON_DIMENSIONS = new Rectangle(670, 150, 120, 30);
    private final Rectangle SEMANTIC_BUTTON_DIMENSIONS = new Rectangle(670, 200, 120, 30);
    private final Rectangle INTERMEDIATE_BUTTON_DIMENSIONS = new Rectangle(670, 250, 120, 30);

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

        JLabel parserLabel = new JLabel("Parser result", JLabel.CENTER);
        parserLabel.setForeground(Color.WHITE);
        parserLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        parserArea = new JTextArea();
        JScrollPane parserScroll = new JScrollPane(parserArea);
        JPanel parserPanel = new JPanel(new BorderLayout());
        parserPanel.setBounds(PARSER_PANEL_DIMENSIONS);
        parserPanel.setBackground(Color.DARK_GRAY);
        parserArea.setEditable(false);
        parserPanel.add(parserLabel, BorderLayout.NORTH);
        parserPanel.add(parserScroll, BorderLayout.CENTER);

        parserArea.setTabSize(2);
        parserArea.setFont(new Font("Courier New", Font.PLAIN, 20));

        JLabel semanticLabel = new JLabel("Semantic result", JLabel.CENTER);
        semanticLabel.setForeground(Color.WHITE);
        semanticLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        semanticArea = new JTextArea();
        JScrollPane semanticScroll = new JScrollPane(semanticArea);
        JPanel semanticPanel = new JPanel(new BorderLayout());
        semanticPanel.setBounds(SEMANTIC_PANEL_DIMENSIONS);
        semanticPanel.setBackground(Color.DARK_GRAY);
        semanticArea.setEditable(false);
        semanticPanel.add(semanticLabel, BorderLayout.NORTH);
        semanticPanel.add(semanticScroll, BorderLayout.CENTER);

        semanticArea.setTabSize(2);
        semanticArea.setFont(new Font("Courier New", Font.PLAIN, 20));

        JLabel intermediateLabel = new JLabel("Intermediate code", JLabel.CENTER);
        intermediateLabel.setForeground(Color.WHITE);
        intermediateLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        intermediateArea = new JTextArea();
        JScrollPane intermediateScroll = new JScrollPane(intermediateArea);
        JPanel intermediatePanel = new JPanel(new BorderLayout());
        intermediatePanel.setBounds(INTERMEDIATE_PANEL_DIMENSIONS);
        intermediatePanel.setBackground(Color.DARK_GRAY);
        intermediateArea.setEditable(false);
        intermediatePanel.add(intermediateLabel, BorderLayout.NORTH);
        intermediatePanel.add(intermediateScroll, BorderLayout.CENTER);

        intermediateArea.setTabSize(2);
        intermediateArea.setFont(new Font("Courier New", Font.PLAIN, 20));

        scannerButton = new JButton("Scanner");
        scannerButton.setBounds(SCANNER_BUTTON_DIMENSIONS);
        scannerButton.setFont(new Font("Dialog", Font.BOLD, 16));

        parserButton = new JButton("Parser");
        parserButton.setBounds(PARSER_BUTTON_DIMENSIONS);
        parserButton.setFont(new Font("Dialog", Font.BOLD, 16));
        parserButton.setEnabled(false);

        semanticButton = new JButton("Semantic");
        semanticButton.setBounds(SEMANTIC_BUTTON_DIMENSIONS);
        semanticButton.setFont(new Font("Dialog", Font.BOLD, 16));
        semanticButton.setEnabled(false);

        intermediateButton = new JButton("Int code");
        intermediateButton.setBounds(INTERMEDIATE_BUTTON_DIMENSIONS);
        intermediateButton.setFont(new Font("Dialog", Font.BOLD, 16));
        intermediateButton.setEnabled(false);

        add(codePanel);
        add(tokensPanel);
        add(parserPanel);
        add(semanticPanel);
        add(intermediatePanel);
        add(scannerButton);
        add(parserButton);
        add(semanticButton);
        add(intermediateButton);
    }

    public void showTokens(ArrayList<String> strings, ArrayList<Token> tokens, Boolean[] reservedWords) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            stringBuilder.append(strings.get(i)).append(" , ")
                    .append(reservedWords[i] ? Token.RW + " " : "")
                    .append(tokens.get(i))
                    .append("\n");
        }
        tokensArea.setText(stringBuilder.toString());
    }

    public void showParserResult(boolean result) {
        parserArea.setText(result ? "Program OK" : "Syntax error");
    }

    public void showSemanticResult(boolean result) {
        semanticArea.setText(result ? "Program OK" : "Semantic error");
    }

    public void showIntermediateCode(String intermediateCode) {
        intermediateArea.setText(intermediateCode);
    }

    public void clearTokens() {
        tokensArea.setText("");
    }

    public void clearParserResult() {
        parserArea.setText("");
    }

    public void clearSemanticResult() {
        semanticArea.setText("");
    }

    public void clearIntermediateCode() {
        intermediateArea.setText("");
    }

    public void setParserButtonState(boolean state) {
        parserButton.setEnabled(state);
    }

    public void setSemanticButtonState(boolean state) {
        semanticButton.setEnabled(state);
    }

    public void setIntermediateButtonState(boolean state) {
        intermediateButton.setEnabled(state);
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public JTextArea getCodeArea() {
        return codeArea;
    }

    public JButton getScannerButton() {
        return scannerButton;
    }

    public JButton getParserButton() {
        return parserButton;
    }

    public JButton getSemanticButton() {
        return semanticButton;
    }

    public JButton getIntermediateButton() {
        return intermediateButton;
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
