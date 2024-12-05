import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class MiniCompiler extends JFrame {
    private JTextArea codeTextArea;
    private JTextArea resultTextArea;
    private JButton openFileButton, lexicalAnalysisButton, syntaxAnalysisButton, semanticAnalysisButton, clearButton;
    private File currentFile;

    public MiniCompiler() {
        // Set up the frame
        setTitle("Mini Compiler");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Code Text Area
        codeTextArea = new JTextArea();
        JScrollPane codeScrollPane = new JScrollPane(codeTextArea);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder("Code Text Area"));

        // Result Text Area
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false); // Make the result area read-only
        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Result Text Area"));

        // Split Pane for Result and Code Text Areas (Vertical Split)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultScrollPane, codeScrollPane);
        splitPane.setDividerLocation(0.5); // Adjust the initial divider position
        splitPane.setResizeWeight(0.5); // Allocate 30% height to Result Text Area initially

        // Panel for buttons with spacing
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 10, 10, 10); // Add spacing around each button
        gbc.fill = GridBagConstraints.BOTH; // Buttons fill both width and height
        gbc.weightx = 1.0; // Expand horizontally
        gbc.weighty = 1.0; // Expand vertically

        openFileButton = new JButton("Open File");
        lexicalAnalysisButton = new JButton("Lexical Analysis");
        syntaxAnalysisButton = new JButton("Syntax Analysis");
        semanticAnalysisButton = new JButton("Semantic Analysis");
        clearButton = new JButton("Clear");

        buttonPanel.add(openFileButton, gbc);
        buttonPanel.add(lexicalAnalysisButton, gbc);
        buttonPanel.add(syntaxAnalysisButton, gbc);
        buttonPanel.add(semanticAnalysisButton, gbc);
        buttonPanel.add(clearButton, gbc);

        // Disable buttons initially
        lexicalAnalysisButton.setEnabled(false);
        syntaxAnalysisButton.setEnabled(false);
        semanticAnalysisButton.setEnabled(false);
        clearButton.setEnabled(false);

        // Add components to frame
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.WEST);

        // Button actions
        openFileButton.addActionListener(e -> openFile());
        lexicalAnalysisButton.addActionListener(e -> performLexicalAnalysis());
        syntaxAnalysisButton.addActionListener(e -> performSyntaxAnalysis());
        semanticAnalysisButton.addActionListener(e -> performSemanticAnalysis());
        clearButton.addActionListener(e -> clearFields());
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                codeTextArea.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    codeTextArea.append(line + "\n");
                }
                resultTextArea.setText("File opened successfully.\n");
                lexicalAnalysisButton.setEnabled(true);
                clearButton.setEnabled(true);
            } catch (IOException ex) {
                resultTextArea.setText("Error reading file: " + ex.getMessage());
            }
        }
    }

    private void performLexicalAnalysis() {
        String code = codeTextArea.getText().trim();
        if (code.isEmpty()) {
            resultTextArea.setText("No code to analyze.");
            return;
        }
    
        resultTextArea.setText("");
    
        // Updated regex: Make all components optional while preserving order
        String regex = "^\\s*" +
               "(\\b(?:byte|short|int|long|float|double|boolean|char|String)\\b)?\\s*" + // Optional data type
               "([a-zA-Z_][a-zA-Z0-9_]*)?\\s*" +                                       // Optional identifier
               "(=)?\\s*" +                                                             // Optional assignment operator
               "((?:\"[^\"]*\"|'[^']*'|[^;\\s]+))?\\s*" +                               // Optional value (handles strings, chars, numbers, etc.)
               "(;)?\\s*$";                                                             // Optional delimiter

        String[] lines = code.split("\\n"); // Analyze code line-by-line
        List<Token> tokens = new ArrayList<>();
        boolean error = false;

        for (String line : lines) {
            line = line.trim(); // Remove leading/trailing whitespace
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);
    
            if (matcher.matches()) {
                // Add tokens only if they are present in the line
                if (matcher.group(1) != null) {
                    tokens.add(new Token("DATA_TYPE", matcher.group(1)));
                    resultTextArea.append("DATA_TYPE: " + matcher.group(1) + "\n");
                }
                if (matcher.group(2) != null) {
                    tokens.add(new Token("IDENTIFIER", matcher.group(2)));
                    resultTextArea.append("IDENTIFIER: " + matcher.group(2) + "\n");
                }
                if (matcher.group(3) != null) {
                    tokens.add(new Token("ASSIGN_OPERATOR", matcher.group(3)));
                    resultTextArea.append("ASSIGN_OPERATOR: " + matcher.group(3) + "\n");
                }
                if (matcher.group(4) != null && !matcher.group(4).isEmpty()) {
                    tokens.add(new Token("VALUE", matcher.group(4)));
                    resultTextArea.append("VALUE: " + matcher.group(4) + "\n");
                }
                if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
                    tokens.add(new Token("DELIMITER", matcher.group(5)));
                    resultTextArea.append("DELIMITER: " + matcher.group(5) + "\n");
                }

                resultTextArea.append("\n");
                
            } else {
                tokens.add(new Token("UNKNOWN", line)); // For unrecognized lines
    
                // Add the unrecognized line and stop processing
                resultTextArea.append("Analyzing line: " + line + "\n");
                resultTextArea.append("UNKNOWN: " + line + "\n\n");
                error = true;
                
            }
        }

        if(error) {
            resultTextArea.append("Error: Unknown token(s) detected." + "\nLexical Analysis Failed.");
            lexicalAnalysisButton.setEnabled(false); // Reset button states
            syntaxAnalysisButton.setEnabled(false);
        } else {
            resultTextArea.append("Lexical Analysis completed.");
            lexicalAnalysisButton.setEnabled(false);
            syntaxAnalysisButton.setEnabled(true);
        }
    
    }
    
    private void performSyntaxAnalysis() {
        String code = codeTextArea.getText();
        if (code.isEmpty()) {
            resultTextArea.setText("No code to analyze.");
            return;
        }
        resultTextArea.setText(""); // Clear previous results

        String[] lines = code.split("\\n"); // Analyze code line-by-line

        boolean error = false;
        for (String line : lines) {
            try {
                line = line.trim();

                // Perform the syntax analysis
                String analysisResult = generateAnalysisResult(line);
        
                // Display the result in the resultTextArea
                resultTextArea.append(analysisResult);
                
            } catch (Exception e) {
                // If there's an error (e.g., invalid syntax), display the error message
                resultTextArea.append("Error in the line: " + line + "\n\n");
                error = true;
            }
        }
        
        if(error) {
            resultTextArea.append("Syntax error(s) detected." + "\nSyntax Analysis Failed.");
            semanticAnalysisButton.setEnabled(false);
        } else {
            // Indicate successful syntax analysis
            resultTextArea.append("Syntax Analysis completed.");
            semanticAnalysisButton.setEnabled(true);
        }

         syntaxAnalysisButton.setEnabled(false);
    }
    
    // Method for syntax analysis
    private String generateAnalysisResult(String input) throws Exception {
        // Trim any leading/trailing whitespace from input
        input = input.trim();
    
        // Updated regex to correctly capture all parts
        String regex = "^(\\b(?:byte|short|int|long|float|double|boolean|char|String)\\b)\\s+" +
                       "([a-zA-Z_][a-zA-Z0-9_]*)\\s*" +
                       "=\\s*" +
                       "([^;]+)\\s*" +
                       "(;)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        result.append("Analyzing line: ").append(input).append("\n");

        if (matcher.matches()) {
            String dataType = matcher.group(1); // Group 1: Data type
            String id = matcher.group(2); // Group 2: Identifier
            String assignOperator = "="; // "=" as literal
            String value = matcher.group(3); // Group 3: Value
            String delimiter = matcher.group(4); // Group 4: Semicolon delimiter
    
            // Add the result with extra spacing for readability
            
            result.append("Data Type: ").append(dataType).append("\n");
            result.append("Identifier: ").append(id).append("\n");
            result.append("Assign Operator: ").append(assignOperator).append("\n");
            result.append("Value: ").append(value).append("\n");
            result.append("Delimiter: ").append(delimiter).append("\n");
            result.append("Line Syntax Analyzed.\n").append("\n");
            return result.toString();
        } else {
            // If input does not match, throw an error
            throw new Exception("Syntax error in the source code.");  
        }
    }
    

    private void performSemanticAnalysis() {
        String code = codeTextArea.getText();
        
        if (code.isEmpty()) {
            resultTextArea.setText("No code to analyze.");
            return;
        }
    
        resultTextArea.setText("");
    
        try {
            // Process the code line by line or as a whole (based on your requirements)
            String[] lines = code.split("\n");
            for (String line : lines) {
                try {
                    // Perform semantic analysis for each line of code
                    String result = semanticAnalyzer(line);
                    resultTextArea.append(result + "\n");  // Display successful semantic analysis result
                } catch (Exception e) {
                    resultTextArea.append("Error: " + e.getMessage() + "\n");  // Display error messages
                }
            }
        } catch (Exception e) {
            resultTextArea.append("Error during semantic analysis: " + e.getMessage() + "\n");
        }
    
        // Disable the semantic analysis button after analysis
        semanticAnalysisButton.setEnabled(false);
    }

    private String semanticAnalyzer(String input) throws Exception{
        Pattern pattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(\".*\"|'.*'|\\S+);");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String variableName = matcher.group(1); // Variable name
            String value = matcher.group(2); // Value assigned to the variable
            
            // Determine the data type of the variable
            String dataType = input.split("\\s+")[0]; // First word is the type (e.g., int, float)

            // Type checking
            return performTypeCheck(variableName, dataType, value);
        } else {
            throw new Exception("Invalid code format: Expected 'dataType variableName = value;'");
        }
    }

    private String performTypeCheck(String variableName, String dataType, String value) throws Exception {
        switch (dataType) {
            case "byte":
                if (!value.matches("-?\\d{1,3}")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid byte.");
                }
                break;
            case "short":
                if (!value.matches("-?\\d{1,5}")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid short.");
                }
                break;
            case "int":
                if (!value.matches("-?\\d+")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid int.");
                }
                break;
            case "long":
                if (!value.matches("-?\\d+")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid long.");
                }
                break;
            case "float":
                if (!value.matches("-?\\d+\\.\\d+")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid float.");
                }
                break;
            case "double":
                if (!value.matches("-?\\d+\\.\\d+")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid double.");
                }
                break;
            case "boolean":
                if (!value.equals("true") && !value.equals("false")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid boolean.");
                }
                break;
            case "char":
                if (!value.matches("'.'")) {
                    throw new Exception("Type mismatch: '" + value + "' is not a valid char.");
                }
                break;
            case "String":
                if (!value.matches("\"[^\"]*\"")) { // Ensure the value is enclosed in double quotes
                    throw new Exception("Type mismatch: '" + value + "' is not a valid String. Strings must be enclosed in double quotes.");
                }
                break;
            default:
                throw new Exception("Unknown data type: " + dataType);
        }
        return "Semantic analysis passed for variable '" + variableName + "' with value '" + value + "' and type '" + dataType + "'.";
    }
    

    private void clearFields() {
        codeTextArea.setText("");
        resultTextArea.setText("");
        lexicalAnalysisButton.setEnabled(false);
        syntaxAnalysisButton.setEnabled(false);
        semanticAnalysisButton.setEnabled(false);
        clearButton.setEnabled(false);
        currentFile = null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MiniCompiler compiler = new MiniCompiler();
            compiler.setVisible(true);
        });
    }
}
