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
        userInterface.getTokensArea().addKeyListener(this);
        userInterface.getScannerButton().addActionListener(this);
        userInterface.getMenuFileOpen().addActionListener(this);
        userInterface.getMenuFileSave().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == userInterface.getScannerButton()) {
            String input = userInterface.getCodeArea().getText();
            /*
            Scan 1
            String[] inputArray = input.split("\\s+");
            Token[] tokens = lexer.scan(inputArray);
            Boolean[] reservedWords = lexer.checkReservedWords(tokens);
            userInterface.showTokens(inputArray, tokens, reservedWords);
             */

            // Scan 2
            Pair<ArrayList<String>, ArrayList<Token>> lexerResult = lexer.scan2(input);
            Boolean[] reservedWords = lexer.checkReservedWords2(lexerResult.getSecond());
            userInterface.showTokens2(lexerResult.getFirst(), lexerResult.getSecond(), reservedWords);
        }

        if (e.getSource() == userInterface.getMenuFileOpen()) {
            int option = userInterface.getFileChooser().showOpenDialog(userInterface);
            if(option == JFileChooser.APPROVE_OPTION){
                File file = userInterface.getFileChooser().getSelectedFile();
                try {
                    Scanner sc = new Scanner(file);
                    StringBuilder stringBuilder = new StringBuilder();
                    while (sc.hasNextLine()) {
                        stringBuilder.append(sc.nextLine()).append("\n");
                    }
                    userInterface.getCodeArea().setText(stringBuilder.toString());
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

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
