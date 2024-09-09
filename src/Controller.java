import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Controller implements ActionListener, KeyListener {
    private final GUI userInterface;
    private final Lexer lexer;

    public Controller(GUI userInterface, Lexer lexer) {
        this.userInterface = userInterface;
        this.lexer = lexer;
        setListeners();
    }

    private void setListeners() {
        userInterface.getCodeArea().addKeyListener(this);
        userInterface.getScannerButton().addActionListener(this);
        userInterface.getParserButton().addActionListener(this);
        userInterface.getMenuFileOpen().addActionListener(this);
        userInterface.getMenuFileSave().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == userInterface.getScannerButton()) {
            String input = userInterface.getCodeArea().getText();
            lexer.scan(input);
            ArrayList<String> strings = lexer.getStrings();
            ArrayList<Token> tokens = lexer.getTokens();
            Boolean[] reservedWords = lexer.checkReservedWords(tokens);
            userInterface.showTokens(strings, tokens, reservedWords);
            userInterface.setParserButtonState(true);
        }

        if (e.getSource() == userInterface.getParserButton()) {
            Parser parser = new Parser(lexer.getTokens());
            userInterface.showParserResult(parser.parse());
        }

        if (e.getSource() == userInterface.getMenuFileOpen()) {
            int option = userInterface.getFileChooser().showOpenDialog(userInterface);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = userInterface.getFileChooser().getSelectedFile();
                try {
                    Scanner sc = new Scanner(file);
                    StringBuilder stringBuilder = new StringBuilder();
                    while (sc.hasNextLine()) {
                        stringBuilder.append(sc.nextLine()).append("\n");
                    }
                    userInterface.getCodeArea().setText(stringBuilder.toString());
                    userInterface.setParserButtonState(false);
                } catch (FileNotFoundException ex) {
                    userInterface.showWarning("The file was not found");
                }
            }
        }

        if (e.getSource() == userInterface.getMenuFileSave()) {
            String code = userInterface.getCodeArea().getText();
            userInterface.getFileChooser().setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            userInterface.getFileChooser().showSaveDialog(userInterface);
            File file = userInterface.getFileChooser().getSelectedFile();
            if (file == null) {
                userInterface.showWarning("The file was not saved");
                return;
            }
            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write(code);
                fileWriter.close();
            } catch (IOException ex) {
                userInterface.showWarning("The file was not saved");
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        userInterface.setParserButtonState(false);
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
