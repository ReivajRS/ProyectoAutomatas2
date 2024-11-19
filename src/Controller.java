import CodeGenerators.IntermediateCodeGenerator;
import CodeGenerators.ObjectCodeGenerator;
import LexicalAnalysis.Lexer;
import SemanticAnalysis.SemanticAnalyzer;
import Utilities.Token;
import SyntacticAnalysis.Parser;

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
    private final Parser parser;
    private final SemanticAnalyzer semanticAnalyzer;
    private final IntermediateCodeGenerator intermediateCodeGenerator;
    private final ObjectCodeGenerator objectCodeGenerator;

    public Controller(GUI userInterface, Lexer lexer, Parser parser, SemanticAnalyzer semanticAnalyzer, IntermediateCodeGenerator intermediateCodeGenerator, ObjectCodeGenerator objectCodeGenerator) {
        this.userInterface = userInterface;
        this.lexer = lexer;
        this.parser = parser;
        this.semanticAnalyzer = semanticAnalyzer;
        this.intermediateCodeGenerator = intermediateCodeGenerator;
        this.objectCodeGenerator = objectCodeGenerator;
        setListeners();
    }

    private void setListeners() {
        userInterface.getCodeArea().addKeyListener(this);
        userInterface.getScannerButton().addActionListener(this);
        userInterface.getParserButton().addActionListener(this);
        userInterface.getSemanticButton().addActionListener(this);
        userInterface.getIntermediateButton().addActionListener(this);
        userInterface.getObjectButton().addActionListener(this);
        userInterface.getMenuFileOpen().addActionListener(this);
        userInterface.getMenuFileSave().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == userInterface.getScannerButton()) {
            String input = userInterface.getCodeArea().getText();
            boolean scanResult = lexer.scan(input);
            ArrayList<String> strings = lexer.getStrings();
            ArrayList<Token> tokens = lexer.getTokens();
            Boolean[] reservedWords = lexer.checkReservedWords(tokens);
            userInterface.showTokens(strings, tokens, reservedWords);
            userInterface.setParserButtonState(scanResult);
            userInterface.clearParserResult();
            userInterface.clearSemanticResult();
            userInterface.clearIntermediateCode();
            userInterface.clearObjectCode();
        }

        if (e.getSource() == userInterface.getParserButton()) {
            parser.initialize(lexer.getTokens(), lexer.getStrings());
            boolean parserResult = parser.parse();
            userInterface.showParserResult(parserResult);
            userInterface.setSemanticButtonState(parserResult);
            userInterface.clearSemanticResult();
            userInterface.clearIntermediateCode();
            userInterface.clearObjectCode();
        }

        if (e.getSource() == userInterface.getSemanticButton()) {
            semanticAnalyzer.initialize(parser.getSyntaxTree());
            boolean semanticResult = semanticAnalyzer.analyze();
            userInterface.showSemanticResult(semanticResult);
            userInterface.setIntermediateButtonState(semanticResult);
            userInterface.clearIntermediateCode();
            userInterface.clearObjectCode();
        }

        if (e.getSource() == userInterface.getIntermediateButton()) {
            intermediateCodeGenerator.initialize(parser.getSyntaxTree(), semanticAnalyzer.getSymbolDataMap());
            String intermediateCode = intermediateCodeGenerator.getIntermediateCode();
            userInterface.showIntermediateCode(intermediateCode);
            userInterface.setObjectButtonState(true);
            userInterface.clearObjectCode();
        }

        if (e.getSource() == userInterface.getObjectButton()) {
            objectCodeGenerator.initialize(parser.getSyntaxTree(), semanticAnalyzer.getSymbolDataMap());
            String objectCode = objectCodeGenerator.getObjectCode();
            userInterface.showObjectCode(objectCode);
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
                    userInterface.clearTokens();
                    userInterface.clearParserResult();
                    userInterface.clearSemanticResult();
                    userInterface.clearIntermediateCode();
                    userInterface.clearObjectCode();
                    userInterface.setParserButtonState(false);
                    userInterface.setSemanticButtonState(false);
                    userInterface.setIntermediateButtonState(false);
                    userInterface.setObjectButtonState(false);
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
        userInterface.setSemanticButtonState(false);
        userInterface.setIntermediateButtonState(false);
        userInterface.setObjectButtonState(false);
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
