package Graphics;

import Environment.Environment;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

/**
 * Generador de diagramas de Venn para operaciones de conjuntos.
 * Soporta operaciones de union, interseccion, diferencia y complemento
 * para conjuntos de hasta 4 elementos.
 * 
 * @author danie
 */
public class VennDiagramGenerator {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // CONSTANTES DE CONFIGURACION
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    private static final int DIAGRAM_WIDTH = 800;     
    private static final int DIAGRAM_HEIGHT = 520;    
    private static final int CIRCLE_RADIUS = 120;    
    private static final int MARGIN = 50;            

    private static final Color UNION_COLOR = new Color(100, 181, 246, 140);         
    private static final Color INTERSECTION_COLOR = new Color(129, 199, 132, 140);    
    private static final Color DIFFERENCE_COLOR = new Color(255, 138, 101, 140);      
    private static final Color COMPLEMENT_COLOR = new Color(200, 200, 100, 120);      
    private static final Color CIRCLE_BORDER = new Color(97, 97, 97, 220);        
    private static final Color UNIVERSE_COLOR = new Color(250, 250, 250, 255);       
    
    private static final Color SET_A_BORDER = new Color(33, 150, 243, 255);          
    private static final Color SET_B_BORDER = new Color(76, 175, 80, 255);          
    private static final Color SET_C_BORDER = new Color(255, 193, 7, 255);         
    private static final Color SET_D_BORDER = new Color(255, 87, 34, 255);           
    
    private static final Color TRIPLE_INTERSECTION_COLOR = new Color(156, 39, 176, 140); 
    private static final Color QUADRUPLE_INTERSECTION_COLOR = new Color(233, 30, 99, 140); 
    
    private static final Color TEXT_COLOR = new Color(66, 66, 66, 255);           
    private static final Color ERROR_COLOR = new Color(244, 67, 54, 255);           
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // ATRIBUTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private final Environment environment;
    private final VennDiagramData diagramData;
    
