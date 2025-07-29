package Gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.border.MatteBorder;

/**
 * Editor con resaltado de sintaxis y numeracion de lineas para el lenguaje de conjuntos
 * 
 * @author danie
 */
public class SyntaxHighlightedEditor extends JPanel {
    
    // Componentes del editor
    private JTextPane textPane;
    private LineNumberPanel lineNumberPanel;
    
    // Colores para tema oscuro
    private static final Color KEYWORD_COLOR = new Color(86, 156, 214);     
    private static final Color COMMENT_COLOR = new Color(106, 153, 85);      
    private static final Color SYMBOL_COLOR = new Color(255, 123, 114);     
    private static final Color ELEMENT_COLOR = new Color(255, 205, 84);      
    private static final Color PUNCTUATION_COLOR = new Color(212, 212, 212); 
    private static final Color BACKGROUND_COLOR = new Color(51, 51, 51);     
    private static final Color TEXT_COLOR = new Color(240, 240, 240);        
    private static final Color VARIABLE_COLOR = new Color(155, 220, 254);    
    private static final Color LINE_NUMBER_COLOR = new Color(130, 130, 130); 
    private static final Color LINE_NUMBER_BG = new Color(45, 45, 45);     
    
    // Palabras clave del lenguaje
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "CONJ", "OPERA", "EVALUAR"
    ));
    
    // Signos de puntuacion
    private static final Set<String> PUNCTUATION = new HashSet<>(Arrays.asList(
        ":", ";", ",", "(", ")", "{", "}", "[", "]"
    ));
    
    // Variables para el estado del editor
    private StyledDocument document;
    private boolean isUpdating = false;
    private boolean showingPlaceholder = false;
    private javax.swing.Timer highlightTimer;
    
    public SyntaxHighlightedEditor() {
        setDoubleBuffered(true);  
        initializeEditor();
        setupSyntaxHighlighting();
        setupAutoClosing();
        setText("");
    }
    
    /**
     * Inicializa el editor con configuraciones basicas
     */
    private void initializeEditor() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setOpaque(true);
        
        // Crear el JTextPane principal
        textPane = new JTextPane();
        document = textPane.getStyledDocument();
        
        // Configurar fuente
        if (isFontAvailable("JetBrains Mono")) {
            textPane.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        } else if (isFontAvailable("Fira Code")) {
            textPane.setFont(new Font("Fira Code", Font.PLAIN, 14));
        } else if (isFontAvailable("Consolas")) {
            textPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        } else {
            textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        }
        
        textPane.setBackground(BACKGROUND_COLOR);
        textPane.setForeground(TEXT_COLOR);
        textPane.setCaretColor(TEXT_COLOR);
        textPane.setSelectionColor(new Color(75, 110, 175));
        textPane.setSelectedTextColor(Color.WHITE);
        textPane.setMargin(new Insets(10, 15, 10, 10));

        textPane.setEditable(true);
        textPane.setFocusable(true);
        textPane.setOpaque(true);
        
        // Crear panel de numeracion de lineas
        lineNumberPanel = new LineNumberPanel();
        
        // AGREGAR DIRECTAMENTE SIN SCROLLPANE PROPIO
        add(lineNumberPanel, BorderLayout.WEST);
        add(textPane, BorderLayout.CENTER);
        
        // Configurar timer para resaltado de linea actual
        highlightTimer = new javax.swing.Timer(100, e -> highlightCurrentLine());
        highlightTimer.setRepeats(false);
        
        // Listener para resaltar linea actual y actualizar numeros
        textPane.addCaretListener(e -> {
            if (!showingPlaceholder) {
                highlightTimer.restart();
                lineNumberPanel.repaint();
            }
        });
        
        // Listener para actualizar numeros cuando cambie el tamaño
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                lineNumberPanel.repaint();
            }
        });
    }
    
    /**
     * Verifica si una fuente esta disponible en el sistema
     */
    private boolean isFontAvailable(String fontName) {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String font : availableFonts) {
            if (font.equals(fontName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Panel personalizado para mostrar numeros de linea 
     */
    private class LineNumberPanel extends JPanel implements Scrollable {
        private final int MARGIN = 8;
        private final int LINE_HEIGHT_OFFSET = 3; // Separacion hacia abajo
        
        public LineNumberPanel() {
            setBackground(LINE_NUMBER_BG);
            setForeground(LINE_NUMBER_COLOR);
            setFont(new Font("Consolas", Font.PLAIN, 11));
            setBorder(new MatteBorder(0, 0, 0, 1, new Color(70, 70, 70)));
            setPreferredSize(new Dimension(50, 0)); 
            setOpaque(true);
        }
        
        @Override
        public Dimension getPreferredSize() {
            // Calcular altura basada en el numero real de lineas
            int totalLines = getLineCount();
            FontMetrics fm = getFontMetrics(getFont());
            int lineHeight = fm.getHeight() + LINE_HEIGHT_OFFSET;
            int height = Math.max(totalLines * lineHeight + 20, getParent() != null ? getParent().getHeight() : 500);
            return new Dimension(50, height);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                               RenderingHints.VALUE_RENDER_QUALITY);
            
            try {
                paintLineNumbers(g2d);
            } catch (Exception e) {
                paintFallbackNumbers(g2d);
            }
            
            g2d.dispose();
        }
        
        /**
         * Pinta los numeros de linea principal
         */
        private void paintLineNumbers(Graphics2D g2d) {
            Font editorFont = textPane.getFont();
            g2d.setFont(new Font(editorFont.getName(), Font.PLAIN, 11));
            FontMetrics fm = g2d.getFontMetrics();
            
            Element root = document.getDefaultRootElement();
            int totalLines = root.getElementCount();
            int currentLine = getCurrentLine();
            
            // Obtener el área visible si hay un scroll padre
            Rectangle visibleRect = getVisibleRect();
            if (visibleRect.height == 0) {
                visibleRect = new Rectangle(0, 0, getWidth(), getHeight());
            }
            
            for (int line = 0; line < totalLines; line++) {
                try {
                    Element lineElement = root.getElement(line);
                    int startOffset = lineElement.getStartOffset();
                    
                    // Intentar usar modelToView2D para posicion exacta
                    Rectangle2D rect = textPane.modelToView2D(startOffset);
                    int y;
                    
                    if (rect != null) {
                        y = (int)(rect.getY() + fm.getAscent() + LINE_HEIGHT_OFFSET);
                    } else {
                        // calcular posicion estimada
                        int lineHeight = fm.getHeight() + LINE_HEIGHT_OFFSET;
                        y = (line * lineHeight) + fm.getAscent() + LINE_HEIGHT_OFFSET + 10;
                    }
                    
                    // Solo dibujar si esta en el área visible
                    if (y >= visibleRect.y - fm.getHeight() && y <= visibleRect.y + visibleRect.height + fm.getHeight()) {
                        // Resaltar linea actual
                        if (line == currentLine - 1) {
                            g2d.setColor(new Color(80, 80, 80, 120));
                            g2d.fillRect(0, y - fm.getAscent() - LINE_HEIGHT_OFFSET, getWidth(), fm.getHeight() + LINE_HEIGHT_OFFSET);
                            g2d.setColor(new Color(220, 220, 220));
                        } else {
                            g2d.setColor(LINE_NUMBER_COLOR);
                        }
                        
                        String lineNumber = String.valueOf(line + 1);
                        int x = getWidth() - fm.stringWidth(lineNumber) - MARGIN;
                        g2d.drawString(lineNumber, x, y);
                    }
                    
                } catch (BadLocationException e) {
                    // Usar Metodo de fallback para esta linea especifica
                    paintFallbackLine(g2d, fm, line, currentLine);
                }
            }
        }
        
        /**
         * Pinta una linea especifica como fallback
         */
        private void paintFallbackLine(Graphics2D g2d, FontMetrics fm, int line, int currentLine) {
            int lineHeight = fm.getHeight() + LINE_HEIGHT_OFFSET;
            int y = (line * lineHeight) + fm.getAscent() + LINE_HEIGHT_OFFSET + 10;
            
            Rectangle visibleRect = getVisibleRect();
            if (y >= visibleRect.y - fm.getHeight() && y <= visibleRect.y + visibleRect.height + fm.getHeight()) {
                if (line == currentLine - 1) {
                    g2d.setColor(new Color(80, 80, 80, 120));
                    g2d.fillRect(0, y - fm.getAscent() - LINE_HEIGHT_OFFSET, getWidth(), fm.getHeight() + LINE_HEIGHT_OFFSET);
                    g2d.setColor(new Color(220, 220, 220));
                } else {
                    g2d.setColor(LINE_NUMBER_COLOR);
                }
                
                String lineNumber = String.valueOf(line + 1);
                int x = getWidth() - fm.stringWidth(lineNumber) - MARGIN;
                g2d.drawString(lineNumber, x, y);
            }
        }
        
        /**
         * Fallback completo para numeros de linea
         */
        private void paintFallbackNumbers(Graphics2D g2d) {
            g2d.setColor(LINE_NUMBER_COLOR);
            g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
            FontMetrics fm = g2d.getFontMetrics();
            
            int totalLines = getLineCount();
            int lineHeight = fm.getHeight() + LINE_HEIGHT_OFFSET;
            int currentLine = getCurrentLine();
            
            Rectangle visibleRect = getVisibleRect();
            int startLine = Math.max(1, (visibleRect.y / lineHeight));
            int endLine = Math.min(totalLines, ((visibleRect.y + visibleRect.height) / lineHeight) + 2);
            
            for (int i = startLine; i <= endLine; i++) {
                int y = (i * lineHeight) + LINE_HEIGHT_OFFSET;
                
                if (i == currentLine) {
                    g2d.setColor(new Color(80, 80, 80, 120));
                    g2d.fillRect(0, y - fm.getAscent() - LINE_HEIGHT_OFFSET, getWidth(), fm.getHeight() + LINE_HEIGHT_OFFSET);
                    g2d.setColor(new Color(220, 220, 220));
                } else {
                    g2d.setColor(LINE_NUMBER_COLOR);
                }
                
                String lineNumber = String.valueOf(i);
                int x = getWidth() - fm.stringWidth(lineNumber) - MARGIN;
                g2d.drawString(lineNumber, x, y);
            }
        }
        
        private int getCurrentLine() {
            try {
                int caretPos = textPane.getCaretPosition();
                Element root = document.getDefaultRootElement();
                return root.getElementIndex(caretPos) + 1;
            } catch (Exception e) {
                return 1;
            }
        }
        
        // Implementación de Scrollable para sincronización con scroll externo
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            FontMetrics fm = getFontMetrics(getFont());
            return fm.getHeight() + LINE_HEIGHT_OFFSET;
        }
        
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height;
        }
        
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
    
    /**
     * Configura el resaltado de sintaxis
     */
    private void setupSyntaxHighlighting() {
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isUpdating && !showingPlaceholder) {
                    SwingUtilities.invokeLater(() -> {
                        if (!showingPlaceholder) {
                            highlightSyntax();
                            updateLineNumbers();
                        }
                    });
                }
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isUpdating) {
                    SwingUtilities.invokeLater(() -> {
                        if (!showingPlaceholder) {
                            highlightSyntax();
                            updateLineNumbers();
                        }
                        try {
                            String currentText = textPane.getText().trim();
                            if (currentText.isEmpty() && !showingPlaceholder) {
                                setText("");
                            }
                        } catch (Exception ex) {
                            // Ignorar errores
                        }
                    });
                }
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        
        // Listener para teclas
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (showingPlaceholder && !Character.isISOControl(e.getKeyChar())) {
                    hidePlaceholder();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (showingPlaceholder) {
                    int keyCode = e.getKeyCode();
                    if (keyCode == KeyEvent.VK_BACK_SPACE || 
                        keyCode == KeyEvent.VK_DELETE || 
                        keyCode == KeyEvent.VK_ENTER ||
                        keyCode == KeyEvent.VK_SPACE ||
                        (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) ||
                        (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9)) {
                        hidePlaceholder();
                    }
                }
            }
        });
    }
    
    /**
     * Actualiza los numeros de linea
     */
    private void updateLineNumbers() {
        SwingUtilities.invokeLater(() -> {
            lineNumberPanel.revalidate();
            lineNumberPanel.repaint();
        });
    }
    
    /**
     * Configura el auto-cierre de caracteres
     */
    private void setupAutoClosing() {
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char typedChar = e.getKeyChar();
                String closingChar = getClosingChar(typedChar);
                
                if (closingChar != null) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            int caretPos = textPane.getCaretPosition();
                            document.insertString(caretPos, closingChar, null);
                            textPane.setCaretPosition(caretPos);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    });
                }
                
                // Auto-completar comentarios multilinea
                if (typedChar == '<') {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            int caretPos = textPane.getCaretPosition();
                            String text = textPane.getText();
                            
                            if (caretPos >= 1 && text.length() >= caretPos) {
                                if (caretPos < text.length() && text.charAt(caretPos) == '!') {
                                    String completion = "!>";
                                    document.insertString(caretPos + 1, completion, null);
                                    textPane.setCaretPosition(caretPos + 1);
                                }
                            }
                        } catch (Exception ex) {
                        }
                    });
                }
            }
        });
    }
     
    /**
     * Obtiene el caracter de cierre correspondiente
     */
    private String getClosingChar(char openChar) {
        switch (openChar) {
            case '{': return "}";
            case '[': return "]";
            case '(': return ")";
            case '"': return "\"";
            case '\'': return "'";
            default: return null;
        }
    }
    
    /**
     * Aplica el resaltado de sintaxis al texto
     */
    private void highlightSyntax() {
        if (showingPlaceholder || isUpdating) return;
        
        isUpdating = true;
        
        try {
            String text = textPane.getText();
            
            // Limpiar estilos existentes
            Style defaultStyle = document.addStyle("default", null);
            StyleConstants.setForeground(defaultStyle, TEXT_COLOR);
            StyleConstants.setBackground(defaultStyle, BACKGROUND_COLOR);
            document.setCharacterAttributes(0, document.getLength(), defaultStyle, true);
            
            // Aplicar resaltados en orden específico
            highlightPunctuation(text);
            highlightElements(text);
            highlightSymbols(text);
            highlightKeywords(text);
            highlightComments(text);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
    }
    
    /**
     * Resalta palabras clave
     */
    private void highlightKeywords(String text) {
        Style keywordStyle = document.addStyle("keyword", null);
        StyleConstants.setForeground(keywordStyle, KEYWORD_COLOR);
        StyleConstants.setBold(keywordStyle, true);
        
        for (String keyword : KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                if (!isPositionInComment(matcher.start(), text)) {
                    document.setCharacterAttributes(matcher.start(), 
                        matcher.end() - matcher.start(), keywordStyle, false);
                }
            }
        }
    }
    
    /**
     * Resalta comentarios
     */
    private void highlightComments(String text) {
        Style commentStyle = document.addStyle("comment", null);
        StyleConstants.setForeground(commentStyle, COMMENT_COLOR);
        StyleConstants.setItalic(commentStyle, true);
        
        // Comentarios de linea (#)
        Pattern lineCommentPattern = Pattern.compile("#.*$", Pattern.MULTILINE);
        Matcher lineCommentMatcher = lineCommentPattern.matcher(text);
        while (lineCommentMatcher.find()) {
            document.setCharacterAttributes(lineCommentMatcher.start(), 
                lineCommentMatcher.end() - lineCommentMatcher.start(), commentStyle, true);
        }
        
        // Comentarios multilinea (<!...!>)
        Pattern blockCommentPattern = Pattern.compile("<!.*?!>", Pattern.DOTALL);
        Matcher blockCommentMatcher = blockCommentPattern.matcher(text);
        while (blockCommentMatcher.find()) {
            document.setCharacterAttributes(blockCommentMatcher.start(), 
                blockCommentMatcher.end() - blockCommentMatcher.start(), commentStyle, true);
        }
    }
    
    /**
     * Resalta simbolos especiales
     */
    private void highlightSymbols(String text) {
        Style symbolStyle = document.addStyle("symbol", null);
        StyleConstants.setForeground(symbolStyle, SYMBOL_COLOR);
        StyleConstants.setBold(symbolStyle, true);
        
        // Operadores en orden de longitud 
        String[] operators = {"^^^^","->", "^^", "~", "U", "&", "^", "-"};
        
        for (String operator : operators) {
            Pattern pattern = Pattern.compile(Pattern.quote(operator));
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                if (!isPositionInComment(matcher.start(), text)) {
                    document.setCharacterAttributes(matcher.start(), 
                        matcher.end() - matcher.start(), symbolStyle, false);
                }
            }
        }
    }
    
    /**
     * Resalta elementos de conjuntos y variables
     */
    private void highlightElements(String text) {
        Style elementStyle = document.addStyle("element", null);
        StyleConstants.setForeground(elementStyle, ELEMENT_COLOR);
        
        Style variableStyle = document.addStyle("variable", null);
        StyleConstants.setForeground(variableStyle, VARIABLE_COLOR);
        
        // Elementos dentro de llaves
        Pattern elementPattern = Pattern.compile("\\{[^}]*\\}");
        Matcher elementMatcher = elementPattern.matcher(text);
        while (elementMatcher.find()) {
            if (!isPositionInComment(elementMatcher.start(), text)) {
                document.setCharacterAttributes(elementMatcher.start(), 
                    elementMatcher.end() - elementMatcher.start(), elementStyle, false);
            }
        }
        
        // Variables despues de CONJ : y OPERA :
        Pattern namePattern = Pattern.compile("(CONJ|OPERA)\\s*:\\s*(\\w+)");
        Matcher nameMatcher = namePattern.matcher(text);
        while (nameMatcher.find()) {
            if (!isPositionInComment(nameMatcher.start(2), text)) {
                document.setCharacterAttributes(nameMatcher.start(2), 
                    nameMatcher.end(2) - nameMatcher.start(2), variableStyle, false);
            }
        }
        
        // Variables despues de ->
        Pattern varPattern = Pattern.compile("->\\s*([a-zA-Z_][a-zA-Z0-9_,\\s~]*)");
        Matcher varMatcher = varPattern.matcher(text);
        while (varMatcher.find()) {
            if (!isPositionInComment(varMatcher.start(1), text)) {
                document.setCharacterAttributes(varMatcher.start(1), 
                    varMatcher.end(1) - varMatcher.start(1), elementStyle, false);
            }
        }
        
        // Referencias de variables
        Pattern refPattern = Pattern.compile("\\b([a-zA-Z]\\w*)(?=\\s*[,;)}])");
        Matcher refMatcher = refPattern.matcher(text);
        while (refMatcher.find()) {
            String match = refMatcher.group(1);
            if (!KEYWORDS.contains(match) && !isPositionInComment(refMatcher.start(), text)) {
                String beforeMatch = text.substring(Math.max(0, refMatcher.start() - 10), refMatcher.start());
                if (!beforeMatch.contains("{") || beforeMatch.lastIndexOf("}") > beforeMatch.lastIndexOf("{")) {
                    document.setCharacterAttributes(refMatcher.start(), 
                        refMatcher.end() - refMatcher.start(), variableStyle, false);
                }
            }
        }
    }
    
    /**
     * Resalta la puntuacion
     */
    private void highlightPunctuation(String text) {
        Style punctuationStyle = document.addStyle("punctuation", null);
        StyleConstants.setForeground(punctuationStyle, PUNCTUATION_COLOR);
        
        for (String punct : PUNCTUATION) {
            Pattern pattern = Pattern.compile(Pattern.quote(punct));
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                if (!isPositionInComment(matcher.start(), text)) {
                    document.setCharacterAttributes(matcher.start(), 
                        matcher.end() - matcher.start(), punctuationStyle, false);
                }
            }
        }
    }
    
    /**
     * Verifica si una posicion esta dentro de un comentario
     */
    private boolean isPositionInComment(int pos, String text) {
        // Verificar comentario de linea
        String beforePos = text.substring(0, pos);
        int lastNewline = beforePos.lastIndexOf('\n');
        String currentLine = text.substring(lastNewline + 1, 
            Math.min(text.indexOf('\n', pos) != -1 ? text.indexOf('\n', pos) : text.length(), text.length()));
        int hashIndex = currentLine.indexOf('#');
        if (hashIndex != -1 && pos >= lastNewline + 1 + hashIndex) {
            return true;
        }
        
        // Verificar comentario multilinea
        int lastOpen = beforePos.lastIndexOf("<!"); 
        int lastClose = beforePos.lastIndexOf("!>");
        return lastOpen > lastClose;
    }
    
    /**
     * Resalta la linea actual
     */
    private void highlightCurrentLine() {
        if (showingPlaceholder) return;
        lineNumberPanel.repaint();
    }
    
    /**
     * Oculta el placeholder
     */
    private void hidePlaceholder() {
        if (showingPlaceholder) {
            isUpdating = true;
            showingPlaceholder = false;
            textPane.setText("");
            textPane.setBackground(BACKGROUND_COLOR);
            isUpdating = false;
            updateLineNumbers();
        }
    }
    
    /**
     * Muestra el placeholder
     */
    private void showPlaceholder() {
        if (!showingPlaceholder) {
            isUpdating = true;
            showingPlaceholder = true;
            
            String placeholderText = """
# EJEMPLO DE Estructura del lenguaje de conjuntos
{
    #Definición de conjuntos
    CONJ : A -> 1,2,3,a,b ;
    CONJ : B -> 0~9;
    CONJ : C -> x,y,z;
    
    #Definición de operaciones
    OPERA : op1 -> ^^ {A};          # Doble Complemento
    OPERA : op2 -> U {A} {B};       # Unión
    
    #Evaluamos
    EVALUAR ( {a, b} , op1 );
    EVALUAR ( {1, 2} , op2 );
    
    # Esto es un comentario de una sola linea
    <!
    Esto es un
    comentario multilinea
    !>
}""";
            
            textPane.setText(placeholderText);
            
            Style placeholderStyle = document.addStyle("placeholder", null);
            StyleConstants.setForeground(placeholderStyle, new Color(180, 180, 160)); 
            StyleConstants.setBackground(placeholderStyle, BACKGROUND_COLOR);
            StyleConstants.setItalic(placeholderStyle, true);
            
            try {
                document.setCharacterAttributes(0, document.getLength(), placeholderStyle, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            isUpdating = false;
            updateLineNumbers();
        }
    }
    
    // Metodos públicos para compatibilidad
    public String getText() {
        if (showingPlaceholder) {
            return "";
        }
        return textPane.getText();
    }
    
    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            if (!showingPlaceholder) {
                showPlaceholder();
            }
        } else {
            if (showingPlaceholder) {
                hidePlaceholder();
            }
            textPane.setText(text);
            if (!showingPlaceholder) {
                highlightSyntax();
            }
            updateLineNumbers();
        }
    }
    
    @Override
    public void setFont(Font font) {
        if (textPane != null) {
            textPane.setFont(font);
            if (lineNumberPanel != null) {
                updateLineNumbers();
            }
        }
    }
    
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (textPane != null) {
            textPane.setBackground(bg);
        }
    }
    
    @Override
    public boolean requestFocusInWindow() {
        return textPane != null ? textPane.requestFocusInWindow() : false;
    }
    
    /**
     * Obtiene el numero total de lineas del documento
     */
    private int getLineCount() {
        try {
            Element root = document.getDefaultRootElement();
            return Math.max(1, root.getElementCount());
        } catch (Exception e) {
            return 1;
        }
    }
    
    /**
     * Obtiene el JTextPane interno para configuraciones avanzadas
     */
    public JTextPane getTextPane() {
        return textPane;
    }
    
    /**
     * Obtiene el panel de numeros de linea para configuraciones avanzadas
     */
    public JPanel getLineNumberPanel() {
        return lineNumberPanel;
    }
    
    /**
     * Retorna las dimensiones preferidas del editor completo
     */
    @Override
    public Dimension getPreferredSize() {
        if (textPane == null) return super.getPreferredSize();
        
        try {
            // Calcular dimensiones basadas en el contenido
            Dimension textSize = textPane.getPreferredSize();
            Dimension lineNumberSize = lineNumberPanel.getPreferredSize();
            
            // Asegurar altura minima
            int height = Math.max(textSize.height, 500);
            int width = textSize.width + lineNumberSize.width;
            
            return new Dimension(width, height);
        } catch (Exception e) {
            return new Dimension(600, 500);
        }
    }
    
    /**
     * Fuerza actualizacion del layout y numeros de linea
     */
    public void updateLayout() {
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
            updateLineNumbers();
        });
    }
}