public class Main {
    public static void main(String[] args) {
        GUI userInterface = new GUI();
        Lexer lexer = new Lexer();
        Controller controller = new Controller(userInterface, lexer);
    }
}