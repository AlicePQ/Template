import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Clase abstracta que define la PLANTILLA del algoritmo.
 * El orden del proceso está fijado en mine(), que es final.
 */
abstract class DataMiner {

    public final void mine(String path) {
        InputStream file = null;
        try {
            file = openFile(path);
            byte[] raw = extractData(file);
            List<Map<String, Object>> data = parseData(raw);

            Analysis analysis = analyzeData(data);
            sendReport(analysis);
        } catch (IOException e) {
            System.err.println("Error procesando archivo: " + e.getMessage());
        } finally {
            closeFile(file);
        }
    }

    /* --- Pasos variables (dependen del tipo de archivo) --- */
    protected abstract InputStream openFile(String path) throws IOException;

    protected abstract byte[] extractData(InputStream file) throws IOException;

    protected abstract List<Map<String, Object>> parseData(byte[] rawData);

    protected abstract void closeFile(InputStream file);

    /* --- Pasos comunes (pueden sobreescribirse si se requiere) --- */
    protected Analysis analyzeData(List<Map<String, Object>> data) {
        // Ejemplo: métricas simples
        int rows = data.size();
        Set<String> columns = new LinkedHashSet<>();
        for (Map<String, Object> row : data) columns.addAll(row.keySet());
        return new Analysis(rows, new ArrayList<>(columns));
    }

    protected void sendReport(Analysis analysis) {
        System.out.println("=== Reporte de Análisis ===");
        System.out.println("Filas: " + analysis.rows());
        System.out.println("Columnas: " + analysis.columns());
        System.out.println("===========================");
    }

    /* DTO simple para resultados de análisis */
    protected record Analysis(int rows, List<String> columns) {}
}

/* -------------------- Implementaciones concretas -------------------- */

class PDFDataMiner extends DataMiner {

    @Override
    protected InputStream openFile(String path) throws IOException {
        System.out.println("[PDF] Abriendo " + path);
        return new FileInputStream(path);
    }

    @Override
    protected byte[] extractData(InputStream file) throws IOException {
        System.out.println("[PDF] Extrayendo bytes (simulado)");
        return file.readAllBytes(); // En producción usar lib PDF
    }

    @Override
    protected List<Map<String, Object>> parseData(byte[] rawData) {
        System.out.println("[PDF] Parseando (simulado: cada línea -> mapa)");
        String text = new String(rawData, StandardCharsets.UTF_8);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String line : text.split("\\R")) {
            if (line.isBlank()) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("paragraph", line.trim());
            rows.add(row);
        }
        return rows;
    }

    @Override
    protected void closeFile(InputStream file) {
        System.out.println("[PDF] Cerrando archivo");
        try { if (file != null) file.close(); } catch (IOException ignored) {}
    }
}

class CSVDataMiner extends DataMiner {

    @Override
    protected InputStream openFile(String path) throws IOException {
        System.out.println("[CSV] Abriendo " + path);
        return new FileInputStream(path);
    }

    @Override
    protected byte[] extractData(InputStream file) throws IOException {
        System.out.println("[CSV] Extrayendo bytes");
        return file.readAllBytes();
    }

    @Override
    protected List<Map<String, Object>> parseData(byte[] rawData) {
        System.out.println("[CSV] Parseando (simulado CSV separado por comas)");
        String text = new String(rawData, StandardCharsets.UTF_8);
        String[] lines = text.split("\\R");
        List<Map<String, Object>> rows = new ArrayList<>();
        String[] headers = lines.length > 0 ? lines[0].split(",") : new String[0];

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) continue;
            String[] values = lines[i].split(",");
            Map<String, Object> row = new LinkedHashMap<>();
            for (int c = 0; c < headers.length && c < values.length; c++) {
                row.put(headers[c].trim(), values[c].trim());
            }
            rows.add(row);
        }
        return rows;
    }

    @Override
    protected void closeFile(InputStream file) {
        System.out.println("[CSV] Cerrando archivo");
        try { if (file != null) file.close(); } catch (IOException ignored) {}
    }
}

class DocDataMiner extends DataMiner {

    @Override
    protected InputStream openFile(String path) throws IOException {
        System.out.println("[DOC] Abriendo " + path);
        return new FileInputStream(path);
    }

    @Override
    protected byte[] extractData(InputStream file) throws IOException {
        System.out.println("[DOC] Extrayendo bytes (simulado)");
        return file.readAllBytes(); // En producción usar Apache POI u otra
    }

    @Override
    protected List<Map<String, Object>> parseData(byte[] rawData) {
        System.out.println("[DOC] Parseando (simulado por líneas a 'runs')");
        String text = new String(rawData, StandardCharsets.UTF_8);
        List<Map<String, Object>> rows = new ArrayList<>();
        int i = 1;
        for (String line : text.split("\\R")) {
            if (line.isBlank()) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("run#" + (i++), line.trim());
            rows.add(row);
        }
        return rows;
    }

    @Override
    protected void closeFile(InputStream file) {
        System.out.println("[DOC] Cerrando archivo");
        try { if (file != null) file.close(); } catch (IOException ignored) {}
    }
}

/* -------------------- Ejemplo de uso -------------------- */

public class Main {
    public static void main(String[] args) {
        // Para probar, usa rutas reales en tu sistema.
        DataMiner csvMiner = new CSVDataMiner();
        DataMiner pdfMiner = new PDFDataMiner();
        DataMiner docMiner = new DocDataMiner();

        // Descomenta y ajusta las rutas:
        // csvMiner.mine("data/sample.csv");
        // pdfMiner.mine("data/sample.pdf.txt"); // texto simulado de PDF
        // docMiner.mine("data/sample.doc.txt"); // texto simulado de DOC

        // Nota: En este ejemplo, PDF/DOC leen texto plano para simplificar la demo.
        System.out.println("Ejemplo listo. Descomenta las llamadas y pon rutas válidas.");
    }
}
