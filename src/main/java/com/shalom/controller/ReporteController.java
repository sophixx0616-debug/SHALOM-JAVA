package com.shalom.controller;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// =========================================================================
// 1. CLASE ABSTRACTA BASE (PATRÓN TEMPLATE METHOD)
// =========================================================================
abstract class ReportTemplate {

    // El Método Plantilla que define los pasos estrictos del algoritmo
    public final byte[] generateReport(String title, List<Map<String, Object>> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        initializeDocument(out);
        buildHeader(out, title);
        buildBody(out, data);
        finalizeDocument(out);
        
        return out.toByteArray();
    }

    // Pasos comunes o abstractos que cada formato implementará a su manera
    protected abstract void initializeDocument(ByteArrayOutputStream out);
    protected abstract void buildHeader(ByteArrayOutputStream out, String title);
    protected abstract void buildBody(ByteArrayOutputStream out, List<Map<String, Object>> data);
    protected abstract void finalizeDocument(ByteArrayOutputStream out);
}

// =========================================================================
// 2. IMPLEMENTACIÓN CONCRETA PARA REPORTES EN PDF
// =========================================================================
class PdfReportGenerator extends ReportTemplate {
    @Override
    protected void initializeDocument(ByteArrayOutputStream out) {
        // Lógica simulada de una librería como iText o OpenPDF para abrir el documento
        out.writeBytes("[PDF Inicializado]\n".getBytes());
    }

    @Override
    protected void buildHeader(ByteArrayOutputStream out, String title) {
        out.writeBytes(("[Encabezado PDF: " + title + "]\n").getBytes());
    }

    @Override
    protected void buildBody(ByteArrayOutputStream out, List<Map<String, Object>> data) {
        out.writeBytes("[Cuerpo PDF: Dibujando tablas y celdas estilizadas]\n".getBytes());
    }

    @Override
    protected void finalizeDocument(ByteArrayOutputStream out) {
        out.writeBytes("[PDF Cerrado y Guardado]\n".getBytes());
    }
}

// =========================================================================
// 3. IMPLEMENTACIÓN CONCRETA PARA REPORTES EN EXCEL
// =========================================================================
class ExcelReportGenerator extends ReportTemplate {
    @Override
    protected void initializeDocument(ByteArrayOutputStream out) {
        // Lógica simulada para iniciar un libro de trabajo de Apache POI (Workbook)
        out.writeBytes("[Excel Inicializado (Workbook)]\n".getBytes());
    }

    @Override
    protected void buildHeader(ByteArrayOutputStream out, String title) {
        out.writeBytes(("[Fila 1 Excel - Título: " + title + "]\n").getBytes());
    }

    @Override
    protected void buildBody(ByteArrayOutputStream out, List<Map<String, Object>> data) {
        out.writeBytes("[Cuerpo Excel: Insertando datos en filas y columnas de celdas]\n".getBytes());
    }

    @Override
    protected void finalizeDocument(ByteArrayOutputStream out) {
        out.writeBytes("[Excel Autoajustado y Guardado]\n".getBytes());
    }
}

// =========================================================================
// 4. EL CONTROLADOR COMO FACHADA (REPORTE CONTROLLER)
// =========================================================================
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    /**
     * Endpoint unificado que utiliza el Template Method para exportar reportes.
     * Cambia dinámicamente de estrategia visual sin repetir el flujo del algoritmo.
     *
     * @param format Tipo de formato solicitado ("pdf" o "excel")
     */
    @GetMapping("/ventas")
    public ResponseEntity<byte[]> downloadSalesReport(@RequestParam String format) {
        // Datos simulados extraídos de la base de datos
        List<Map<String, Object>> mockData = List.of(Map.of("id", 1, "monto", 150.0));
        String title = "Reporte de Ventas Shalom";

        ReportTemplate generator;
        MediaType mediaType;
        String extension;

        // Selección dinámica del generador concreto
        if ("pdf".equalsIgnoreCase(format)) {
            generator = new PdfReportGenerator();
            mediaType = MediaType.APPLICATION_PDF;
            extension = ".pdf";
        } else if ("excel".equalsIgnoreCase(format)) {
            generator = new ExcelReportGenerator();
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            extension = ".xlsx";
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // Ejecución del Template Method definido en la clase padre
        byte[] fileContent = generator.generateReport(title, mockData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", "reporte_ventas" + extension);

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }
}
