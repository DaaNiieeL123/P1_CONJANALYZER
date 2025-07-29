package Graphics;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Gestor centralizado para la generación y almacenamiento de diagramas de Venn como imagenes
 * Permite crear, abrir, eliminar y gestionar diagramas de Venn
 * @author danie
 */
public class ImageDiagramManager {
    
    private static ImageDiagramManager instance;
    
    public static ImageDiagramManager getInstance() {
        if (instance == null) {
            instance = new ImageDiagramManager();
        }
        return instance;
    }
    
    private String outputDirectory;
    private String imageFormat;
    private List<String> generatedImages;
    private boolean autoOpen;
    private JFrame parentFrame;
    
    private ImageDiagramManager() {
        this.outputDirectory = "diagramas_venn";
        this.imageFormat = "png";
        this.generatedImages = Collections.synchronizedList(new ArrayList<>());
        this.autoOpen = false;
        this.parentFrame = null;
        
        createOutputDirectory();
    }
    
    /**
     * Genera una imagen de diagrama de Venn automáticamente
     */
    public String generateDiagramImage(VennDiagramData data) {
        try {
            String fileName = generateUniqueFileName(data);
            String fullPath = outputDirectory + File.separator + fileName;
            
            boolean success = VennDiagramGenerator.generateImageFile(data, fullPath, imageFormat);
            
            if (success) {
                generatedImages.add(fullPath);
                //System.out.println("✅ Diagrama generado: " + fullPath);
                
                if (autoOpen) {
                    openImage(fullPath);
                }
                
                return fullPath;
            } else {
                System.err.println("❌ Error generando diagrama para: " + data.getOperationName());
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generando diagrama: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Abre una imagen en el visualizador predeterminado del sistema
     */
    public void openImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Desktop.getDesktop().open(imageFile);
            } else {
                System.err.println("❌ Archivo no encontrado: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error abriendo imagen: " + e.getMessage());
        }
    }
    
    /**
     * Abre el directorio de salida en el explorador de archivos
     */
    public void openOutputDirectory() {
        try {
            File directory = new File(outputDirectory);
            if (directory.exists()) {
                Desktop.getDesktop().open(directory);
            } else {
                System.err.println("❌ Directorio no encontrado: " + outputDirectory);
            }
        } catch (Exception e) {
            System.err.println("❌ Error abriendo directorio: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una imagen especifica
     */
    public boolean deleteImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists() && imageFile.delete()) {
                generatedImages.remove(imagePath);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error eliminando imagen: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Limpia la lista de imagenes generadas 
     */
    public void clearGeneratedImages() {
        generatedImages.clear();
    }
    
    /**
     * Elimina fisicamente todas las imagenes generadas
     */
    public void deleteAllGeneratedImages() {    
        // Crear el directorio si no existe
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            return;
        }
        
        // Obtener todos los archivos de imagen en la carpeta
        File[] imageFiles = outputDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || 
                   lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif") ||
                   lowerName.endsWith(".bmp");
        });
        
        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                try {
                    if (imageFile.delete()) {
                        System.out.println("");
                    } else {
                        System.err.println("❌ No se pudo eliminar: " + imageFile.getName());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error eliminando " + imageFile.getName() + ": " + e.getMessage());
                }
            }
        }
        
        // Limpiar la lista de imagenes generadas
        generatedImages.clear();
        
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private void createOutputDirectory() {
        File directory = new File(outputDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    /*
     * Genera un nombre de archivo único basado en la fecha y hora actual
     */
    private String generateUniqueFileName(VennDiagramData data) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = now.format(formatter);
        
        String title = data.getTitle() != null ? 
            data.getTitle().replaceAll("[^a-zA-Z0-9]", "_") : 
            "diagrama";
        
        return String.format("venn_%s_%s.%s", title, timestamp, imageFormat);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    public String getOutputDirectory() {
        return outputDirectory;
    }
    
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        createOutputDirectory();
    }
    
    public String getImageFormat() {
        return imageFormat;
    }
    
    public void setImageFormat(String imageFormat) {
        if (imageFormat.equals("png") || imageFormat.equals("jpg") || imageFormat.equals("jpeg")) {
            this.imageFormat = imageFormat;
        } else {
            this.imageFormat = "png";
        }
    }
    
    public List<String> getGeneratedImages() {
        return new ArrayList<>(generatedImages);
    }
    
    public int getGeneratedImagesCount() {
        return generatedImages.size();
    }
    
    public boolean isAutoOpen() {
        return autoOpen;
    }
    
    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }
    
    public JFrame getParentFrame() {
        return parentFrame;
    }
    
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE COMPATIBILIDAD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    public void addDiagram(VennDiagramData data) {
        generateDiagramImage(data);
    }
    
    public int getDiagramCount() {
        return generatedImages.size();
    }
    
    public boolean hasDiagrams() {
        return !generatedImages.isEmpty();
    }
    
    public void showAllDiagrams() {
        if (generatedImages.isEmpty()) {
            System.out.println("⚠️ No hay diagramas para mostrar");
            return;
        }
        openOutputDirectory();
    }
    
    public void clearDiagrams() {
        clearGeneratedImages();
    }
}