    /**
     * Constructor del generador de diagramas.
     * 
     * @param environment Entorno de ejecucion
     */
    public VennDiagramGenerator(Environment environment) {
        this.environment = environment;
        this.diagramData = new VennDiagramData();
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Genera un diagrama de Venn para una operacion de conjuntos.
     * 
     * @param operationName Nombre de la operacion
     * @param operator Operador de la operacion (U, &, -, ^)
     * @param operands Lista de operandos
     * @param result Resultado de la operacion
     * @return JPanel con el diagrama generado
     */
    public JPanel generateDiagram(String operationName, String operator, List<String> operands, Set<Object> result) {
        diagramData.setOperationName(operationName);
        diagramData.setOperator(operator);
        diagramData.setOperands(operands);
        diagramData.setResult(result);
        
        Map<String, Set<Object>> referencedSets = obtenerConjuntosReferenciados(operands);
        diagramData.setReferencedSets(referencedSets);
        
        VennDiagramPanel panel = new VennDiagramPanel(diagramData);
        
        return panel;
    }
    
    /**
     * Genera un diagrama simple para un conjunto único.
     * 
     * @param setName Nombre del conjunto
     * @param elements Elementos del conjunto
     * @return JPanel con el diagrama generado
     */
    public JPanel generateSimpleDiagram(String setName, Set<Object> elements) {
        Map<String, Set<Object>> singleSet = new HashMap<>();
        singleSet.put(setName, elements);
        
        diagramData.setOperationName(setName);
        diagramData.setOperator("SET");
        diagramData.setOperands(Arrays.asList(setName));
        diagramData.setResult(elements);
        diagramData.setReferencedSets(singleSet);
        
        return new VennDiagramPanel(diagramData);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS DE UTILIDAD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene los conjuntos referenciados en los operandos.
     * 
     * @param operands Lista de operandos
     * @return Mapa con los conjuntos referenciados
     */
    private Map<String, Set<Object>> obtenerConjuntosReferenciados(List<String> operands) {
        Map<String, Set<Object>> referencedSets = new HashMap<>();
        
        for (String operand : operands) {
            if (environment.existSet(operand)) {
                Set<Object> set = environment.getSet(operand);
                if (set != null) {
                    referencedSets.put(operand, set);
                }
            }
        }
        
        return referencedSets;
    }
    
    /**
     * Obtiene el color apropiado para una operacion.
     * 
     * @param operator Operador de la operacion
     * @return Color correspondiente
     */
    public static Color getOperationColor(String operator) {
        switch (operator) {
            case "U": return UNION_COLOR;
            case "&": return INTERSECTION_COLOR;
            case "-": return DIFFERENCE_COLOR;
            case "^": return COMPLEMENT_COLOR;
            default: return UNION_COLOR;
        }
    }
    
    /**
     * Obtiene el nombre descriptivo de una operacion.
     * 
     * @param operator Operador de la operacion
     * @return Nombre descriptivo
     */
    public static String getOperationName(String operator) {
        switch (operator) {
            case "U": return "union";
            case "&": return "interseccion";
            case "-": return "Diferencia";
            case "^": return "Complemento";
            case "SET": return "Conjunto";
            default: return "operacion";
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // GETTERS PARA CONFIGURACION
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    public static int getDiagramWidth() { return DIAGRAM_WIDTH; }
    public static int getDiagramHeight() { return DIAGRAM_HEIGHT; }
    public static int getCircleRadius() { return CIRCLE_RADIUS; }
    public static int getMargin() { return MARGIN; }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS PARA GENERAR IMAGENES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Genera una imagen PNG del diagrama de Venn y la guarda en el directorio especificado
     * @param data Datos del diagrama de Venn
     * @param outputPath Ruta donde guardar la imagen
     * @return true si se guardo exitosamente, false en caso contrario
     */
    public static boolean generateImageFile(VennDiagramData data, String outputPath) {
        return generateImageFile(data, outputPath, "png");
    }
    
    /**
     * Genera una imagen del diagrama de Venn y la guarda en el directorio especificado
     * @param data Datos del diagrama de Venn
     * @param outputPath Ruta donde guardar la imagen
     * @param format Formato de la imagen (png, jpg, jpeg)
     * @return true si se guardo exitosamente, false en caso contrario
     */
    public static boolean generateImageFile(VennDiagramData data, String outputPath, String format) {
        try {
            BufferedImage image = new BufferedImage(DIAGRAM_WIDTH, DIAGRAM_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            
            g2d.setColor(UNIVERSE_COLOR);
            g2d.fillRect(0, 0, DIAGRAM_WIDTH, DIAGRAM_HEIGHT);
            
            renderDiagram(g2d, data);
            
            g2d.dispose();
            
            File outputFile = new File(outputPath);
            
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            String formatToUse = format.toLowerCase();
            boolean success = false;
            
            if (formatToUse.equals("jpg") || formatToUse.equals("jpeg")) {
                BufferedImage jpgImage = new BufferedImage(DIAGRAM_WIDTH, DIAGRAM_HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D jpgG2d = jpgImage.createGraphics();
                jpgG2d.setColor(UNIVERSE_COLOR);
                jpgG2d.fillRect(0, 0, DIAGRAM_WIDTH, DIAGRAM_HEIGHT);
                jpgG2d.drawImage(image, 0, 0, null);
                jpgG2d.dispose();
                
                success = ImageIO.write(jpgImage, "jpg", outputFile);
            } else {
                success = ImageIO.write(image, "png", outputFile);
            }
            
            return success;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Genera una imagen PNG del diagrama de Venn con nombre automático
     * @param data Datos del diagrama de Venn
     * @param outputDir Directorio donde guardar la imagen
     * @return Ruta del archivo generado, null si fallo
     */
    public static String generateImageFileWithAutoName(VennDiagramData data, String outputDir) {
        // Generar nombre automático basado en la operacion y timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        String operation = data.getOperation().toLowerCase().replace(" ", "_");
        String fileName = "venn_" + operation + "_" + timestamp + ".png";
        
        String fullPath = outputDir + File.separator + fileName;
        
        if (generateImageFile(data, fullPath)) {
            return fullPath;
        }
        
        return null;
    }
    
    /**
     * Genera una imagen del diagrama de Venn en memoria (sin guardar archivo)
     * @param data Datos del diagrama de Venn
     * @return BufferedImage con el diagrama renderizado
     */
    public static BufferedImage generateImageInMemory(VennDiagramData data) {
        BufferedImage image = new BufferedImage(DIAGRAM_WIDTH, DIAGRAM_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        g2d.setColor(UNIVERSE_COLOR);
        g2d.fillRect(0, 0, DIAGRAM_WIDTH, DIAGRAM_HEIGHT);
        
        renderDiagram(g2d, data);
        
        g2d.dispose();
        
        return image;
    }
    /**
     * Renderiza el diagrama de Venn directamente en el contexto gráfico
     * @param g2d Contexto gráfico 2D
     * @param data Datos del diagrama de Venn
     */
    private static void renderDiagram(Graphics2D g2d, VennDiagramData data) {
        drawTitle(g2d, data);
        
        if (data.getReferencedSets().isEmpty() || data.getOperands().isEmpty()) {
            drawNoDataMessage(g2d);
            return;
        }
        
        Set<String> uniqueSets = new HashSet<>(data.getReferencedSets().keySet());
        
        if (uniqueSets.size() == 1) {
            renderSingleSetDiagram(g2d, data, uniqueSets.iterator().next());
        } else if (uniqueSets.size() == 2) {
            renderTwoSetDiagram(g2d, data);
        } else if (uniqueSets.size() == 3) {
            renderThreeSetDiagram(g2d, data);
        } else if (uniqueSets.size() == 4) {
            renderFourSetDiagram(g2d, data);
        } else if (uniqueSets.size() >= 5) {
            drawTooManySetMessage(g2d);
        } else {
            drawNoDataMessage(g2d);
        }
        
    }
    
    /**
     * Dibuja el titulo del diagrama
     */
    private static void drawTitle(Graphics2D g2d, VennDiagramData data) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2d.setColor(Color.BLACK);
        
        String title = "DIAGRAMA DE VENN - " + data.getTitle();
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int x = (DIAGRAM_WIDTH - titleWidth) / 2;
        
        g2d.drawString(title, x, 40);
    }
    
    /**
     * Dibuja mensaje cuando no hay datos válidos
     */
    private static void drawNoDataMessage(Graphics2D g2d) {
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(ERROR_COLOR);
        
        String message = "operacion no soportada para diagramas de Venn";
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        int x = (DIAGRAM_WIDTH - messageWidth) / 2;
        int y = DIAGRAM_HEIGHT / 2;
        
        g2d.drawString(message, x, y);
    }
    
    /**
     * Dibuja mensaje cuando hay demasiados conjuntos
     */
    private static void drawTooManySetMessage(Graphics2D g2d) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.setColor(ERROR_COLOR);
        
        String[] messages = {
            "No es posible graficar más de 4 conjuntos",
            "debido a que costaría diferenciar",
            "las áreas sombreadas"
        };
        
        FontMetrics fm = g2d.getFontMetrics();
        int y = DIAGRAM_HEIGHT / 2 - 30;
        
        for (String message : messages) {
            int messageWidth = fm.stringWidth(message);
            int x = (DIAGRAM_WIDTH - messageWidth) / 2;
            g2d.drawString(message, x, y);
            y += fm.getHeight() + 5;
        }
    }
    
    /**
     * Renderiza un diagrama de dos conjuntos usando analisis de regiones
     */
    private static void renderTwoSetDiagram(Graphics2D g2d, VennDiagramData data) {
        List<String> uniqueSetNames = new ArrayList<>(data.getReferencedSets().keySet());
        String setNameA = uniqueSetNames.size() > 0 ? uniqueSetNames.get(0) : "A";
        String setNameB = uniqueSetNames.size() > 1 ? uniqueSetNames.get(1) : "B";
        
        Set<Object> setA = data.getReferencedSets().get(setNameA);
        Set<Object> setB = data.getReferencedSets().get(setNameB);
        Set<Object> operationResult = data.getResult();
        
        int centerY = DIAGRAM_HEIGHT / 2;
        int leftX = DIAGRAM_WIDTH / 2 - 40;
        int rightX = DIAGRAM_WIDTH / 2 + 40;
        
        int universeX = MARGIN;
        int universeY = MARGIN;
        int universeWidth = DIAGRAM_WIDTH - 2 * MARGIN;
        int universeHeight = DIAGRAM_HEIGHT - 2 * MARGIN;
        
        g2d.setColor(UNIVERSE_COLOR);
        g2d.fillRect(universeX, universeY, universeWidth, universeHeight);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(universeX, universeY, universeWidth, universeHeight);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("U", universeX + universeWidth - 25, universeY + 20);
        
        if (setA != null && setB != null && operationResult != null) {
            analyzeAndPaintRegions(g2d, setA, setB, operationResult, leftX, rightX, centerY, universeX, universeY, universeWidth, universeHeight);
        }
        
        g2d.setColor(SET_A_BORDER);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(leftX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(SET_B_BORDER);
        g2d.drawOval(rightX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString(setNameA, leftX - CIRCLE_RADIUS + 15, centerY - CIRCLE_RADIUS + 25);
        g2d.drawString(setNameB, rightX + CIRCLE_RADIUS - 25, centerY - CIRCLE_RADIUS + 25);
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(TEXT_COLOR);
        drawResultText(g2d, operationResult, universeX, universeWidth);
    }
    
    /**
     * Renderiza un diagrama de tres conjuntos usando analisis de regiones
     */
    private static void renderThreeSetDiagram(Graphics2D g2d, VennDiagramData data) {
        List<String> uniqueSetNames = new ArrayList<>(data.getReferencedSets().keySet());
        String setNameA = uniqueSetNames.size() > 0 ? uniqueSetNames.get(0) : "A";
        String setNameB = uniqueSetNames.size() > 1 ? uniqueSetNames.get(1) : "B";
        String setNameC = uniqueSetNames.size() > 2 ? uniqueSetNames.get(2) : "C";
        
        Set<Object> setA = data.getReferencedSets().get(setNameA);
        Set<Object> setB = data.getReferencedSets().get(setNameB);
        Set<Object> setC = data.getReferencedSets().get(setNameC);
        Set<Object> operationResult = data.getResult();
        
        int centerX = DIAGRAM_WIDTH / 2;
        int centerY = DIAGRAM_HEIGHT / 2;
        
        int circleA_X = centerX;
        int circleA_Y = centerY - 35;
        
        int circleB_X = centerX - 50;
        int circleB_Y = centerY + 35;
        
        int circleC_X = centerX + 50;
        int circleC_Y = centerY + 35;
        
        int universeX = MARGIN;
        int universeY = MARGIN;
        int universeWidth = DIAGRAM_WIDTH - 2 * MARGIN;
        int universeHeight = DIAGRAM_HEIGHT - 2 * MARGIN;
        
        g2d.setColor(UNIVERSE_COLOR);
        g2d.fillRect(universeX, universeY, universeWidth, universeHeight);
        g2d.setColor(TEXT_COLOR);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(universeX, universeY, universeWidth, universeHeight);
        
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("U", universeX + universeWidth - 25, universeY + 20);
        
        if (setA != null && setB != null && setC != null && operationResult != null) {
            analyzeAndPaintThreeSetRegions(g2d, setA, setB, setC, operationResult, 
                                         circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y,
                                         universeX, universeY, universeWidth, universeHeight);
        }
        
        g2d.setColor(SET_A_BORDER);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(SET_B_BORDER);
        g2d.drawOval(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(SET_C_BORDER);
        g2d.drawOval(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString(setNameA, circleA_X - 10, circleA_Y - CIRCLE_RADIUS + 20);
        g2d.drawString(setNameB, circleB_X - CIRCLE_RADIUS + 15, circleB_Y + CIRCLE_RADIUS - 10);
        g2d.drawString(setNameC, circleC_X + CIRCLE_RADIUS - 25, circleC_Y + CIRCLE_RADIUS - 10);
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(TEXT_COLOR);
        drawResultText(g2d, operationResult, universeX, universeWidth);
    }
    
    /**
     * Renderiza un diagrama de cuatro conjuntos usando analisis de regiones
     */
    private static void renderFourSetDiagram(Graphics2D g2d, VennDiagramData data) {
        List<String> uniqueSetNames = new ArrayList<>(data.getReferencedSets().keySet());
        String setNameA = uniqueSetNames.size() > 0 ? uniqueSetNames.get(0) : "A";
        String setNameB = uniqueSetNames.size() > 1 ? uniqueSetNames.get(1) : "B";
        String setNameC = uniqueSetNames.size() > 2 ? uniqueSetNames.get(2) : "C";
        String setNameD = uniqueSetNames.size() > 3 ? uniqueSetNames.get(3) : "D";
        
        Set<Object> setA = data.getReferencedSets().get(setNameA);
        Set<Object> setB = data.getReferencedSets().get(setNameB);
        Set<Object> setC = data.getReferencedSets().get(setNameC);
        Set<Object> setD = data.getReferencedSets().get(setNameD);
        Set<Object> operationResult = data.getResult();
        
        int centerX = DIAGRAM_WIDTH / 2;
        int centerY = DIAGRAM_HEIGHT / 2;
        
        int circleA_X = centerX - 45;
        int circleA_Y = centerY - 25;
        
        int circleB_X = centerX + 45;
        int circleB_Y = centerY - 25;
        
        int circleC_X = centerX - 45;
        int circleC_Y = centerY + 25;
        
        int circleD_X = centerX + 45;
        int circleD_Y = centerY + 25;
        
        int universeX = MARGIN;
        int universeY = MARGIN;
        int universeWidth = DIAGRAM_WIDTH - 2 * MARGIN;
        int universeHeight = DIAGRAM_HEIGHT - 2 * MARGIN;
        
        g2d.setColor(UNIVERSE_COLOR);
        g2d.fillRect(universeX, universeY, universeWidth, universeHeight);
        g2d.setColor(TEXT_COLOR);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(universeX, universeY, universeWidth, universeHeight);
        
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("U", universeX + universeWidth - 25, universeY + 20);
        
        if (setA != null && setB != null && setC != null && setD != null && operationResult != null) {
            analyzeAndPaintFourSetRegions(g2d, setA, setB, setC, setD, operationResult, 
                                        circleA_X, circleA_Y, circleB_X, circleB_Y, 
                                        circleC_X, circleC_Y, circleD_X, circleD_Y,
                                        universeX, universeY, universeWidth, universeHeight);
        }
        
        g2d.setColor(SET_A_BORDER);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(SET_B_BORDER);
        g2d.drawOval(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(SET_C_BORDER);
        g2d.drawOval(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(SET_D_BORDER);
        g2d.drawOval(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString(setNameA, circleA_X - CIRCLE_RADIUS + 15, circleA_Y - CIRCLE_RADIUS + 20);
        g2d.drawString(setNameB, circleB_X + CIRCLE_RADIUS - 25, circleB_Y - CIRCLE_RADIUS + 20);
        g2d.drawString(setNameC, circleC_X - CIRCLE_RADIUS + 15, circleC_Y + CIRCLE_RADIUS - 10);
        g2d.drawString(setNameD, circleD_X + CIRCLE_RADIUS - 25, circleD_Y + CIRCLE_RADIUS - 10);
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(TEXT_COLOR);
        drawResultText(g2d, operationResult, universeX, universeWidth);
    }
    
    /**
     * Analiza todas las regiones posibles del diagrama de Venn y pinta solo las que contengan elementos del resultado
     * @param g2d Contexto gráfico
     * @param setA Conjunto A
     * @param setB Conjunto B
     * @param operationResult Resultado de la operacion
     * @param leftX Posición X del circulo A
     * @param rightX Posición X del circulo B
     * @param centerY Posición Y central
     * @param universeX Posición X del universo
     * @param universeY Posición Y del universo
     * @param universeWidth Ancho del universo
     * @param universeHeight Alto del universo
     */
    private static void analyzeAndPaintRegions(Graphics2D g2d, Set<Object> setA, Set<Object> setB, Set<Object> operationResult, 
                                             int leftX, int rightX, int centerY, int universeX, int universeY, int universeWidth, int universeHeight) {
        
        if (operationResult.isEmpty()) {
            return;
        }
        
        Set<Object> onlyA = calculateOnlyA(setA, setB);
        Set<Object> onlyB = calculateOnlyB(setA, setB);
        Set<Object> intersection = calculateIntersection(setA, setB);
        
        boolean paintOnlyA = hasCommonElements(onlyA, operationResult);
        boolean paintOnlyB = hasCommonElements(onlyB, operationResult);
        boolean paintIntersection = hasCommonElements(intersection, operationResult);
        boolean paintComplement = hasComplementElements(setA, setB, operationResult);
        
        if (paintOnlyA) {
            paintRegionOnlyA(g2d, leftX, rightX, centerY);
        }
        
        if (paintOnlyB) {
            paintRegionOnlyB(g2d, leftX, rightX, centerY);
        }
        
        if (paintIntersection) {
            paintRegionIntersection(g2d, leftX, rightX, centerY);
        }
        
        if (paintComplement) {
            paintRegionComplement(g2d, leftX, rightX, centerY, universeX, universeY, universeWidth, universeHeight);
        }
    }
    
    /**
     * Analiza todas las regiones posibles del diagrama de Venn de 3 conjuntos y pinta solo las que contengan elementos del resultado
     * @param g2d Contexto gráfico
     * @param setA Conjunto A
     * @param setB Conjunto B
     * @param setC Conjunto C
     * @param operationResult Resultado de la operacion
     * @param circleA_X Posición X del circulo A
     * @param circleA_Y Posición Y del circulo A
     * @param circleB_X Posición X del circulo B
     * @param circleB_Y Posición Y del circulo B
     * @param circleC_X Posición X del circulo C
     * @param circleC_Y Posición Y del circulo C
     * @param universeX Posición X del universo
     * @param universeY Posición Y del universo
     * @param universeWidth Ancho del universo
     * @param universeHeight Alto del universo
     */
    private static void analyzeAndPaintThreeSetRegions(Graphics2D g2d, Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> operationResult,
                                                      int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y,
                                                      int universeX, int universeY, int universeWidth, int universeHeight) {
        
        // Si el resultado está vacio, no pintar nada
        if (operationResult.isEmpty()) {
            return;
        }
        
        // Calcular las 8 regiones posibles del diagrama de Venn de 3 conjuntos
        Set<Object> onlyA = calculateOnlyA_ThreeSet(setA, setB, setC);                    // Solo A
        Set<Object> onlyB = calculateOnlyB_ThreeSet(setA, setB, setC);                    // Solo B
        Set<Object> onlyC = calculateOnlyC_ThreeSet(setA, setB, setC);                    // Solo C
        Set<Object> intersectionAB = calculateIntersectionAB_ThreeSet(setA, setB, setC);  // A ∩ B (pero no C)
        Set<Object> intersectionAC = calculateIntersectionAC_ThreeSet(setA, setB, setC);  // A ∩ C (pero no B)
        Set<Object> intersectionBC = calculateIntersectionBC_ThreeSet(setA, setB, setC);  // B ∩ C (pero no A)
        Set<Object> intersectionABC = calculateIntersectionABC_ThreeSet(setA, setB, setC); // A ∩ B ∩ C
        //Set<Object> complement = calculateComplement_ThreeSet(setA, setB, setC);           // Complemento
        
        // Verificar qué regiones tienen elementos que coinciden con el resultado
        boolean paintOnlyA = hasCommonElements(onlyA, operationResult);
        boolean paintOnlyB = hasCommonElements(onlyB, operationResult);
        boolean paintOnlyC = hasCommonElements(onlyC, operationResult);
        boolean paintIntersectionAB = hasCommonElements(intersectionAB, operationResult);
        boolean paintIntersectionAC = hasCommonElements(intersectionAC, operationResult);
        boolean paintIntersectionBC = hasCommonElements(intersectionBC, operationResult);
        boolean paintIntersectionABC = hasCommonElements(intersectionABC, operationResult);
        boolean paintComplement = hasComplementElements_ThreeSet(setA, setB, setC, operationResult);
        
        // Pintar las regiones que corresponden
        if (paintOnlyA) {
            paintThreeSetRegionOnlyA(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y);
        }
        
        if (paintOnlyB) {
            paintThreeSetRegionOnlyB(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y);
        }
        
        if (paintOnlyC) {
            paintThreeSetRegionOnlyC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y);
        }
        
        if (paintIntersectionAB) {
            paintThreeSetRegionIntersectionAB(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y);
        }
        
        if (paintIntersectionAC) {
            paintThreeSetRegionIntersectionAC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y);
        }
        
        if (paintIntersectionBC) {
            paintThreeSetRegionIntersectionBC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y);
        }
        
        if (paintIntersectionABC) {
            paintThreeSetRegionIntersectionABC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y);
        }
        
        if (paintComplement) {
            paintThreeSetRegionComplement(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, 
                                        universeX, universeY, universeWidth, universeHeight);
        }
    }
    
    /**
     * Analiza todas las regiones posibles del diagrama de Venn de 4 conjuntos y pinta solo las que contengan elementos del resultado
     * @param g2d Contexto gráfico
     * @param setA Conjunto A
     * @param setB Conjunto B
     * @param setC Conjunto C
     * @param setD Conjunto D
     * @param operationResult Resultado de la operacion
     * @param circleA_X Posición X del circulo A
     * @param circleA_Y Posición Y del circulo A
     * @param circleB_X Posición X del circulo B
     * @param circleB_Y Posición Y del circulo B
     * @param circleC_X Posición X del circulo C
     * @param circleC_Y Posición Y del circulo C
     * @param circleD_X Posición X del circulo D
     * @param circleD_Y Posición Y del circulo D
     * @param universeX Posición X del universo
     * @param universeY Posición Y del universo
     * @param universeWidth Ancho del universo
     * @param universeHeight Alto del universo
     */
    private static void analyzeAndPaintFourSetRegions(Graphics2D g2d, Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD, Set<Object> operationResult,
                                                     int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, 
                                                     int circleC_X, int circleC_Y, int circleD_X, int circleD_Y,
                                                     int universeX, int universeY, int universeWidth, int universeHeight) {
        
        // Si el resultado está vacio, no pintar nada
        if (operationResult.isEmpty()) {
            return;
        }
        
        // Calcular las 16 regiones posibles del diagrama de Venn de 4 conjuntos
        // Regiones únicas (solo un conjunto)
        Set<Object> onlyA = calculateOnlyA_FourSet(setA, setB, setC, setD);
        Set<Object> onlyB = calculateOnlyB_FourSet(setA, setB, setC, setD);
        Set<Object> onlyC = calculateOnlyC_FourSet(setA, setB, setC, setD);
        Set<Object> onlyD = calculateOnlyD_FourSet(setA, setB, setC, setD);
        
        // Intersecciones de 2 conjuntos (pero no los otros)
        Set<Object> intersectionAB = calculateIntersectionAB_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionAC = calculateIntersectionAC_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionAD = calculateIntersectionAD_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionBC = calculateIntersectionBC_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionBD = calculateIntersectionBD_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionCD = calculateIntersectionCD_FourSet(setA, setB, setC, setD);
        
        // Intersecciones de 3 conjuntos (pero no el cuarto)
        Set<Object> intersectionABC = calculateIntersectionABC_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionABD = calculateIntersectionABD_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionACD = calculateIntersectionACD_FourSet(setA, setB, setC, setD);
        Set<Object> intersectionBCD = calculateIntersectionBCD_FourSet(setA, setB, setC, setD);
        
        // interseccion de 4 conjuntos
        Set<Object> intersectionABCD = calculateIntersectionABCD_FourSet(setA, setB, setC, setD);
        
        // Complemento
        //Set<Object> complement = calculateComplement_FourSet(setA, setB, setC, setD);
        
        // Verificar qué regiones tienen elementos que coinciden con el resultado
        boolean paintOnlyA = hasCommonElements(onlyA, operationResult);
        boolean paintOnlyB = hasCommonElements(onlyB, operationResult);
        boolean paintOnlyC = hasCommonElements(onlyC, operationResult);
        boolean paintOnlyD = hasCommonElements(onlyD, operationResult);
        
        boolean paintIntersectionAB = hasCommonElements(intersectionAB, operationResult);
        boolean paintIntersectionAC = hasCommonElements(intersectionAC, operationResult);
        boolean paintIntersectionAD = hasCommonElements(intersectionAD, operationResult);
        boolean paintIntersectionBC = hasCommonElements(intersectionBC, operationResult);
        boolean paintIntersectionBD = hasCommonElements(intersectionBD, operationResult);
        boolean paintIntersectionCD = hasCommonElements(intersectionCD, operationResult);
        
        boolean paintIntersectionABC = hasCommonElements(intersectionABC, operationResult);
        boolean paintIntersectionABD = hasCommonElements(intersectionABD, operationResult);
        boolean paintIntersectionACD = hasCommonElements(intersectionACD, operationResult);
        boolean paintIntersectionBCD = hasCommonElements(intersectionBCD, operationResult);
        
        boolean paintIntersectionABCD = hasCommonElements(intersectionABCD, operationResult);
        boolean paintComplement = hasComplementElements_FourSet(setA, setB, setC, setD, operationResult);
        
        // Pintar las regiones que corresponden
        if (paintOnlyA) {
            paintFourSetRegionOnlyA(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintOnlyB) {
            paintFourSetRegionOnlyB(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintOnlyC) {
            paintFourSetRegionOnlyC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintOnlyD) {
            paintFourSetRegionOnlyD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionAB) {
            paintFourSetRegionIntersectionAB(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionAC) {
            paintFourSetRegionIntersectionAC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionAD) {
            paintFourSetRegionIntersectionAD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionBC) {
            paintFourSetRegionIntersectionBC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionBD) {
            paintFourSetRegionIntersectionBD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionCD) {
            paintFourSetRegionIntersectionCD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionABC) {
            paintFourSetRegionIntersectionABC(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionABD) {
            paintFourSetRegionIntersectionABD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionACD) {
            paintFourSetRegionIntersectionACD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionBCD) {
            paintFourSetRegionIntersectionBCD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintIntersectionABCD) {
            paintFourSetRegionIntersectionABCD(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y);
        }
        
        if (paintComplement) {
            paintFourSetRegionComplement(g2d, circleA_X, circleA_Y, circleB_X, circleB_Y, circleC_X, circleC_Y, circleD_X, circleD_Y,
                                       universeX, universeY, universeWidth, universeHeight);
        }
    }
    
    /**
     * Dibuja el texto del resultado con manejo inteligente de texto largo
     * @param g2d Contexto gráfico
     * @param operationResult Resultado de la operacion
     * @param universeX Posición X del universo
     * @param universeWidth Ancho del universo
     */
    private static void drawResultText(Graphics2D g2d, Set<Object> operationResult, int universeX, int universeWidth) {
        String resultText;
        
        // Verificar si el resultado está vacio
        if (operationResult == null || operationResult.isEmpty()) {
            resultText = "Resultado: ∅ (vacío)";
        } else {
            // Limitar el número de elementos mostrados para evitar desbordamiento
            int maxElements = 15; // Máximo 15 elementos visibles
            List<Object> elementList = new ArrayList<>(operationResult);
            
            if (elementList.size() <= maxElements) {
                // Mostrar todos los elementos
                resultText = "Resultado: " + operationResult.toString();
            } else {
                // Mostrar solo los primeros elementos + "..."
                List<Object> limitedElements = elementList.subList(0, maxElements);
                Set<Object> limitedSet = new LinkedHashSet<>(limitedElements);
                String limitedString = limitedSet.toString();
                // Remover el "]" final y agregar "... y X más]"
                limitedString = limitedString.substring(0, limitedString.length() - 1);
                int remaining = elementList.size() - maxElements;
                resultText = "Resultado: " + limitedString + "... y " + remaining + " más]";
            }
        }
        
        Font font = new Font("SansSerif", Font.PLAIN, 18);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics(font);
        
        // Calcular el ancho disponible para el texto
        int maxWidth = universeWidth - 20; // Margen de 10 píxeles en cada lado
        int textWidth = fm.stringWidth(resultText);
        
        int startY = DIAGRAM_HEIGHT - 25; // Ajustado para alta resolución
        
        // Si el texto aún es muy largo, aplicar truncado inteligente
        if (textWidth > maxWidth) {
            // Truncar el texto y agregar "..."
            String truncatedText = "Resultado: ";
            String ellipsis = "...";
            int ellipsisWidth = fm.stringWidth(ellipsis);
            int availableWidth = maxWidth - fm.stringWidth(truncatedText) - ellipsisWidth;
            
            if (operationResult.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (Object element : operationResult) {
                    String elementStr = element.toString();
                    String testText = sb.length() > 0 ? sb + ", " + elementStr : elementStr;
                    
                    if (fm.stringWidth(testText) > availableWidth) {
                        break;
                    }
                    
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(elementStr);
                }
                
                resultText = truncatedText + "{" + sb.toString() + ellipsis + "}";
            }
        }
        
        // Dibujar el texto centrado en la parte inferior
        int textX = universeX + 10;
        g2d.drawString(resultText, textX, startY);
    }
    
    /**
     * Calcula los elementos que están solo en A (A - B)
     */
    private static Set<Object> calculateOnlyA(Set<Object> setA, Set<Object> setB) {
        Set<Object> result = new HashSet<>(setA);
        result.removeAll(setB);
        return result;
    }
    
    /**
     * Calcula los elementos que están solo en B (B - A)
     */
    private static Set<Object> calculateOnlyB(Set<Object> setA, Set<Object> setB) {
        Set<Object> result = new HashSet<>(setB);
        result.removeAll(setA);
        return result;
    }
    
    /**
     * Calcula la interseccion entre A y B
     */
    private static Set<Object> calculateIntersection(Set<Object> setA, Set<Object> setB) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setB);
        return result;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS DE CÁLCULO DE REGIONES PARA 3 CONJUNTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Calcula los elementos que están solo en A (A - B - C)
     */
    private static Set<Object> calculateOnlyA_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC) {
        Set<Object> result = new HashSet<>(setA);
        result.removeAll(setB);
        result.removeAll(setC);
        return result;
    }
    
    /**
     * Calcula los elementos que están solo en B (B - A - C)
     */
    private static Set<Object> calculateOnlyB_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC) {
        Set<Object> result = new HashSet<>(setB);
        result.removeAll(setA);
        result.removeAll(setC);
        return result;
    }
    
    /**
     * Calcula los elementos que están solo en C (C - A - B)
     */
    private static Set<Object> calculateOnlyC_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC) {
        Set<Object> result = new HashSet<>(setC);
        result.removeAll(setA);
        result.removeAll(setB);
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ B pero no en C
     */
    private static Set<Object> calculateIntersectionAB_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setB);  // A ∩ B
        result.removeAll(setC);  // (A ∩ B) - C
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ C pero no en B
     */
    private static Set<Object> calculateIntersectionAC_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setC);  // A ∩ C
        result.removeAll(setB);  // (A ∩ C) - B
        return result;
    }
    
    /**
     * Calcula los elementos que están en B ∩ C pero no en A
     */
    private static Set<Object> calculateIntersectionBC_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC) {
        Set<Object> result = new HashSet<>(setB);
        result.retainAll(setC);  // B ∩ C
        result.removeAll(setA);  // (B ∩ C) - A
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ B ∩ C
     */
    private static Set<Object> calculateIntersectionABC_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setB);  // A ∩ B
        result.retainAll(setC);  // (A ∩ B) ∩ C
        return result;
    }
    
    /**
     * Verifica si el resultado contiene elementos que pertenecen al complemento de 3 conjuntos
     * (elementos que no están en A, B ni C)
     */
    private static boolean hasComplementElements_ThreeSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> operationResult) {
        if (operationResult.isEmpty()) {
            return false;
        }
        
        // Calcular la union de A, B y C
        Set<Object> union = new HashSet<>(setA);
        union.addAll(setB);
        union.addAll(setC);
        
        // Verificar si hay elementos en el resultado que no están en la union
        for (Object element : operationResult) {
            if (!union.contains(element)) {
                return true;
            }
        }
        
        return false;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS DE CÁLCULO DE REGIONES PARA 4 CONJUNTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Calcula los elementos que están solo en A (A - B - C - D)
     */
    private static Set<Object> calculateOnlyA_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.removeAll(setB);
        result.removeAll(setC);
        result.removeAll(setD);
        return result;
    }
    
    /**
     * Calcula los elementos que están solo en B (B - A - C - D)
     */
    private static Set<Object> calculateOnlyB_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setB);
        result.removeAll(setA);
        result.removeAll(setC);
        result.removeAll(setD);
        return result;
    }
    
    /**
     * Calcula los elementos que están solo en C (C - A - B - D)
     */
    private static Set<Object> calculateOnlyC_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setC);
        result.removeAll(setA);
        result.removeAll(setB);
        result.removeAll(setD);
        return result;
    }
    
    /**
     * Calcula los elementos que están solo en D (D - A - B - C)
     */
    private static Set<Object> calculateOnlyD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setD);
        result.removeAll(setA);
        result.removeAll(setB);
        result.removeAll(setC);
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ B pero no en C ni D
     */
    private static Set<Object> calculateIntersectionAB_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setB);  // A ∩ B
        result.removeAll(setC);  // (A ∩ B) - C
        result.removeAll(setD);  // (A ∩ B) - C - D
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ C pero no en B ni D
     */
    private static Set<Object> calculateIntersectionAC_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setC);  // A ∩ C
        result.removeAll(setB);  // (A ∩ C) - B
        result.removeAll(setD);  // (A ∩ C) - B - D
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ D pero no en B ni C
     */
    private static Set<Object> calculateIntersectionAD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setD);  // A ∩ D
        result.removeAll(setB);  // (A ∩ D) - B
        result.removeAll(setC);  // (A ∩ D) - B - C
        return result;
    }
    
    /**
     * Calcula los elementos que están en B ∩ C pero no en A ni D
     */
    private static Set<Object> calculateIntersectionBC_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setB);
        result.retainAll(setC);  // B ∩ C
        result.removeAll(setA);  // (B ∩ C) - A
        result.removeAll(setD);  // (B ∩ C) - A - D
        return result;
    }
    
    /**
     * Calcula los elementos que están en B ∩ D pero no en A ni C
     */
    private static Set<Object> calculateIntersectionBD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setB);
        result.retainAll(setD);  // B ∩ D
        result.removeAll(setA);  // (B ∩ D) - A
        result.removeAll(setC);  // (B ∩ D) - A - C
        return result;
    }
    
    /**
     * Calcula los elementos que están en C ∩ D pero no en A ni B
     */
    private static Set<Object> calculateIntersectionCD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setC);
        result.retainAll(setD);  // C ∩ D
        result.removeAll(setA);  // (C ∩ D) - A
        result.removeAll(setB);  // (C ∩ D) - A - B
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ B ∩ C pero no en D
     */
    private static Set<Object> calculateIntersectionABC_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setB);  // A ∩ B
        result.retainAll(setC);  // A ∩ B ∩ C
        result.removeAll(setD);  // (A ∩ B ∩ C) - D
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ B ∩ D pero no en C
     */
    private static Set<Object> calculateIntersectionABD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setB);  // A ∩ B
        result.retainAll(setD);  // A ∩ B ∩ D
        result.removeAll(setC);  // (A ∩ B ∩ D) - C
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ C ∩ D pero no en B
     */
    private static Set<Object> calculateIntersectionACD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setC);  // A ∩ C
        result.retainAll(setD);  // A ∩ C ∩ D
        result.removeAll(setB);  // (A ∩ C ∩ D) - B
        return result;
    }
    
    /**
     * Calcula los elementos que están en B ∩ C ∩ D pero no en A
     */
    private static Set<Object> calculateIntersectionBCD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setB);
        result.retainAll(setC);  // B ∩ C
        result.retainAll(setD);  // B ∩ C ∩ D
        result.removeAll(setA);  // (B ∩ C ∩ D) - A
        return result;
    }
    
    /**
     * Calcula los elementos que están en A ∩ B ∩ C ∩ D
     */
    private static Set<Object> calculateIntersectionABCD_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD) {
        Set<Object> result = new HashSet<>(setA);
        result.retainAll(setB);  // A ∩ B
        result.retainAll(setC);  // A ∩ B ∩ C
        result.retainAll(setD);  // A ∩ B ∩ C ∩ D
        return result;
    }
    /**
     * Verifica si el resultado contiene elementos que pertenecen al complemento de 4 conjuntos
     * (elementos que no están en A, B, C ni D)
     */
    private static boolean hasComplementElements_FourSet(Set<Object> setA, Set<Object> setB, Set<Object> setC, Set<Object> setD, Set<Object> operationResult) {
        if (operationResult.isEmpty()) {
            return false;
        }
        
        // Calcular la union de A, B, C y D
        Set<Object> union = new HashSet<>(setA);
        union.addAll(setB);
        union.addAll(setC);
        union.addAll(setD);
        
        // Verificar si hay elementos en el resultado que no están en la union
        for (Object element : operationResult) {
            if (!union.contains(element)) {
                return true;
            }
        }
        
        return false;
    }
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS DE PINTADO DE REGIONES PARA 3 CONJUNTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Pinta la region que pertenece solo a A (A - B - C)
     */
    private static void paintThreeSetRegionOnlyA(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y) {
        
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular A - B - C
        java.awt.geom.Area onlyA = new java.awt.geom.Area(areaA);
        onlyA.subtract(areaB);
        onlyA.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyA);
    }
    
    /**
     * Pinta la region que pertenece solo a B (B - A - C)
     */
    private static void paintThreeSetRegionOnlyB(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular B - A - C
        java.awt.geom.Area onlyB = new java.awt.geom.Area(areaB);
        onlyB.subtract(areaA);
        onlyB.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyB);
    }
    
    /**
     * Pinta la region que pertenece solo a C (C - A - B)
     */
    private static void paintThreeSetRegionOnlyC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular C - A - B
        java.awt.geom.Area onlyC = new java.awt.geom.Area(areaC);
        onlyC.subtract(areaA);
        onlyC.subtract(areaB);
        
        // Pintar la region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyC);
    }
    
    /**
     * Pinta la region de interseccion A ∩ B (pero no C)
     */
    private static void paintThreeSetRegionIntersectionAB(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ B) - C
        java.awt.geom.Area intersectionAB = new java.awt.geom.Area(areaA);
        intersectionAB.intersect(areaB);
        intersectionAB.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionAB);
    }
    
    /**
     * Pinta la region de interseccion A ∩ C (pero no B)
     */
    private static void paintThreeSetRegionIntersectionAC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ C) - B
        java.awt.geom.Area intersectionAC = new java.awt.geom.Area(areaA);
        intersectionAC.intersect(areaC);
        intersectionAC.subtract(areaB);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionAC);
    }
    
    /**
     * Pinta la region de interseccion B ∩ C (pero no A)
     */
    private static void paintThreeSetRegionIntersectionBC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (B ∩ C) - A
        java.awt.geom.Area intersectionBC = new java.awt.geom.Area(areaB);
        intersectionBC.intersect(areaC);
        intersectionBC.subtract(areaA);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionBC);
    }
    
    /**
     * Pinta la region de interseccion A ∩ B ∩ C
     */
    private static void paintThreeSetRegionIntersectionABC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular A ∩ B ∩ C
        java.awt.geom.Area intersectionABC = new java.awt.geom.Area(areaA);
        intersectionABC.intersect(areaB);
        intersectionABC.intersect(areaC);
        
        // Pintar la region
        g2d.setColor(TRIPLE_INTERSECTION_COLOR); // Color púrpura para la interseccion triple
        g2d.fill(intersectionABC);
    }
    
    /**
     * Pinta la region del complemento para 3 conjuntos (U - (A ∪ B ∪ C))
     */
    private static void paintThreeSetRegionComplement(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y,
                                                    int universeX, int universeY, int universeWidth, int universeHeight) {
        // Crear área del universo
        java.awt.geom.Area universeArea = new java.awt.geom.Area(new java.awt.geom.Rectangle2D.Double(universeX , universeY , universeWidth , universeHeight ));
        
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular complemento (Universo - A - B - C)
        java.awt.geom.Area complement = new java.awt.geom.Area(universeArea);
        complement.subtract(areaA);
        complement.subtract(areaB);
        complement.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(COMPLEMENT_COLOR);
        g2d.fill(complement);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS DE PINTADO DE REGIONES PARA 4 CONJUNTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Pinta la region que pertenece solo a A (A - B - C - D)
     */
    private static void paintFourSetRegionOnlyA(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular A - B - C - D
        java.awt.geom.Area onlyA = new java.awt.geom.Area(areaA);
        onlyA.subtract(areaB);
        onlyA.subtract(areaC);
        onlyA.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyA);
    }
    
    /**
     * Pinta la region que pertenece solo a B (B - A - C - D)
     */
    private static void paintFourSetRegionOnlyB(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular B - A - C - D
        java.awt.geom.Area onlyB = new java.awt.geom.Area(areaB);
        onlyB.subtract(areaA);
        onlyB.subtract(areaC);
        onlyB.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyB);
    }
    
    /**
     * Pinta la region que pertenece solo a C (C - A - B - D)
     */
    private static void paintFourSetRegionOnlyC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular C - A - B - D
        java.awt.geom.Area onlyC = new java.awt.geom.Area(areaC);
        onlyC.subtract(areaA);
        onlyC.subtract(areaB);
        onlyC.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyC);
    }
    
    /**
     * Pinta la region que pertenece solo a D (D - A - B - C)
     */
    private static void paintFourSetRegionOnlyD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular D - A - B - C
        java.awt.geom.Area onlyD = new java.awt.geom.Area(areaD);
        onlyD.subtract(areaA);
        onlyD.subtract(areaB);
        onlyD.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyD);
    }
    
    /**
     * Pinta la region de interseccion A ∩ B (pero no C ni D)
     */
    private static void paintFourSetRegionIntersectionAB(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ B) - C - D
        java.awt.geom.Area intersectionAB = new java.awt.geom.Area(areaA);
        intersectionAB.intersect(areaB);
        intersectionAB.subtract(areaC);
        intersectionAB.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionAB);
    }
    
    /**
     * Pinta la region de interseccion A ∩ C (pero no B ni D)
     */
    private static void paintFourSetRegionIntersectionAC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ C) - B - D
        java.awt.geom.Area intersectionAC = new java.awt.geom.Area(areaA);
        intersectionAC.intersect(areaC);
        intersectionAC.subtract(areaB);
        intersectionAC.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionAC);
    }
    
    /**
     * Pinta la region de interseccion A ∩ D (pero no B ni C)
     */
    private static void paintFourSetRegionIntersectionAD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ D) - B - C
        java.awt.geom.Area intersectionAD = new java.awt.geom.Area(areaA);
        intersectionAD.intersect(areaD);
        intersectionAD.subtract(areaB);
        intersectionAD.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionAD);
    }
    
    /**
     * Pinta la region de interseccion B ∩ C (pero no A ni D)
     */
    private static void paintFourSetRegionIntersectionBC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (B ∩ C) - A - D
        java.awt.geom.Area intersectionBC = new java.awt.geom.Area(areaB);
        intersectionBC.intersect(areaC);
        intersectionBC.subtract(areaA);
        intersectionBC.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionBC);
    }
    
    /**
     * Pinta la region de interseccion B ∩ D (pero no A ni C)
     */
    private static void paintFourSetRegionIntersectionBD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (B ∩ D) - A - C
        java.awt.geom.Area intersectionBD = new java.awt.geom.Area(areaB);
        intersectionBD.intersect(areaD);
        intersectionBD.subtract(areaA);
        intersectionBD.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionBD);
    }
    
    /**
     * Pinta la region de interseccion C ∩ D (pero no A ni B)
     */
    private static void paintFourSetRegionIntersectionCD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (C ∩ D) - A - B
        java.awt.geom.Area intersectionCD = new java.awt.geom.Area(areaC);
        intersectionCD.intersect(areaD);
        intersectionCD.subtract(areaA);
        intersectionCD.subtract(areaB);
        
        // Pintar la region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersectionCD);
    }
    
    /**
     * Pinta la region de interseccion A ∩ B ∩ C (pero no D)
     */
    private static void paintFourSetRegionIntersectionABC(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ B ∩ C) - D
        java.awt.geom.Area intersectionABC = new java.awt.geom.Area(areaA);
        intersectionABC.intersect(areaB);
        intersectionABC.intersect(areaC);
        intersectionABC.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(TRIPLE_INTERSECTION_COLOR); // Color púrpura para la interseccion triple
        g2d.fill(intersectionABC);
    }
    
    /**
     * Pinta la region de interseccion A ∩ B ∩ D (pero no C)
     */
    private static void paintFourSetRegionIntersectionABD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ B ∩ D) - C
        java.awt.geom.Area intersectionABD = new java.awt.geom.Area(areaA);
        intersectionABD.intersect(areaB);
        intersectionABD.intersect(areaD);
        intersectionABD.subtract(areaC);
        
        // Pintar la region
        g2d.setColor(TRIPLE_INTERSECTION_COLOR); // Color púrpura para la interseccion triple
        g2d.fill(intersectionABD);
    }
    
    /**
     * Pinta la region de interseccion A ∩ C ∩ D (pero no B)
     */
    private static void paintFourSetRegionIntersectionACD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (A ∩ C ∩ D) - B
        java.awt.geom.Area intersectionACD = new java.awt.geom.Area(areaA);
        intersectionACD.intersect(areaC);
        intersectionACD.intersect(areaD);
        intersectionACD.subtract(areaB);
        
        // Pintar la region
        g2d.setColor(TRIPLE_INTERSECTION_COLOR); // Color púrpura para la interseccion triple
        g2d.fill(intersectionACD);
    }
    
    /**
     * Pinta la region de interseccion B ∩ C ∩ D (pero no A)
     */
    private static void paintFourSetRegionIntersectionBCD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular (B ∩ C ∩ D) - A
        java.awt.geom.Area intersectionBCD = new java.awt.geom.Area(areaB);
        intersectionBCD.intersect(areaC);
        intersectionBCD.intersect(areaD);
        intersectionBCD.subtract(areaA);
        
        // Pintar la region
        g2d.setColor(TRIPLE_INTERSECTION_COLOR); // Color púrpura para la interseccion triple
        g2d.fill(intersectionBCD);
    }
    
    /**
     * Pinta la region de interseccion A ∩ B ∩ C ∩ D
     */
    private static void paintFourSetRegionIntersectionABCD(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y) {
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular A ∩ B ∩ C ∩ D
        java.awt.geom.Area intersectionABCD = new java.awt.geom.Area(areaA);
        intersectionABCD.intersect(areaB);
        intersectionABCD.intersect(areaC);
        intersectionABCD.intersect(areaD);
        
        // Pintar la region
        g2d.setColor(QUADRUPLE_INTERSECTION_COLOR); // Color magenta para la interseccion de 4 conjuntos
        g2d.fill(intersectionABCD);
    }
    
    /**
     * Pinta la region del complemento para 4 conjuntos (U - (A ∪ B ∪ C ∪ D))
     */
    private static void paintFourSetRegionComplement(Graphics2D g2d, int circleA_X, int circleA_Y, int circleB_X, int circleB_Y, int circleC_X, int circleC_Y, int circleD_X, int circleD_Y,
                                                   int universeX, int universeY, int universeWidth, int universeHeight) {
        // Crear área del universo
        java.awt.geom.Area universeArea = new java.awt.geom.Area(new java.awt.geom.Rectangle2D.Double(universeX , universeY , universeWidth, universeHeight));
        
        // Crear áreas de los circulos
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleA_X - CIRCLE_RADIUS, circleA_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleB_X - CIRCLE_RADIUS, circleB_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaC = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleC_X - CIRCLE_RADIUS, circleC_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        java.awt.geom.Area areaD = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(circleD_X - CIRCLE_RADIUS, circleD_Y - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular complemento (Universo - A - B - C - D)
        java.awt.geom.Area complement = new java.awt.geom.Area(universeArea);
        complement.subtract(areaA);
        complement.subtract(areaB);
        complement.subtract(areaC);
        complement.subtract(areaD);
        
        // Pintar la region
        g2d.setColor(COMPLEMENT_COLOR);
        g2d.fill(complement);
    }
    
    /**
     * Verifica si dos conjuntos tienen elementos en común
     * También maneja el caso especial del complemento
     */
    private static boolean hasCommonElements(Set<Object> regionElements, Set<Object> operationResult) {
        if (operationResult.isEmpty()) {
            return false;
        }
        
        // Caso normal: verificar interseccion directa
        for (Object element : regionElements) {
            if (operationResult.contains(element)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si el resultado contiene elementos que pertenecen al complemento
     * (elementos que no están en A ni en B)
     */
    private static boolean hasComplementElements(Set<Object> setA, Set<Object> setB, Set<Object> operationResult) {
        if (operationResult.isEmpty()) {
            return false;
        }
        
        // Calcular la union de A y B
        Set<Object> union = new HashSet<>(setA);
        union.addAll(setB);
        
        // Verificar si hay elementos en el resultado que no están en la union
        for (Object element : operationResult) {
            if (!union.contains(element)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Pinta la region que pertenece solo a A (A - B)
     */
    private static void paintRegionOnlyA(Graphics2D g2d, int leftX, int rightX, int centerY) {
        // Crear área del circulo A
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(leftX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Crear área del circulo B
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(rightX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular A - B
        java.awt.geom.Area onlyA = new java.awt.geom.Area(areaA);
        onlyA.subtract(areaB);
        
        // Pintar solo esa region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyA);
    }
    
    /**
     * Pinta la region que pertenece solo a B (B - A)
     */
    private static void paintRegionOnlyB(Graphics2D g2d, int leftX, int rightX, int centerY) {
        // Crear área del circulo A
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(leftX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Crear área del circulo B
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(rightX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular B - A
        java.awt.geom.Area onlyB = new java.awt.geom.Area(areaB);
        onlyB.subtract(areaA);
        
        // Pintar solo esa region
        g2d.setColor(DIFFERENCE_COLOR);
        g2d.fill(onlyB);
    }
    
    /**
     * Pinta la region de interseccion (A ∩ B)
     */
    private static void paintRegionIntersection(Graphics2D g2d, int leftX, int rightX, int centerY) {
        // Crear área del circulo A
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(leftX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Crear área del circulo B
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(rightX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular A ∩ B
        java.awt.geom.Area intersection = new java.awt.geom.Area(areaA);
        intersection.intersect(areaB);
        
        // Pintar solo esa region
        g2d.setColor(INTERSECTION_COLOR);
        g2d.fill(intersection);
    }
    
    /**
     * Pinta la region del complemento (U - (A ∪ B))
     */
    private static void paintRegionComplement(Graphics2D g2d, int leftX, int rightX, int centerY, int universeX, int universeY, int universeWidth, int universeHeight) {
        // Crear área del universo
        java.awt.geom.Area universeArea = new java.awt.geom.Area(new java.awt.geom.Rectangle2D.Double(universeX, universeY , universeWidth , universeHeight));
        
        // Crear área del circulo A
        java.awt.geom.Area areaA = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(leftX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Crear área del circulo B
        java.awt.geom.Area areaB = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(rightX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular complemento (Universo - A - B)
        java.awt.geom.Area complement = new java.awt.geom.Area(universeArea);
        complement.subtract(areaA);
        complement.subtract(areaB);
        
        // Pintar solo esa region
        g2d.setColor(COMPLEMENT_COLOR);
        g2d.fill(complement);
    }
    
    /**
     * Renderiza un diagrama para un solo conjunto
     */
    private static void renderSingleSetDiagram(Graphics2D g2d, VennDiagramData data, String setName) {
        // Posición del circulo centrado
        int centerX = DIAGRAM_WIDTH / 2;
        int centerY = DIAGRAM_HEIGHT / 2;
        
        // Dimensiones del rectángulo del universo
        int universeX = MARGIN;
        int universeY = MARGIN;
        int universeWidth = DIAGRAM_WIDTH - 2 * MARGIN;
        int universeHeight = DIAGRAM_HEIGHT - 2 * MARGIN;
        
        // Dibujar rectángulo del universo
        g2d.setColor(UNIVERSE_COLOR);
        g2d.fillRect(universeX, universeY, universeWidth, universeHeight);
        g2d.setColor(TEXT_COLOR);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(universeX, universeY, universeWidth, universeHeight);
        
        // Etiqueta del universo
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("U", universeX + universeWidth - 25, universeY + 20);
        
        // Renderizar según el tipo de operacion
        String operation = data.getOperation().toUpperCase();
        
        switch (operation) {
            case "INTERSECTION":
                // A ∩ A = A - dibujar el conjunto completo
                renderSingleSetFull(g2d, data, setName, centerX, centerY, INTERSECTION_COLOR);
                break;
            case "UNION":
                // A ∪ A = A - dibujar el conjunto completo
                renderSingleSetFull(g2d, data, setName, centerX, centerY, UNION_COLOR);
                break;
            case "DIFFERENCE":
                // A - A = ∅ - no dibujar nada relleno
                renderSingleSetEmpty(g2d, data, setName, centerX, centerY);
                break;
            case "COMPLEMENT":
                // ^A - dibujar el complemento
                renderSingleSetComplement(g2d, data, setName, centerX, centerY, universeX, universeY, universeWidth, universeHeight);
                break;
            default:
                renderSingleSetFull(g2d, data, setName, centerX, centerY, UNION_COLOR);
                break;
        }
        
        // Resultado en la parte inferior con manejo de texto largo
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(TEXT_COLOR);
        drawResultText(g2d, data.getResult(), universeX, universeWidth);
    }
    
    /**
     * Renderiza un conjunto completo (A ∪ A, A ∩ A)
     */
    private static void renderSingleSetFull(Graphics2D g2d, VennDiagramData data, String setName, int centerX, int centerY, Color fillColor) {
        // Dibujar circulo relleno
        g2d.setColor(fillColor);
        g2d.fillOval(centerX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        // Contorno del circulo
        g2d.setColor(SET_A_BORDER);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        // Etiqueta del conjunto
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString(setName, centerX - CIRCLE_RADIUS + 15, centerY - CIRCLE_RADIUS + 25);
    }
    
    /**
     * Renderiza un conjunto vacio (A - A)
     */
    private static void renderSingleSetEmpty(Graphics2D g2d, VennDiagramData data, String setName, int centerX, int centerY) {
        // Solo dibujar el contorno del circulo (sin relleno)
        g2d.setColor(SET_A_BORDER);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        // Etiqueta del conjunto
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString(setName, centerX - CIRCLE_RADIUS + 15, centerY - CIRCLE_RADIUS + 25);
        
        // Indicar que está vacio
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("∅", centerX - 5, centerY + 5);
    }
    
    /**
     * Renderiza el complemento de un conjunto (^A)
     */
    private static void renderSingleSetComplement(Graphics2D g2d, VennDiagramData data, String setName, int centerX, int centerY, int universeX, int universeY, int universeWidth, int universeHeight) {
        // Crear área del universo (ajustada al rectángulo completo)
        java.awt.geom.Area universeArea = new java.awt.geom.Area(new java.awt.geom.Rectangle2D.Double(universeX , universeY , universeWidth , universeHeight ));
        
        // Crear área del conjunto A
        java.awt.geom.Area setArea = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(centerX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
        
        // Calcular complemento (Universo - A)
        java.awt.geom.Area complement = new java.awt.geom.Area(universeArea);
        complement.subtract(setArea);
        
        // SOLO dibujar el complemento - que ocupa todo el universo menos el circulo
        g2d.setColor(COMPLEMENT_COLOR);
        g2d.fill(complement);
        
        // Contorno del conjunto A (solo borde)
        g2d.setColor(SET_A_BORDER);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - CIRCLE_RADIUS, centerY - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
        
        // Etiqueta del conjunto
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.drawString(setName, centerX - CIRCLE_RADIUS + 15, centerY - CIRCLE_RADIUS + 25);
        
        // Etiqueta del complemento
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("^" + setName, universeX + 30, universeY + 45);
    }
    public static Color getCircleBorder() { return CIRCLE_BORDER; }
    public static Color getUniverseColor() { return UNIVERSE_COLOR; }
}
