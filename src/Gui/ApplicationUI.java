/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import javax.imageio.ImageIO;
import java.util.Arrays;
import Project.Project;
import Graphics.ImageDiagramManager;
import Reports.reports;
import Utils.Token;
import Utils.ErrorHandler;
import javax.swing.JTextPane;

/**
 *Interfaz grafica principal de la aplicacion
 * Esta clase maneja la creacion, edicion y visualizacion de archivos,
 * así como la generacion de reportes y diagramas.
 * @author danie
 */
public class ApplicationUI extends javax.swing.JFrame {
    private static ApplicationUI instance; 
    private File currentFile;
    private List<Token> tokens = new ArrayList<>();
    
    private List<File> imageFiles = new ArrayList<>();
    private int currentImageIndex = 0;
    private JLabel imageLabel;
    private JPanel imagePanel;
    private double currentZoom = 1.1;
    private BufferedImage currentOriginalImage;
    
    private SyntaxHighlightedEditor syntaxEditor;

    public ApplicationUI() {
        initComponents();
        instance = this;
        this.currentFile = null;
        txtConsola.setEditable(false);
        txtConsola.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        txtConsola.setForeground(Color.WHITE);
        clearTable(); 
        initImageViewer(); 
        initSyntaxEditor();
    }

    public static JTextPane getTxtConsole() {
        if (instance != null) {
            return instance.txtConsola;
        } else {
            System.out.println("Error: La instancia de interfazG no esta disponible.");
            return null;
        }
    }

