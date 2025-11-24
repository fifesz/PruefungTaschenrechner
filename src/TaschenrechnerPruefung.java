import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class TaschenrechnerPruefung extends JFrame {

    private final JTextField display = new JTextField("0");

    private double currentValue = 0.0;
    private String currentOperator = null;
    private boolean enteringNewNumber = true;
    private boolean errorState = false;

    private String lastOperator = null;
    private double lastOperand = 0.0;

    private final DecimalFormat fmt = new DecimalFormat("#.##########");

    public TaschenrechnerPruefung() {
        super("Taschenrechner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        display.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 4, 5, 5));
        add(buttonPanel, BorderLayout.CENTER);

        String[] buttons = {
                "√", "x²", "BIN", "C",
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                ".", "0", "←", "+"
        };

        for (String label : buttons) {
            JButton b = createButton(label);
            buttonPanel.add(b);
        }

        JButton equals = createButton("=");
        equals.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        equals.setPreferredSize(new Dimension(0, 60));
        add(equals, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        b.addActionListener(e -> onButtonPressed(text));
        return b;
    }

    private void onButtonPressed(String label) {

        if (errorState && !label.equals("C")) {
            return;
        }

        if (label.equals("-") && enteringNewNumber && display.getText().equals("0")) {
            display.setText("-");
            enteringNewNumber = false;
            return;
        }

        switch (label) {
            case "0": case "1": case "2": case "3": case "4":
            case "5": case "6": case "7": case "8": case "9":
                appendDigit(label.charAt(0));
                break;

            case ".":
                appendDot();
                break;

            case "+": case "-": case "*": case "/":
                operatorPressed(label);
                break;

            case "=":
                equalsPressed();
                break;

            case "C":
                clearAll();
                break;

            case "←":
                backspace();
                break;

            case "√":
                sqrtPressed();
                break;

            case "x²":
                squarePressed();
                break;

            case "BIN":
                binaryPressed();
                break;

            default:
                break;
        }
    }

    private void appendDigit(char digit) {
        if (enteringNewNumber) {
            display.setText("" + digit);
            enteringNewNumber = false;
        } else {
            String text = display.getText();
            if (text.equals("0")) display.setText("" + digit);
            else display.setText(text + digit);
        }
    }

    private void appendDot() {
        if (enteringNewNumber) {
            display.setText("0.");
            enteringNewNumber = false;
        } else if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }

    private void backspace() {
        if (enteringNewNumber) return;
        String s = display.getText();
        if (s.length() <= 1) {
            display.setText("0");
            enteringNewNumber = true;
        } else {
            display.setText(s.substring(0, s.length() - 1));
        }
    }

    private void clearAll() {
        currentValue = 0.0;
        lastOperator = null;
        lastOperand = 0.0;
        currentOperator = null;
        enteringNewNumber = true;
        errorState = false;
        display.setText("0");
    }

    private void operatorPressed(String op) {
        try {
            double displayed = Double.parseDouble(display.getText());

            if (currentOperator == null) {
                currentValue = displayed;
            } else {
                currentValue = applyOperation(currentOperator, currentValue, displayed);
                display.setText(formatNumber(currentValue));
            }
            currentOperator = op;
            enteringNewNumber = true;

        } catch (Exception ex) {
            setError("Error");
        }
    }

    private void equalsPressed() {
        try {
            double displayed = Double.parseDouble(display.getText());

            if (currentOperator != null) {
                double result = applyOperation(currentOperator, currentValue, displayed);

                lastOperator = currentOperator;
                lastOperand = displayed;

                currentValue = result;
                display.setText(formatNumber(result));

                currentOperator = null;
                enteringNewNumber = true;

            } else if (lastOperator != null) {
                double base = Double.parseDouble(display.getText());
                double result = applyOperation(lastOperator, base, lastOperand);
                currentValue = result;
                display.setText(formatNumber(result));
                enteringNewNumber = true;
            }

        } catch (Exception ex) {
            setError("Error");
        }
    }

    private double applyOperation(String op, double a, double b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0.0) throw new ArithmeticException("Division by zero");
                return a / b;
        }
        throw new ArithmeticException("Unknown operator");
    }


    private void sqrtPressed() {
        try {
            double v = Double.parseDouble(display.getText());
            if (v < 0) {
                setError("Invalid input");
                return;
            }
            double r = Math.sqrt(v);
            display.setText(formatNumber(r));
            enteringNewNumber = true;

        } catch (Exception ex) {
            setError("Error");
        }
    }

    private void squarePressed() {
        try {
            double v = Double.parseDouble(display.getText());
            double r = v * v;
            display.setText(formatNumber(r));
            enteringNewNumber = true;

        } catch (Exception ex) {
            setError("Error");
        }
    }

    private void binaryPressed() {
        try {
            double v = Double.parseDouble(display.getText());

            // Nur integer erlaubt
            if (v != Math.floor(v)) {
                setError("Not integer");
                return;
            }

            long val = (long)v;

            String bin = Long.toBinaryString(Math.abs(val));
            if (val < 0) bin = "-" + bin;

            display.setText(bin);
            enteringNewNumber = true;

        } catch (Exception ex) {
            setError("Error");
        }
    }


    private void setError(String msg) {
        display.setText("Error");
        errorState = true;
        currentOperator = null;
    }

    private String formatNumber(double v) {
        String s = fmt.format(v);
        if (s.equals("-0")) return "0";
        return s;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaschenrechnerPruefung());
    }
}
