package Graphics;

import Environment.Environment;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * Panel personalizado para renderizar diagramas de Venn.
 * Soporta operaciones de union, interseccion, diferencia y complemento
 * para hasta 4 conjuntos.
 * 
 * @author danie
 */
public class VennDiagramPanel extends JPanel {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // ATRIBUTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private final VennDiagramData diagramData;
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 14);
    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
    private final Font elementFont = new Font("SansSerif", Font.PLAIN, 18);
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Constructor del panel de diagrama de Venn.
     * 
     * @param diagramData Datos del diagrama
     */
    public VennDiagramPanel(VennDiagramData diagramData) {
        this.diagramData = diagramData;
        setupPanel();
    }
    
    /**
     * Configuración inicial del panel.
     */
    private void setupPanel() {
        setPreferredSize(new Dimension(VennDiagramGenerator.getDiagramWidth(), 
                                      VennDiagramGenerator.getDiagramHeight()));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE RENDERIZADO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Activar antialiasing para mejor calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (!diagramData.hasValidData()) {
            drawNoDataMessage(g2d);
            return;
        }
        
        // Dibujar titulo
        drawTitle(g2d);
        
        // Dibujar diagrama según el tipo de operacion
        if (diagramData.isUnaryOperation()) {
            drawUnaryOperation(g2d);
        } else if (diagramData.isBinaryOperation()) {
            drawBinaryOperation(g2d);
        } else {
            drawUnsupportedMessage(g2d);
        }
        
        // Dibujar informacion adicional
        drawOperationInfo(g2d);
    }
    
    /**
     * Dibuja el titulo del diagrama.
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawTitle(Graphics2D g2d) {
        g2d.setFont(titleFont);
        g2d.setColor(Color.BLACK);
        
        String title = diagramData.getOperationName() + " - " + 
                      VennDiagramGenerator.getOperationName(diagramData.getOperator());
        
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (getWidth() - titleWidth) / 2;
        
        g2d.drawString(title, titleX, 20);
    }
    
    /**
     * Dibuja una operacion unaria (complemento o conjunto simple).
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawUnaryOperation(Graphics2D g2d) {
        if (diagramData.getOperator().equals("^")) {
            drawComplementOperation(g2d);
        } else {
            drawSingleSet(g2d);
        }
    }
    
    /**
     * Dibuja una operacion binaria (union, interseccion, diferencia).
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawBinaryOperation(Graphics2D g2d) {
        List<String> operands = diagramData.getOperands();
        String setA = operands.get(0);
        String setB = operands.get(1);
        
        // Dibujar universo
        drawUniverse(g2d);
        
        // Posiciones de los circulos
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = VennDiagramGenerator.getCircleRadius();
        int overlap = radius / 2;
        
        int circleAX = centerX - overlap;
        int circleAY = centerY;
        int circleBX = centerX + overlap;
        int circleBY = centerY;
        
        // Crear áreas de los circulos
        Area areaA = new Area(new Ellipse2D.Double(circleAX - radius, circleAY - radius, 
                                                  radius * 2, radius * 2));
        Area areaB = new Area(new Ellipse2D.Double(circleBX - radius, circleBY - radius, 
                                                  radius * 2, radius * 2));
        
        // Dibujar área de resultado según la operacion
        drawOperationResult(g2d, areaA, areaB);
        
        // Dibujar contornos de los circulos
        g2d.setColor(VennDiagramGenerator.getCircleBorder());
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(circleAX - radius, circleAY - radius, radius * 2, radius * 2));
        g2d.draw(new Ellipse2D.Double(circleBX - radius, circleBY - radius, radius * 2, radius * 2));
        
        // Dibujar etiquetas de los conjuntos
        drawSetLabels(g2d, setA, setB, circleAX, circleAY, circleBX, circleBY, radius);
        
        // Dibujar elementos en las regiones apropiadas
        drawSetElements(g2d, setA, setB, circleAX, circleAY, circleBX, circleBY, radius);
    }
    
    /**
     * Dibuja el resultado de la operacion resaltando las áreas apropiadas.
     * 
     * @param g2d Contexto grafico 2D
     * @param areaA Área del conjunto A
     * @param areaB Área del conjunto B
     */
    private void drawOperationResult(Graphics2D g2d, Area areaA, Area areaB) {
        Color operationColor = VennDiagramGenerator.getOperationColor(diagramData.getOperator());
        g2d.setColor(operationColor);
        
        Area resultArea = new Area();
        
        switch (diagramData.getOperator()) {
            case "U": 
                resultArea.add(areaA);
                resultArea.add(areaB);
                break;
            case "&": 
                resultArea.add(areaA);
                resultArea.intersect(areaB);
                break;
            case "-": 
                resultArea.add(areaA);
                resultArea.subtract(areaB);
                break;
        }
        
        g2d.fill(resultArea);
    }
    
    /**
     * Dibuja un conjunto simple o el resultado de una operacion unaria.
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawSingleSet(Graphics2D g2d) {
        drawUniverse(g2d);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = VennDiagramGenerator.getCircleRadius();
        
        // Dibujar círculo relleno
        g2d.setColor(VennDiagramGenerator.getOperationColor("U"));
        g2d.fill(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
        
        // Dibujar contorno
        g2d.setColor(VennDiagramGenerator.getCircleBorder());
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
        
        // Dibujar etiqueta
        g2d.setColor(Color.BLACK);
        g2d.setFont(labelFont);
        String setName = diagramData.getOperands().get(0);
        g2d.drawString(setName, centerX - 10, centerY - radius - 10);
    }
    
    /**
     * Dibuja una operacion de complemento.
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawComplementOperation(Graphics2D g2d) {
        drawUniverse(g2d);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = VennDiagramGenerator.getCircleRadius();
        
        // Crear área del universo
        Area universeArea = new Area(new Rectangle2D.Double(VennDiagramGenerator.getMargin(), 
                                                           30, 
                                                           getWidth() - 2 * VennDiagramGenerator.getMargin(), 
                                                           getHeight() - 80));
        
        // Crear área del conjunto
        Area setArea = new Area(new Ellipse2D.Double(centerX - radius, centerY - radius, 
                                                    radius * 2, radius * 2));
        
        // Restar el conjunto del universo para obtener el complemento
        universeArea.subtract(setArea);
        
        // Dibujar complemento
        g2d.setColor(VennDiagramGenerator.getOperationColor("^"));
        g2d.fill(universeArea);
        
        // Dibujar contorno del conjunto
        g2d.setColor(VennDiagramGenerator.getCircleBorder());
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
        
        // Dibujar etiqueta
        g2d.setColor(Color.BLACK);
        g2d.setFont(labelFont);
        String setName = diagramData.getOperands().get(0);
        g2d.drawString("^" + setName, centerX - 15, centerY - radius - 10);
    }
    
    /**
     * Dibuja el universo como fondo.
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawUniverse(Graphics2D g2d) {
        g2d.setColor(VennDiagramGenerator.getUniverseColor());
        g2d.fill(new Rectangle2D.Double(VennDiagramGenerator.getMargin(), 
                                       30, 
                                       getWidth() - 2 * VennDiagramGenerator.getMargin(), 
                                       getHeight() - 80));
        
        // Dibujar borde del universo
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(new Rectangle2D.Double(VennDiagramGenerator.getMargin(), 
                                       30, 
                                       getWidth() - 2 * VennDiagramGenerator.getMargin(), 
                                       getHeight() - 80));
        
        // Etiqueta del universo
        g2d.setFont(labelFont);
        g2d.drawString("U", VennDiagramGenerator.getMargin() + 5, 45);
    }
    
    /**
     * Dibuja las etiquetas de los conjuntos.
     * 
     * @param g2d Contexto grafico 2D
     * @param setA Nombre del conjunto A
     * @param setB Nombre del conjunto B
     * @param circleAX Posicion X del círculo A
     * @param circleAY Posicion Y del círculo A
     * @param circleBX Posicion X del círculo B
     * @param circleBY Posicion Y del círculo B
     * @param radius Radio de los circulos
     */
    private void drawSetLabels(Graphics2D g2d, String setA, String setB, 
                              int circleAX, int circleAY, int circleBX, int circleBY, int radius) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(labelFont);
        
        // Etiqueta del conjunto A
        g2d.drawString(setA, circleAX - radius - 20, circleAY - radius - 10);
        
        // Etiqueta del conjunto B
        g2d.drawString(setB, circleBX + radius + 5, circleBY - radius - 10);
    }
    
    /**
     * Dibuja los elementos en las regiones apropiadas.
     * 
     * @param g2d Contexto grafico 2D
     * @param setA Nombre del conjunto A
     * @param setB Nombre del conjunto B
     * @param circleAX Posicion X del círculo A
     * @param circleAY Posicion Y del círculo A
     * @param circleBX Posicion X del círculo B
     * @param circleBY Posicion Y del círculo B
     * @param radius Radio de los circulos
     */
    private void drawSetElements(Graphics2D g2d, String setA, String setB, 
                                int circleAX, int circleAY, int circleBX, int circleBY, int radius) {
        Map<String, Set<Object>> referencedSets = diagramData.getReferencedSets();
        
        if (!referencedSets.containsKey(setA) || !referencedSets.containsKey(setB)) {
            return;
        }
        
        Set<Object> elementsA = referencedSets.get(setA);
        Set<Object> elementsB = referencedSets.get(setB);
        
        // Encontrar elementos unicos y comunes
        Set<Object> onlyA = new HashSet<>(elementsA);
        onlyA.removeAll(elementsB);
        
        Set<Object> onlyB = new HashSet<>(elementsB);
        onlyB.removeAll(elementsA);
        
        Set<Object> intersection = new HashSet<>(elementsA);
        intersection.retainAll(elementsB);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(elementFont);
        
        // Dibujar elementos unicos de A
        drawElementsInRegion(g2d, onlyA, circleAX - radius/2, circleAY);
        
        // Dibujar elementos unicos de B
        drawElementsInRegion(g2d, onlyB, circleBX + radius/2, circleBY);
        
        // Dibujar elementos de la interseccion
        drawElementsInRegion(g2d, intersection, (circleAX + circleBX) / 2, circleAY);
    }
    
    /**
     * Dibuja elementos en una region especifica.
     * 
     * @param g2d Contexto grafico 2D
     * @param elements Elementos a dibujar
     * @param x Posicion X base
     * @param y Posicion Y base
     */
    private void drawElementsInRegion(Graphics2D g2d, Set<Object> elements, int x, int y) {
        int elementY = y - 20;
        int elementX = x - 20;
        int count = 0;
        
        for (Object element : elements) {
            if (count >= 8) { // Limitar numero de elementos mostrados
                g2d.drawString("...", elementX, elementY);
                break;
            }
            
            String elementStr = formatElement(element);
            if (elementStr.length() > 3) {
                elementStr = elementStr.substring(0, 3) + "...";
            }
            
            g2d.drawString(elementStr, elementX, elementY);
            elementY += 12;
            
            if (count == 3) { // Nueva columna después de 4 elementos
                elementY = y - 20;
                elementX += 40;
            }
            count++;
        }
    }
    
    /**
     * Dibuja informacion adicional sobre la operacion.
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawOperationInfo(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(elementFont);
        
        // Mostrar el resultado
        String resultText = "Resultado: " + Environment.formatSet(diagramData.getResult());
        if (resultText.length() > 50) {
            resultText = resultText.substring(0, 50) + "...";
        }
        
        g2d.drawString(resultText, 10, getHeight() - 10);
    }
    
    /**
     * Dibuja mensaje cuando no hay datos validos.
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawNoDataMessage(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(labelFont);
        String message = "No hay datos validos para mostrar";
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        g2d.drawString(message, (getWidth() - messageWidth) / 2, getHeight() / 2);
    }
    
    /**
     * Dibuja mensaje para operaciones no soportadas.
     * 
     * @param g2d Contexto grafico 2D
     */
    private void drawUnsupportedMessage(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.setFont(labelFont);
        String message = "⚠️ operacion no soportada para diagramas de Venn ⚠️";
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        g2d.drawString(message, (getWidth() - messageWidth) / 2, getHeight() / 2);
    }
    
    /**
     * Formatea un elemento para su visualizacion.
     * 
     * @param element Elemento a formatear
     * @return Cadena formateada del elemento
     */
    private String formatElement(Object element) {
        if (element instanceof Character) {
            return "'" + element + "'";
        }
        return element.toString();
    }
}