    private void openNewFile() {
        JFileChooser fileChooser = createCustomFileChooser("Abrir Archivo", ".ca", "Archivos .ca");

        int userSelection = fileChooser.showOpenDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            clearEditor();
            txtConsola.setText("");
            clearTable();
            readFile(fileToOpen); 
            currentFile = fileToOpen; 
        }
    }

    private void readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            setEditorText(content.toString());
        } catch (IOException ex) {
            displayErrorMessage("Error al abrir el archivo: " + ex.getMessage(), "Error de Archivo");
        }
    }

    private void saveFile() {
        if (currentFile != null) {
            writeToFile(currentFile);
        } else {
            saveAs();
        }
    }

    private void saveAs() {
        JFileChooser fileChooser = createCustomFileChooser("Guardar Como", ".ca", "Archivos .ca");

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".ca")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".ca");
            }
            writeToFile(fileToSave);
            currentFile = fileToSave;
        }
    }

    private void writeToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(getEditorText());
            displaySuccessMessage("Archivo guardado exitosamente.", "Archivo Guardado");
            txtConsola.setText("");
        } catch (IOException ex) {
            displayErrorMessage("Error al guardar el archivo: " + ex.getMessage(), "Error de Guardado");
        }
    }
    
    

    private void generateErrorReport() {
        if (ErrorHandler.erroresLexicos.isEmpty() && ErrorHandler.erroresSintacticos.isEmpty()) {
            showWarningMessage(
                "⚠️ No hay errores para mostrar.\n" +
                "📋 El análisis no ha detectado errores en el código.",
                "Aviso");
            return;
        }
        
        try {
            String archivoHTML = reports.generateErrorReportHTML();
            
            if (archivoHTML != null) {
                java.awt.Desktop.getDesktop().open(new java.io.File(archivoHTML));
                
                displaySuccessMessage(
                    "✅ Reporte de errores generado exitosamente!\n" +
                    "📄 Archivo: " + archivoHTML + "\n" +
                    "🌐 El reporte se abrió en tu navegador.",
                    "Reporte Generado");
            } else {
                displayErrorMessage(
                    "❌ Error generando el reporte de errores.",
                    "Error");
            }
        } catch (Exception e) {
            displayErrorMessage(
                "❌ Error abriendo el reporte: " + e.getMessage(),
                "Error");
        }
    }
    
    private void analyzeText() {
        ErrorHandler.ResetError();
        String input = getEditorText(); 
        if (input.trim().isEmpty()) { 
            txtConsola.setText("No hay texto para analizar.");
            return;
        }

        txtConsola.setText("");  
        tokens.clear();
        clearTable();  

        try {
            ImageDiagramManager imageManager = ImageDiagramManager.getInstance();
            imageManager.deleteAllGeneratedImages();
            
            tokens = Project.parseInput(input, txtConsola); 
            clearTable(); 
            
            updateImageViewer();
            
        } catch (IOException e) {
            txtConsola.setText("Error al procesar el texto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearTable() {
        DefaultTableModel model = (DefaultTableModel) tableReporte.getModel();
        model.setRowCount(0); 
    }

    private void openTokenReport() {
        if (tokens.isEmpty()) {
            showWarningMessage("No hay tokens para mostrar.", "Aviso");
        } else {
            try {
                String archivoHTML = reports.createTokenReportHTML(tokens);
                
                if (archivoHTML != null) {
                    java.awt.Desktop.getDesktop().open(new java.io.File(archivoHTML));
                    
                    displaySuccessMessage(
                        "✅ Reporte de tokens generado exitosamente!\n" +
                        "📄 Archivo: " + archivoHTML + "\n" +
                        "🌐 El reporte se abrió en tu navegador.",
                        "Reporte Generado");
                } else {
                    displayErrorMessage(
                        "❌ Error generando el reporte de tokens.",
                        "Error");
                }
            } catch (Exception e) {
                displayErrorMessage(
                    "❌ Error abriendo el reporte: " + e.getMessage(),
                    "Error");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // MetodoS PARA DIAGRAMAS DE VENN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private void generateDiagramsAsPNG() {
        ImageDiagramManager imageManager = ImageDiagramManager.getInstance();
        imageManager.setParentFrame(this);
        
        String input = getEditorText();
        if (input.trim().isEmpty()) {
            showWarningMessage(
                "⚠️ No hay código para analizar. Por favor ingrese código primero.",
                "Aviso");
            return;
        }
        
        try {
            imageManager.deleteAllGeneratedImages();
            tokens = Project.parseInput(input, txtConsola);
            updateImageViewer();
            
            displaySuccessMessage(
                "✅ Diagramas JPG generados exitosamente!\n" +
                "📂 Revisa la carpeta 'diagramas_venn' o usa el visor integrado.",
                "Diagramas de Venn");
                
        } catch (Exception e) {
            displayErrorMessage(
                "❌ Error generando diagramas: " + e.getMessage(),
                "Error");
        }
    }
    
    /**
     * Abre el directorio donde se guardan los diagramas
     */
    private void openDiagramsDirectory() {
        ImageDiagramManager imageManager = ImageDiagramManager.getInstance();
        
        try {
            imageManager.openOutputDirectory();
            txtConsola.setText(txtConsola.getText() + 
                "\n📂 Directorio de diagramas abierto: " + imageManager.getOutputDirectory() + "\n");
        } catch (Exception e) {
            displayErrorMessage(
                "❌ Error abriendo directorio: " + e.getMessage(),
                "Error");
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // MetodoS PARA EL VISOR DE imagenes
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private void initImageViewer() {
        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(51,51, 51));
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(new Color(51,51, 51));
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        
        JScrollPane imageScrollPane = new JScrollPane(imageContainer);
        imageScrollPane.setBackground(new Color(51,51, 51));
        imageScrollPane.getViewport().setBackground(new Color(51,51, 51));
        imageScrollPane.setBorder(null);
        imageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel zoomControls = createZoomControls();
        
        imagePanel.add(imageScrollPane, BorderLayout.CENTER);
        imagePanel.add(zoomControls, BorderLayout.SOUTH);

        jScrollPane4.setViewportView(imagePanel);
        
        loadAvailableImages();
        refreshImageDisplay();
        updateDownloadButtonState();
    }
    
    private JPanel createZoomControls() {
        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        zoomPanel.setBackground(new Color(51, 51, 51));
        
        JButton btnZoomOut = new JButton("🔍-");
        btnZoomOut.setBackground(new Color(70, 70, 70));
        btnZoomOut.setForeground(Color.WHITE);
        btnZoomOut.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnZoomOut.setFocusPainted(false);
        btnZoomOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnZoomOut.addActionListener(e -> zoomOut());
        
        JButton btnZoom100 = new JButton("100%");
        btnZoom100.setBackground(new Color(70, 70, 70));
        btnZoom100.setForeground(Color.WHITE);
        btnZoom100.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnZoom100.setFocusPainted(false);
        btnZoom100.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnZoom100.addActionListener(e -> zoomToActualSize());
        
        JButton btnZoomIn = new JButton("🔍+");
        btnZoomIn.setBackground(new Color(70, 70, 70));
        btnZoomIn.setForeground(Color.WHITE);
        btnZoomIn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnZoomIn.setFocusPainted(false);
        btnZoomIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnZoomIn.addActionListener(e -> zoomIn());
        
        JButton btnFitWindow = new JButton("📏");
        btnFitWindow.setBackground(new Color(70, 70, 70));
        btnFitWindow.setForeground(Color.WHITE);
        btnFitWindow.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnFitWindow.setFocusPainted(false);
        btnFitWindow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFitWindow.setToolTipText("Ajustar a ventana");
        btnFitWindow.addActionListener(e -> fitToWindow());
        
        zoomPanel.add(btnZoomOut);
        zoomPanel.add(btnZoom100);
        zoomPanel.add(btnZoomIn);
        zoomPanel.add(btnFitWindow);
        
        return zoomPanel;
    }
    
    private void loadAvailableImages() {
        imageFiles.clear();
        
        File diagramsDir = new File("diagramas_venn");
        
        if (diagramsDir.exists() && diagramsDir.isDirectory()) {
            File[] files = diagramsDir.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || 
                       lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif");
            });
            
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
                
                for (File file : files) {
                    imageFiles.add(file);
                }
            }
        }
        
        currentImageIndex = 0;
    }
    
    private void refreshImageDisplay() {
        if (imageFiles.isEmpty()) {
            displayNoImagesMessage();
            btnPrevious.setEnabled(false);
            btnNext.setEnabled(false);
        } else {
            displayCurrentImage();
            updateButtonStates();
        }
    }
    
    private void displayNoImagesMessage() {
        String mensaje = "<html><div style='text-align: center; font-family: SansSerif; font-size: 16px; color: #666;'>" +
                        "<br><br><br><br>" +
                        "📊 No hay diagramas disponibles<br><br>" +
                        "Por favor ejecute el programa para generar diagramas" +
                        "<br><br><br><br>" +
                        "</div></html>";
        imageLabel.setText(mensaje);
        imageLabel.setIcon(null);
        currentOriginalImage = null;
        
        updateDownloadButtonState();
    }
    
    private void displayCurrentImage() {
        if (currentImageIndex >= 0 && currentImageIndex < imageFiles.size()) {
            try {
                File imageFile = imageFiles.get(currentImageIndex);
                BufferedImage originalImage = ImageIO.read(imageFile);
                
                if (originalImage != null) {
                    currentOriginalImage = originalImage;
                    fitToWindow();
                    
                    String info = String.format("📊 %s (%dx%d px)", 
                        imageFile.getName(), 
                        originalImage.getWidth(), 
                        originalImage.getHeight());
                    imageLabel.setToolTipText(info);
                    
                } else {
                    imageLabel.setText("❌ Error cargando imagen");
                    imageLabel.setIcon(null);
                    currentOriginalImage = null;
                }
            } catch (IOException e) {
                imageLabel.setText("❌ Error leyendo imagen: " + e.getMessage());
                imageLabel.setIcon(null);
                currentOriginalImage = null;
            }
        }
        
        updateDownloadButtonState();
    }
    
    private void zoomIn() {
        if (currentOriginalImage != null) {
            currentZoom = Math.min(currentZoom * 1.25, 5.0);
            updateImageDisplay();
        }
    }
    
    private void zoomOut() {
        if (currentOriginalImage != null) {
            currentZoom = Math.max(currentZoom / 1.25, 0.1);
            updateImageDisplay();
        }
    }
    
    private void zoomToActualSize() {
        if (currentOriginalImage != null) {
            currentZoom = 1.0;
            updateImageDisplay();
        }
    }
    
    private void fitToWindow() {
        if (currentOriginalImage != null) {
            int availableWidth = jScrollPane4.getWidth() - 60;
            int availableHeight = jScrollPane4.getHeight() - 120;
            
            if (availableWidth <= 100) availableWidth = 600;
            if (availableHeight <= 100) availableHeight = 400;
            
            double scaleX = (double) availableWidth / currentOriginalImage.getWidth();
            double scaleY = (double) availableHeight / currentOriginalImage.getHeight();
            currentZoom = Math.min(scaleX, scaleY);
            
            currentZoom = Math.max(0.1, Math.min(currentZoom, 3.0));
            
            updateImageDisplay();
        }
    }
    
    private void updateImageDisplay() {
        if (currentOriginalImage != null) {
            ImageIcon imageIcon = createZoomedImageIcon(currentOriginalImage, currentZoom);
            imageLabel.setIcon(imageIcon);
            imageLabel.setText(null);
        }
    }

    private ImageIcon createZoomedImageIcon(BufferedImage original, double zoom) {
        if (zoom == 1.0) {
            return new ImageIcon(original);
        }
        
        int newWidth = (int) Math.round(original.getWidth() * zoom);
        int newHeight = (int) Math.round(original.getHeight() * zoom);
        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return new ImageIcon(scaledImage);
    }
    
    private void updateButtonStates() {
        btnPrevious.setEnabled(currentImageIndex > 0);
        btnNext.setEnabled(currentImageIndex < imageFiles.size() - 1);
        updateDownloadButtonState();
    }

    private void updateDownloadButtonState() {
        boolean hasActiveImage = currentOriginalImage != null && 
                               !imageFiles.isEmpty() && 
                               currentImageIndex >= 0 && 
                               currentImageIndex < imageFiles.size();
        
        if (btnDownloand != null) {
            btnDownloand.setEnabled(hasActiveImage);
            
            if (hasActiveImage) {
                btnDownloand.setToolTipText("💾 Descargar imagen actual");
            } else {
                btnDownloand.setToolTipText("❌ No hay imagen activa para descargar");
            }
        }
    }
    
    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            displayCurrentImage();
            updateButtonStates();
        }
    }

    private void showNextImage() {
        if (currentImageIndex < imageFiles.size() - 1) {
            currentImageIndex++;
            displayCurrentImage();
            updateButtonStates();
        }
    }

    /**
     * Actualiza el visor cargando nuevamente las imagenes disponibles
     */
    public void updateImageViewer() {
        loadAvailableImages();
        refreshImageDisplay();
        updateDownloadButtonState();
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // MetodoS PARA EL EDITOR CON RESALTADO DE SINTAXIS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private void initSyntaxEditor() {
        syntaxEditor = new SyntaxHighlightedEditor();
        jScrollPane2.setViewportView(syntaxEditor);
        
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane2.getHorizontalScrollBar().setUnitIncrement(16);
        jScrollPane2.setWheelScrollingEnabled(true);
        
        syntaxEditor.setBackground(new Color(51, 51, 51));
        syntaxEditor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        syntaxEditor.setFocusable(true);
        syntaxEditor.setRequestFocusEnabled(false);
        
        SwingUtilities.invokeLater(() -> {
            if (syntaxEditor.getText().trim().isEmpty()) {
                syntaxEditor.setText("");
            }
        });
    }
    
    private String getEditorText() {
        if (syntaxEditor != null) {
            return syntaxEditor.getText();
        }
        return textArea.getText();
    }
    
    private void setEditorText(String text) {
        if (syntaxEditor != null) {
            syntaxEditor.setText(text);
        } else {
            textArea.setText(text);
        }
    }
    
    private void clearEditor() {
        if (syntaxEditor != null) {
            syntaxEditor.setText("");
        } else {
            textArea.setText("");
        }
    }
    
    private void clearEditorAndConsole() {
        clearEditor();
        txtConsola.setText("");
    }
    private void openJsonReport() {
        File jsonFolder = new File("JSON");
        if (!jsonFolder.exists()) {
            showWarningMessage(
                "⚠️ No se encontró la carpeta JSON.\n" +
                "📋 Ejecute el programa primero para generar el reporte JSON.",
                "Carpeta JSON no encontrada");
            return;
        }
        
        File jsonFile = new File("JSON/simplificaciones.json");
        if (!jsonFile.exists()) {
            showWarningMessage(
                "⚠️ No se encontró el archivo simplificaciones.json.\n" +
                "📋 Ejecute el programa primero para generar las simplificaciones.",
                "Archivo JSON no encontrado");
            return;
        }
        
        try {
            if (jsonFile.length() == 0) {
                showWarningMessage(
                    "⚠️ El archivo JSON esta vacío.\n" +
                    "📋 No hay simplificaciones para mostrar.",
                    "Archivo JSON vacío");
                return;
            }
            
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(jsonFile);
                    
                    displaySuccessMessage(
                        "✅ Reporte JSON abierto exitosamente!\n" +
                        "📄 Archivo: " + jsonFile.getAbsolutePath() + "\n" +
                        "💾 Tamaño: " + formatFileSize(jsonFile.length()) + "\n" +
                        "🔧 Se abrió con el programa predeterminado del sistema.",
                        "Reporte JSON");
                } else {
                    displayErrorMessage(
                        "❌ El sistema no soporta abrir archivos automáticamente.\n" +
                        "📍 Ubicación del archivo: " + jsonFile.getAbsolutePath(),
                        "Error del Sistema");
                }
            } else {
                displayErrorMessage(
                    "❌ Desktop no esta soportado en este sistema.\n" +
                    "📍 Ubicación del archivo: " + jsonFile.getAbsolutePath(),
                    "Error del Sistema");
            }
            
        } catch (IOException e) {
            displayErrorMessage(
                "❌ Error abriendo el archivo JSON: " + e.getMessage() + "\n" +
                "📍 Ubicación: " + jsonFile.getAbsolutePath(),
                "Error de Archivo");
        } catch (Exception e) {
            displayErrorMessage(
                "❌ Error inesperado: " + e.getMessage(),
                "Error");
        }
    }
    
    /**
     * Formatea el tamaño de archivo en una cadena legible
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }

    /**
     * Descarga la imagen actual del visor
     */
    private void downloadImage() {
        if (currentOriginalImage == null || imageFiles.isEmpty() || currentImageIndex < 0) {
            showWarningMessage(
                "⚠️ No hay imagen activa para descargar.\n" +
                "📊 Por favor, genere diagramas primero y seleccione una imagen.",
                "Sin imagen activa");
            return;
        }
        
        try {
            File currentImageFile = imageFiles.get(currentImageIndex);
            String originalName = currentImageFile.getName();
            String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            
            JFileChooser fileChooser = createCustomFileChooser("Guardar Imagen", extension, "Imágenes " + extension.toUpperCase());
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            
            String suggestedName = baseName + "_descarga" + extension;
            fileChooser.setSelectedFile(new File(suggestedName));
            
            javax.swing.filechooser.FileNameExtensionFilter filter = 
                new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imágenes (" + extension + ")", extension.substring(1));
            fileChooser.setFileFilter(filter);
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File destinationFile = fileChooser.getSelectedFile();
                
                if (!destinationFile.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    destinationFile = new File(destinationFile.getAbsolutePath() + extension);
                }
                
                if (destinationFile.exists()) {
                    int overwrite = javax.swing.JOptionPane.showConfirmDialog(
                        this,
                        "📄 El archivo ya existe. ¿Desea sobrescribirlo?\n\n" +
                        "📍 Archivo: " + destinationFile.getName(),
                        "Confirmar sobrescritura",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (overwrite != javax.swing.JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                java.nio.file.Files.copy(
                    currentImageFile.toPath(), 
                    destinationFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                
                String fileSize = formatFileSize(destinationFile.length());
                displaySuccessMessage(
                    "✅ Imagen descargada exitosamente!\n\n" +
                    "📄 Archivo: " + destinationFile.getName() + "\n" +
                    "📍 Ubicación: " + destinationFile.getParent() + "\n" +
                    "📏 Tamaño: " + fileSize + "\n" +
                    "🖼️ Resolución: " + currentOriginalImage.getWidth() + "x" + currentOriginalImage.getHeight() + " píxeles",
                    "Descarga Completada");
            }
            
        } catch (Exception e) {
            displayErrorMessage(
                "❌ Error descargando la imagen: " + e.getMessage() + "\n\n" +
                "💡 Verifique que tenga permisos de escritura en la ubicación seleccionada.",
                "Error de Descarga");
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
 
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtConsola = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableReporte = new javax.swing.JTable();
        btnHelp = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnDownloand = new javax.swing.JButton();
        jMenuBar2 = new javax.swing.JMenuBar();
        archivos = new javax.swing.JMenu();
        newArchivo = new javax.swing.JMenuItem();
        guardar = new javax.swing.JMenuItem();
        guardarComo = new javax.swing.JMenuItem();
        limpiarConsola = new javax.swing.JMenuItem();
        ejecutar = new javax.swing.JMenu();
        ejecutarP = new javax.swing.JMenuItem();
        reporte1 = new javax.swing.JMenu();
        R_Tokens = new javax.swing.JMenuItem();
        R_Errores = new javax.swing.JMenuItem();
        R_Json = new javax.swing.JMenuItem();
        reporte = new javax.swing.JMenu();
        generarDiagramas = new javax.swing.JMenuItem();
        actualizarVisor = new javax.swing.JMenuItem();
        abrirDirectorio = new javax.swing.JMenuItem();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jDesktopPane1Layout.setVerticalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(51, 51, 51));

        jLabel1.setText("jLabel1");

        jPanel2.setBackground(new java.awt.Color(30, 30, 46));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        jPanel2.setForeground(new java.awt.Color(30, 30, 46));
        jPanel2.setFocusCycleRoot(true);

        txtConsola.setBackground(new java.awt.Color(51, 51, 51));
        txtConsola.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        txtConsola.setForeground(new java.awt.Color(255, 255, 255));
        txtConsola.setCaretColor(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(txtConsola);

        textArea.setBackground(new java.awt.Color(51, 51, 51));
        textArea.setColumns(20);
        textArea.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        textArea.setRows(5);
        textArea.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        jScrollPane2.setViewportView(textArea);

        jLabel4.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("IMAGENES");

        jLabel5.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("CONSOLA");

        jLabel6.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("ENTRADA");

        jScrollPane4.setBackground(new java.awt.Color(51, 51, 51));
        jScrollPane4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        jScrollPane4.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane4.setOpaque(false);

        tableReporte.setBackground(new java.awt.Color(204, 255, 204));
        tableReporte.setFont(new java.awt.Font("Sans Serif Collection", 1, 14)); // NOI18N
        tableReporte.setForeground(new java.awt.Color(204, 255, 204));
        tableReporte.setSelectionBackground(new java.awt.Color(0, 204, 102));
        tableReporte.setSelectionForeground(new java.awt.Color(0, 153, 102));
        tableReporte.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tableReporteFocusGained(evt);
            }
        });
        jScrollPane4.setViewportView(tableReporte);

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/sign24.png"))); // NOI18N
        btnHelp.setBorderPainted(false);
        btnHelp.setContentAreaFilled(false);
        btnHelp.setFocusable(false);
        btnHelp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnHelp.setMaximumSize(new java.awt.Dimension(40, 40));
        btnHelp.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/sign24.png"))); // NOI18N
        btnHelp.setRolloverEnabled(true);
        btnHelp.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/sign32.png"))); // NOI18N
        btnHelp.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/sign32.png"))); // NOI18N
        btnHelp.setSelected(true);
        btnHelp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpActionPerformed(evt);
            }
        });

        btnNext.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/right-arrow.png"))); // NOI18N
        btnNext.setText("SIGUIENTE");
        btnNext.setBorderPainted(false);
        btnNext.setContentAreaFilled(false);
        btnNext.setFocusable(false);
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setMaximumSize(new java.awt.Dimension(40, 40));
        btnNext.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/right-arrow.png"))); // NOI18N
        btnNext.setRolloverEnabled(true);
        btnNext.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/right-arrow32.png"))); // NOI18N
        btnNext.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/right-arrow32.png"))); // NOI18N
        btnNext.setSelected(true);
        btnNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnPrevious.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/left-arrow.png"))); // NOI18N
        btnPrevious.setText("ANTERIOR");
        btnPrevious.setBorderPainted(false);
        btnPrevious.setContentAreaFilled(false);
        btnPrevious.setFocusable(false);
        btnPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrevious.setMaximumSize(new java.awt.Dimension(40, 40));
        btnPrevious.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/left-arrow.png"))); // NOI18N
        btnPrevious.setRolloverEnabled(true);
        btnPrevious.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/left-arrow32.png"))); // NOI18N
        btnPrevious.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/left-arrow32.png"))); // NOI18N
        btnPrevious.setSelected(true);
        btnPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }
        });

        btnDownloand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/photo24.png"))); // NOI18N
        btnDownloand.setBorderPainted(false);
        btnDownloand.setContentAreaFilled(false);
        btnDownloand.setFocusable(false);
        btnDownloand.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDownloand.setMaximumSize(new java.awt.Dimension(40, 40));
        btnDownloand.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/photo24.png"))); // NOI18N
        btnDownloand.setRolloverEnabled(true);
        btnDownloand.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/photo32.png"))); // NOI18N
        btnDownloand.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/photo32.png"))); // NOI18N
        btnDownloand.setSelected(true);
        btnDownloand.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDownloand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownloandActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel6))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(454, 454, 454))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addComponent(btnDownloand, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(112, 112, 112)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(134, 134, 134)
                .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(160, 160, 160))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnDownloand, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnPrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnNext, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMenuBar2.setBackground(new java.awt.Color(30, 30, 46));
        jMenuBar2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jMenuBar2.setForeground(new java.awt.Color(51, 51, 51));
        jMenuBar2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jMenuBar2.setFont(new java.awt.Font("Tempus Sans ITC", 1, 14)); // NOI18N

        archivos.setBackground(new java.awt.Color(0, 0, 0));
        archivos.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        archivos.setForeground(new java.awt.Color(255, 255, 255));
        archivos.setText("ARCHIVOS");
        archivos.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 16)); // NOI18N
        archivos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                archivosActionPerformed(evt);
            }
        });

        newArchivo.setBackground(new java.awt.Color(0, 51, 102));
        newArchivo.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        newArchivo.setForeground(new java.awt.Color(255, 255, 255));
        newArchivo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/folder.png"))); // NOI18N
        newArchivo.setText("Abrir Archivo");
        newArchivo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        newArchivo.setRequestFocusEnabled(false);
        newArchivo.setVerifyInputWhenFocusTarget(false);
        newArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newArchivoActionPerformed(evt);
            }
        });
        archivos.add(newArchivo);

        guardar.setBackground(new java.awt.Color(0, 51, 102));
        guardar.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        guardar.setForeground(new java.awt.Color(255, 255, 255));
        guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/save.png"))); // NOI18N
        guardar.setText("Guardar");
        guardar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarActionPerformed(evt);
            }
        });
        archivos.add(guardar);

        guardarComo.setBackground(new java.awt.Color(0, 51, 102));
        guardarComo.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        guardarComo.setForeground(new java.awt.Color(255, 255, 255));
        guardarComo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/data-storage.png"))); // NOI18N
        guardarComo.setText("Guardar Como");
        guardarComo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        guardarComo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarComoActionPerformed(evt);
            }
        });
        archivos.add(guardarComo);

        limpiarConsola.setBackground(new java.awt.Color(0, 51, 102));
        limpiarConsola.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        limpiarConsola.setForeground(new java.awt.Color(255, 255, 255));
        limpiarConsola.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/data-cleaning.png"))); // NOI18N
        limpiarConsola.setText("Limpiar ");
        limpiarConsola.setToolTipText("");
        limpiarConsola.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        limpiarConsola.setRequestFocusEnabled(false);
        limpiarConsola.setVerifyInputWhenFocusTarget(false);
        limpiarConsola.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpiarConsolaActionPerformed(evt);
            }
        });
        archivos.add(limpiarConsola);

        jMenuBar2.add(archivos);

        ejecutar.setBackground(new java.awt.Color(0, 0, 0));
        ejecutar.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        ejecutar.setForeground(new java.awt.Color(255, 255, 255));
        ejecutar.setText("EJECUTAR");
        ejecutar.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 16)); // NOI18N

        ejecutarP.setBackground(new java.awt.Color(0, 51, 102));
        ejecutarP.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        ejecutarP.setForeground(new java.awt.Color(255, 255, 255));
        ejecutarP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/browser.png"))); // NOI18N
        ejecutarP.setText("Ejecutar");
        ejecutarP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ejecutarP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejecutarPActionPerformed(evt);
            }
        });
        ejecutar.add(ejecutarP);

        jMenuBar2.add(ejecutar);

        reporte1.setBackground(new java.awt.Color(0, 0, 0));
        reporte1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        reporte1.setForeground(new java.awt.Color(255, 255, 255));
        reporte1.setText("REPORTES");
        reporte1.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 16)); // NOI18N

        R_Tokens.setBackground(new java.awt.Color(0, 51, 102));
        R_Tokens.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        R_Tokens.setForeground(new java.awt.Color(255, 255, 255));
        R_Tokens.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/table.png"))); // NOI18N
        R_Tokens.setText("Tokens");
        R_Tokens.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        R_Tokens.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R_TokensActionPerformed(evt);
            }
        });
        reporte1.add(R_Tokens);

        R_Errores.setBackground(new java.awt.Color(0, 51, 102));
        R_Errores.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        R_Errores.setForeground(new java.awt.Color(255, 255, 255));
        R_Errores.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/clipboard.png"))); // NOI18N
        R_Errores.setText("Errores");
        R_Errores.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        R_Errores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R_ErroresActionPerformed(evt);
            }
        });
        reporte1.add(R_Errores);

        R_Json.setBackground(new java.awt.Color(0, 51, 102));
        R_Json.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        R_Json.setForeground(new java.awt.Color(255, 255, 255));
        R_Json.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/json.png"))); // NOI18N
        R_Json.setText("Json");
        R_Json.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        R_Json.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R_JsonActionPerformed(evt);
            }
        });
        reporte1.add(R_Json);

        jMenuBar2.add(reporte1);

        reporte.setBackground(new java.awt.Color(0, 0, 0));
        reporte.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.lightGray, java.awt.Color.lightGray));
        reporte.setForeground(new java.awt.Color(255, 255, 255));
        reporte.setText("DIAGRAMAS");
        reporte.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 16)); // NOI18N

        generarDiagramas.setBackground(new java.awt.Color(0, 51, 102));
        generarDiagramas.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        generarDiagramas.setForeground(new java.awt.Color(255, 255, 255));
        generarDiagramas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/generative-image.png"))); // NOI18N
        generarDiagramas.setText("Generar Diagrama");
        generarDiagramas.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        generarDiagramas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generarDiagramasActionPerformed(evt);
            }
        });
        reporte.add(generarDiagramas);

        actualizarVisor.setBackground(new java.awt.Color(0, 51, 102));
        actualizarVisor.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        actualizarVisor.setForeground(new java.awt.Color(255, 255, 255));
        actualizarVisor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/image-.png"))); // NOI18N
        actualizarVisor.setText("Actualizar Visor ");
        actualizarVisor.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        actualizarVisor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarVisorActionPerformed(evt);
            }
        });
        reporte.add(actualizarVisor);

        abrirDirectorio.setBackground(new java.awt.Color(0, 51, 102));
        abrirDirectorio.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        abrirDirectorio.setForeground(new java.awt.Color(255, 255, 255));
        abrirDirectorio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/folderImage.png"))); // NOI18N
        abrirDirectorio.setText("Abrir Ruta");
        abrirDirectorio.setToolTipText("");
        abrirDirectorio.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        abrirDirectorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abrirDirectorioActionPerformed(evt);
            }
        });
        reporte.add(abrirDirectorio);

        jMenuBar2.add(reporte);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ejecutarPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejecutarPActionPerformed
        analyzeText();
    }//GEN-LAST:event_ejecutarPActionPerformed

    private void R_ErroresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_R_ErroresActionPerformed
        generateErrorReport();
    }//GEN-LAST:event_R_ErroresActionPerformed

    private void R_TokensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_R_TokensActionPerformed
        openTokenReport();
    }//GEN-LAST:event_R_TokensActionPerformed

    private void guardarComoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarComoActionPerformed
        saveAs();
    }//GEN-LAST:event_guardarComoActionPerformed

    private void guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed
        saveFile();
    }//GEN-LAST:event_guardarActionPerformed

    private void newArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newArchivoActionPerformed
        openNewFile();
    }//GEN-LAST:event_newArchivoActionPerformed

    private void tableReporteFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tableReporteFocusGained
        //YA NO SE USA.
    }//GEN-LAST:event_tableReporteFocusGained

    private void generarDiagramasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generarDiagramasActionPerformed
        // Generar diagramas PNG manualmente
        generateDiagramsAsPNG();
    }//GEN-LAST:event_generarDiagramasActionPerformed

    private void abrirDirectorioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirDirectorioActionPerformed
        // Abrir directorio de diagramas
        openDiagramsDirectory();
    }//GEN-LAST:event_abrirDirectorioActionPerformed

    private void actualizarVisorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizarVisorActionPerformed
        // Actualizar manualmente el visor de imagenes
        updateImageViewer();
        displayInfoMessage(
            "🔄 Visor de imagenes actualizado!\n" +
            "📊 Se han cargado las imagenes disponibles en la carpeta 'diagramas_venn'.",
            "Visor Actualizado");
    }//GEN-LAST:event_actualizarVisorActionPerformed

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        // Crear y mostrar ventana de ayuda con sintaxis
        displaySyntaxHelp();
    }

    private void archivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_archivosActionPerformed
    }//GEN-LAST:event_archivosActionPerformed

    private void R_JsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_R_JsonActionPerformed
        // Abrir reporte JSON
        openJsonReport();
    }//GEN-LAST:event_R_JsonActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        showNextImage();
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousActionPerformed
        showPreviousImage();
    }//GEN-LAST:event_btnPreviousActionPerformed

    private void limpiarConsolaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpiarConsolaActionPerformed
       clearEditorAndConsole();
    }//GEN-LAST:event_limpiarConsolaActionPerformed

    private void btnDownloandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownloandActionPerformed
        downloadImage();
    }//GEN-LAST:event_btnDownloandActionPerformed
    

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ApplicationUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ApplicationUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ApplicationUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ApplicationUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ApplicationUI().setVisible(true);
            }
        });
    }
    
    private void displaySyntaxHelp() { 
        String syntaxGuideText = """
         GUÍA DE SINTAXIS DEL LENGUAJE
        
         DEFINICIÓN DE CONJUNTOS:
        • CONJ : nombre -> elemento1, elemento2, ... ;
        • CONJ : numeros -> 1 ~ 10 ;  // Rango
        • CONJ : letras ->  A ~ z ;   // Rango de caracteres

         OPERACIONES:
        • OPERA : result1 -> U {conjunto1} {conjunto2} ;  // Unión
        • OPERA : result2 -> & {conjunto1} {conjunto2} ;  // Intersección
        • OPERA : result3 -> - {conjunto1} {conjunto2} ;  // Diferencia
        • OPERA : result4 -> ^ {conjunto1} ;  // Complemento

         EVALUACIÓN DE OPERACIONES:
        • EVALUAR ( {a, b} , result1 ) ;
        • EVALUAR ( {a, b} , result2 ) ;
        • EVALUAR ( {a, b} , result3 ) ;
        • EVALUAR ( {a, b} , result4 ) ; 

         SÍMBOLOS ESPECIALES:
        • U : Unión
        • & : Intersección
        • - : Diferencia
        • ^ : Complemento
        • ~ : Rango 
        
         EJEMPLOS:
        CONJ : A -> H,o,l,a ;
        CONJ : B -> 1,2,3,4,5 ;
        OPERA : op2 ->  U {A} {B} ; 
        EVALUAR ( {a, b} , op2 ) ;
        
         Tip: Use el resaltado de sintaxis para identificar elementos correctamente.
        """;
        
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        //  VENTANA DE AYUDA CON COLORES DE LA INTERFAZ PRINCIPAL
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        
        JDialog dialog = new JDialog(this, "📚 Guía Oficial de Sintaxis", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);
        dialog.setSize(720, 650);
        dialog.setLocationRelativeTo(this);
        
        //  PANEL PRINCIPAL CON COLORES DE LA INTERFAZ
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(30, 30, 46)); 
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 51, 51), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        //  CABECERA CON COLORES DE LA INTERFAZ
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(51, 51, 51)); 
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(720, 85));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 30, 46), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Título principal con tipografía clara
        JLabel titleLabel = new JLabel("GUIA OFICIAL DE SINTAXIS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE); 
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Subtítulo con tipografía clara
        JLabel subtitleLabel = new JLabel("Manual de Referencia Completo");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.WHITE); 
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titleContainer = new JPanel(new BorderLayout(0, 8));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel, BorderLayout.CENTER);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        //  ÁREA DE CONTENIDO CON COLORES DE LA INTERFAZ
        JTextArea textArea = new JTextArea(syntaxGuideText);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        textArea.setBackground(new Color(51, 51, 51)); 
        textArea.setForeground(Color.WHITE); 
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setCaretColor(Color.WHITE);
        textArea.setMargin(new java.awt.Insets(25, 30, 25, 30));
        textArea.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 46), 2));
        
        //  ScrollPane con colores de la interfaz
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBackground(new Color(51, 51, 51));
        scrollPane.getViewport().setBackground(new Color(51, 51, 51));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 51, 51), 1));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Personalizar barras de desplazamiento con colores de la interfaz
        scrollPane.getVerticalScrollBar().setBackground(new Color(51, 51, 51));
        scrollPane.getHorizontalScrollBar().setBackground(new Color(51, 51, 51));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        //  PANEL DE BOTONES CON COLORES DE LA INTERFAZ
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(new Color(51, 51, 51)); 
        buttonPanel.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 46), 1));
        buttonPanel.setPreferredSize(new Dimension(720, 60));
        
        //  BOTÓN CERRAR CON ESTILO DE LA INTERFAZ
        JButton closeButton = new JButton("Entendido");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setBackground(new Color(30, 30, 46)); 
        closeButton.setForeground(Color.WHITE); 
        closeButton.setBorder(BorderFactory.createLineBorder(new Color(51, 51, 51), 2));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(120, 35));
        
        //  BOTÓN DE AYUDA CON ESTILO DE LA INTERFAZ
        JButton helpButton = new JButton("Mas Info");
        helpButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        helpButton.setBackground(new Color(51, 51, 51)); 
        helpButton.setForeground(Color.WHITE); 
        helpButton.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 46), 2));
        helpButton.setFocusPainted(false);
        helpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpButton.setPreferredSize(new Dimension(120, 35));
        
        // Eventos de los botones
        closeButton.addActionListener(e -> dialog.dispose());
        
        helpButton.addActionListener(e -> {
            displayInfoMessage(
                "📚 Esta guía contiene toda la sintaxis necesaria para usar el lenguaje.\n\n" +
                "🔹 Los conjuntos se definen con CONJ\n" +
                "🔹 Las operaciones se definen con OPERA\n" +
                "🔹 Use EVALUAR para probar las operaciones\n\n" +
                "💡 Consulte los ejemplos para mayor claridad.",
                "Información Adicional"
            );
        });
        
        buttonPanel.add(helpButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Configurar y mostrar la ventana con los colores de la interfaz
        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // MetodoS AUXILIARES PARA VENTANAS PERSONALIZADAS CON TEMA OSCURO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Muestra un mensaje de información con tema oscuro personalizado
     * @param mensaje El mensaje a mostrar
     * @param titulo El título de la ventana
     */
    private void displayInfoMessage(String mensaje, String titulo) {
        showCustomMessage(mensaje, titulo, "ℹ️", new Color(70, 130, 180));
    }
    
    /**
     * Muestra un mensaje de éxito con tema oscuro personalizado y celebración
     * @param mensaje El mensaje a mostrar
     * @param titulo El título de la ventana
     */
    private void displaySuccessMessage(String mensaje, String titulo) {
        showCelebrationMessage(mensaje, titulo, "🎉", new Color(50, 150, 50));
    }
    
    /**
     * Muestra un mensaje de error con tema oscuro personalizado y efectos dramáticos
     * @param mensaje El mensaje a mostrar
     * @param titulo El título de la ventana
     */
    private void displayErrorMessage(String mensaje, String titulo) {
        displayDramaticMessage(mensaje, titulo, "💥", new Color(220, 50, 50));
    }
    
    /**
     * Muestra un mensaje de advertencia con tema oscuro personalizado
     * @param mensaje El mensaje a mostrar
     * @param titulo El título de la ventana
     */
    private void showWarningMessage(String mensaje, String titulo) {
        showCustomMessage(mensaje, titulo, "⚠️", new Color(255, 165, 0));
    }
    
    /**
     * Ventana especial para celebraciones con confeti y efectos especiales
     */
    private void showCelebrationMessage(String mensaje, String titulo, String icono, Color colorAcento) {
        JDialog dialog = new JDialog(this, "🎊 " + titulo + " 🎊", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(520, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Panel principal con efectos de celebración
        JPanel mainPanel = new JPanel() {
            private List<Confetti> confettiList = new ArrayList<>();
            private javax.swing.Timer animationTimer;
            
            // Clase para partículas de confeti
            class Confetti {
                int x, y, vx, vy;
                Color color;
                int size;
                
                Confetti() {
                    x = (int)(Math.random() * getWidth());
                    y = -10;
                    vx = (int)(Math.random() * 4 - 2);
                    vy = (int)(Math.random() * 3 + 2);
                    color = new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
                    size = (int)(Math.random() * 6 + 3);
                }
                
                void update() {
                    x += vx;
                    y += vy;
                    vy += 0.1; 
                }
                
                void draw(Graphics2D g2d) {
                    g2d.setColor(color);
                    g2d.fillOval(x, y, size, size);
                }
                
                boolean isOffScreen() {
                    return y > getHeight() + 10;
                }
            }
            
            {
                // Inicializar confeti
                for (int i = 0; i < 30; i++) {
                    confettiList.add(new Confetti());
                }
                
                animationTimer = new javax.swing.Timer(50, e -> {
                    // Actualizar confeti
                    for (int i = confettiList.size() - 1; i >= 0; i--) {
                        Confetti c = confettiList.get(i);
                        c.update();
                        if (c.isOffScreen()) {
                            confettiList.remove(i);
                            confettiList.add(new Confetti()); 
                        }
                    }
                    repaint();
                });
                animationTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo degradado celebratorio
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(51, 51, 51),
                    getWidth(), getHeight(), new Color(colorAcento.getRed()/4, colorAcento.getGreen()/4, colorAcento.getBlue()/4)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Dibujar confeti
                for (Confetti c : confettiList) {
                    c.draw(g2d);
                }
                
                // Efectos de luces brillantes
                g2d.setColor(new Color(255, 255, 255, 30));
                for (int i = 0; i < 5; i++) {
                    int x = (int)(Math.random() * getWidth());
                    int y = (int)(Math.random() * getHeight());
                    int size = (int)(Math.random() * 20 + 10);
                    g2d.fillOval(x, y, size, size);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Resto del código similar al Metodo anterior pero con tema de celebración
        showCustomWindow(dialog, mainPanel, mensaje, titulo, icono, colorAcento, "🎈 ¡GENIAL! 🎈");
    }
    
    /**
     * Ventana especial para errores con efectos dramáticos
     */
    private void displayDramaticMessage(String mensaje, String titulo, String icono, Color colorAcento) {
        JDialog dialog = new JDialog(this, "⚡ " + titulo + " ⚡", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(520, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Panel principal con efectos 
        JPanel mainPanel = new JPanel() {
            private javax.swing.Timer pulseTimer;
            private boolean pulsing = false;
            
            {
                pulseTimer = new javax.swing.Timer(200, e -> {
                    pulsing = !pulsing;
                    repaint();
                });
                pulseTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo con efecto de pulso rojo
                Color baseColor = pulsing ? new Color(51, 51, 51) : new Color(70, 51, 51);
                Color pulseColor = pulsing ? new Color(80, 51, 51) : new Color(51, 51, 51);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, baseColor,
                    getWidth(), getHeight(), pulseColor
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Efectos de "rayos" de error
                g2d.setColor(new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), pulsing ? 50 : 20));
                g2d.setStroke(new BasicStroke(3));
                for (int i = 0; i < 8; i++) {
                    int angle = i * 45;
                    int startX = getWidth() / 2;
                    int startY = getHeight() / 2;
                    int endX = startX + (int)(Math.cos(Math.toRadians(angle)) * 100);
                    int endY = startY + (int)(Math.sin(Math.toRadians(angle)) * 100);
                    g2d.drawLine(startX, startY, endX, endY);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        showCustomWindow(dialog, mainPanel, mensaje, titulo, icono, colorAcento, "💀 ENTENDIDO 💀");
    }
    
    /**
     * Metodo auxiliar para completar las ventanas personalizadas
     */
    private void showCustomWindow(JDialog dialog, JPanel mainPanel, String mensaje, String titulo, String icono, Color colorAcento, String textoBoton) {
        // Panel de cabecera con título súper estilizado
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("✨ " + titulo.toUpperCase() + " ✨");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(colorAcento.brighter());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel de contenido súper mejorado
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));
        
        // Icono súper grande y animado
        JLabel iconLabel = new JLabel(icono);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(100, 100));
        
        // Efecto de rotación para el icono
        javax.swing.Timer rotationTimer = new javax.swing.Timer(100, null);
        rotationTimer.addActionListener(e -> {
            // Crear efecto de "temblor" en el icono
            int offset = (int)(Math.random() * 4 - 2);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(offset, offset, 0, 0));
        });
        rotationTimer.start();
        
        contentPanel.add(iconLabel, BorderLayout.WEST);
        
        // Mensaje súper estilizado
        JPanel messageContainer = new JPanel(new BorderLayout());
        messageContainer.setOpaque(false);
        messageContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        
        JTextArea textArea = new JTextArea(mensaje);
        textArea.setFont(new Font("SansSerif", Font.BOLD, 14));
        textArea.setBackground(new Color(0, 0, 0, 0));
        textArea.setForeground(Color.WHITE);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        
        // Borde súper estilizado para el área de texto
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorAcento, 2),
                BorderFactory.createLineBorder(colorAcento.darker(), 1)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        messageContainer.add(textArea, BorderLayout.CENTER);
        contentPanel.add(messageContainer, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Botón súper mega estilizado
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setOpaque(false);
        
        JButton okButton = new JButton(textoBoton) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo con gradiente y bordes redondeados
                GradientPaint gradient1 = new GradientPaint(
                    0, 0, colorAcento.brighter(),
                    0, getHeight()/2, colorAcento
                );
                GradientPaint gradient2 = new GradientPaint(
                    0, getHeight()/2, colorAcento,
                    0, getHeight(), colorAcento.darker()
                );
                
                g2d.setPaint(gradient1);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight()/2, 20, 20);
                g2d.setPaint(gradient2);
                g2d.fillRoundRect(0, getHeight()/2, getWidth(), getHeight()/2, 20, 20);
                
                // Borde brillante
                g2d.setColor(colorAcento.brighter().brighter());
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Efecto de brillo interno
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() / 3, 15, 15);
                
                // Texto con sombra
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(getText(), x + 2, y + 2); 
                
                g2d.setColor(getForeground());
                g2d.drawString(getText(), x, y);
            }
        };
        
        okButton.setBackground(colorAcento);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        okButton.setPreferredSize(new Dimension(180, 45));
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efectos súper avanzados del botón
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setPreferredSize(new Dimension(185, 48));
                okButton.revalidate();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setPreferredSize(new Dimension(180, 45));
                okButton.revalidate();
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                okButton.setPreferredSize(new Dimension(175, 42));
                okButton.revalidate();
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButton.setPreferredSize(new Dimension(185, 48));
                okButton.revalidate();
            }
        });
        
        okButton.addActionListener(e -> {
            // Cierre simple sin efectos de opacidad
            dialog.dispose();
        });
        
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(mainPanel);
        
        // Mostrar directamente sin efectos de fade
        dialog.setVisible(true);
    }
    
    /**
     * Metodo genérico para mostrar mensajes personalizados con tema oscuro y efectos divertidos
     * @param mensaje El mensaje a mostrar
     * @param titulo El título de la ventana
     * @param icono El icono emoji a mostrar
     * @param colorAcento Color de acento para el título
     */
    private void showCustomMessage(String mensaje, String titulo, String icono, Color colorAcento) {
        // Crear dialog personalizado con efectos
        JDialog dialog = new JDialog(this, "🎨 " + titulo + " 🎨", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Panel principal con gradiente de fondo
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Crear gradiente sutil
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(51, 51, 51),
                    0, getHeight(), new Color(65, 65, 65)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Agregar lineas decorativas
                g2d.setColor(colorAcento);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(20, 20, getWidth() - 20, 20);
                g2d.drawLine(20, getHeight() - 60, getWidth() - 20, getHeight() - 60);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Panel de cabecera con título estilizado
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("✨ " + titulo.toUpperCase() + " ✨");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(colorAcento);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel del contenido principal
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        // Panel izquierdo con icono animado
        JPanel iconPanel = new JPanel() {
            private boolean glowing = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dibujar círculo de fondo con efecto glow
                int size = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                if (glowing) {
                    // Efecto glow
                    for (int i = 5; i > 0; i--) {
                        g2d.setColor(new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 30 * i));
                        g2d.fillOval(x - i * 2, y - i * 2, size + i * 4, size + i * 4);
                    }
                }
                
                g2d.setColor(new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 60));
                g2d.fillOval(x, y, size, size);
                g2d.setColor(colorAcento);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x, y, size, size);
            }
            
            {
                javax.swing.Timer timer = new javax.swing.Timer(800, e -> {
                    glowing = !glowing;
                    repaint();
                });
                timer.start();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(80, 80));
        
        JLabel iconLabel = new JLabel(icono);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconPanel.setLayout(new BorderLayout());
        iconPanel.add(iconLabel, BorderLayout.CENTER);
        
        contentPanel.add(iconPanel, BorderLayout.WEST);
        
        // Panel central con mensaje estilizado
        JPanel messageContainer = new JPanel(new BorderLayout());
        messageContainer.setOpaque(false);
        messageContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        JTextArea textArea = new JTextArea(mensaje);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textArea.setBackground(new Color(0, 0, 0, 0)); // Transparente
        textArea.setForeground(Color.WHITE);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 100), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Agregar scroll si el mensaje es muy largo
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(300, 80));
        
        messageContainer.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(messageContainer, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Panel de botones con efectos especiales
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        // Botón principal con efectos 3D
        JButton okButton = new JButton("🚀 ACEPTAR 🚀") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo con gradiente
                GradientPaint gradient = new GradientPaint(
                    0, 0, getBackground().brighter(),
                    0, getHeight(), getBackground().darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Borde con efecto 3D
                g2d.setColor(getBackground().brighter().brighter());
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                
                // Dibujar texto
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        okButton.setBackground(colorAcento);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        okButton.setPreferredSize(new Dimension(140, 35));
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> {
            // Cierre simple sin efectos de opacidad
            dialog.dispose();
        });
        
        // Efectos hover 
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(colorAcento.brighter());
                okButton.setPreferredSize(new Dimension(145, 38)); // Crecer ligeramente
                okButton.revalidate();
                
                // Efecto de brillo
                javax.swing.Timer glowTimer = new javax.swing.Timer(50, null);
                glowTimer.addActionListener(e -> okButton.repaint());
                glowTimer.setRepeats(false);
                glowTimer.start();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(colorAcento);
                okButton.setPreferredSize(new Dimension(140, 35)); // Volver al tamaño original
                okButton.revalidate();
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                okButton.setBackground(colorAcento.darker());
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButton.setBackground(colorAcento.brighter());
            }
        });
        
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(mainPanel);
        
        // Mostrar directamente sin efectos de fade
        dialog.setVisible(true);
    }
    
    /**
     * Crea un JFileChooser con tema oscuro personalizado
     * @param titulo Título del file chooser
     * @param extension Extensión de archivos a filtrar (ej: ".ca")
     * @param descripcion Descripción del filtro de archivos
     * @return JFileChooser personalizado
     */
    private JFileChooser createCustomFileChooser(String titulo, String extension, String descripcion) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(titulo);
        
        // Personalizar colores del JFileChooser
        try {
            // Cambiar el Look and Feel temporalmente para el file chooser
            javax.swing.UIManager.put("Panel.background", new Color(51, 51, 51));
            javax.swing.UIManager.put("Label.foreground", Color.WHITE);
            javax.swing.UIManager.put("List.background", new Color(51, 51, 51));
            javax.swing.UIManager.put("List.foreground", Color.WHITE);
            javax.swing.UIManager.put("TextField.background", new Color(70, 70, 70));
            javax.swing.UIManager.put("TextField.foreground", Color.WHITE);
            javax.swing.UIManager.put("ComboBox.background", new Color(70, 70, 70));
            javax.swing.UIManager.put("ComboBox.foreground", Color.WHITE);
            javax.swing.UIManager.put("Button.background", new Color(80, 80, 80));
            javax.swing.UIManager.put("Button.foreground", Color.WHITE);
            
            // Actualizar la UI del file chooser
            SwingUtilities.updateComponentTreeUI(fileChooser);
        } catch (Exception e) {
            // Si falla la personalización, usar el tema por defecto
            System.err.println("No se pudo personalizar el tema del FileChooser: " + e.getMessage());
        }
        
        // Agregar filtro de archivos si se especifica
        if (extension != null && descripcion != null) {
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(extension);
                }

                @Override
                public String getDescription() {
                    return descripcion;
                }
            });
        }
        
        return fileChooser;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem R_Errores;
    private javax.swing.JMenuItem R_Json;
    private javax.swing.JMenuItem R_Tokens;
    private javax.swing.JMenuItem abrirDirectorio;
    private javax.swing.JMenuItem actualizarVisor;
    private javax.swing.JMenu archivos;
    private javax.swing.JButton btnDownloand;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JMenu ejecutar;
    private javax.swing.JMenuItem ejecutarP;
    private javax.swing.JMenuItem generarDiagramas;
    private javax.swing.JMenuItem guardar;
    private javax.swing.JMenuItem guardarComo;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JMenuItem limpiarConsola;
    private javax.swing.JMenuItem newArchivo;
    private javax.swing.JMenu reporte;
    private javax.swing.JMenu reporte1;
    private javax.swing.JTable tableReporte;
    private javax.swing.JTextArea textArea;
    private javax.swing.JTextPane txtConsola;
    // End of variables declaration//GEN-END:variables
}
