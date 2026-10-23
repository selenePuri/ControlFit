package com.cibertec.controlfit.service.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

class ReportePdfGeneradorTest {

    private final ReportePdfGenerador generador = new ReportePdfGenerador();
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 22, 14, 30);

    private static ReporteDatos.Pagos pagos(int cantidad) {
        List<ReporteDatos.FilaPago> filas = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            filas.add(new ReporteDatos.FilaPago("#P" + i, "#M" + i, "Ángela Peña Núñez", "7000" + (1000 + i),
                    AHORA.minusDays(i), new BigDecimal("1500.00"), i == 1 ? new BigDecimal("500.00") : BigDecimal.ZERO,
                    "YAPE", "PAGADO"));
        }
        BigDecimal cobrado = new BigDecimal("1500.00").multiply(BigDecimal.valueOf(cantidad));
        BigDecimal devuelto = cantidad == 0 ? BigDecimal.ZERO : new BigDecimal("500.00");
        List<ReporteDatos.FilaDevolucion> devoluciones = cantidad == 0 ? List.of() : List.of(
                new ReporteDatos.FilaDevolucion("#D1", "#P1", "Ángela Peña Núñez", AHORA, new BigDecimal("500.00"),
                        "Cambio de plan", "COMPLETADA"),
                new ReporteDatos.FilaDevolucion("#D2", "#P1", "Ángela Peña Núñez", AHORA, new BigDecimal("200.00"),
                        null, "PENDIENTE"));
        return new ReporteDatos.Pagos(AHORA, "admin", cantidad, cobrado, devuelto, cobrado.subtract(devuelto),
                List.of(new ReporteDatos.ConteoEstado("PAGADO", cantidad, cobrado)),
                List.of(new ReporteDatos.ConteoEstado("COMPLETADA", cantidad == 0 ? 0 : 1, devuelto),
                        new ReporteDatos.ConteoEstado("PENDIENTE", cantidad == 0 ? 0 : 1, new BigDecimal("200.00"))),
                filas, devoluciones);
    }

    private static ReporteDatos.Membresias membresias(int cantidad) {
        List<ReporteDatos.FilaMembresia> filas = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            filas.add(new ReporteDatos.FilaMembresia("#M" + i, "José Ñañez", "7000" + (1000 + i), "MENSUAL",
                    "Sin promoción", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                    new BigDecimal("100.00"), "ACTIVA"));
        }
        return new ReporteDatos.Membresias(AHORA, "recepcionista", cantidad,
                List.of(new ReporteDatos.ConteoEstado("ACTIVA", cantidad, BigDecimal.ZERO)), filas);
    }

    private static String texto(byte[] pdf) throws IOException {
        PdfReader lector = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(lector);
            StringBuilder sb = new StringBuilder();
            for (int p = 1; p <= lector.getNumberOfPages(); p++) {
                sb.append(extractor.getTextFromPage(p)).append('\n');
            }
            return sb.toString();
        } finally {
            lector.close();
        }
    }

    @Test
    void reportePagosEsUnPdfValidoConResumenYDetalle() throws IOException {
        byte[] pdf = generador.generarPagos(pagos(3));

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(new PdfReader(pdf).getNumberOfPages()).isEqualTo(1);
        String t = texto(pdf);
        assertThat(t).contains("REPORTE DE PAGOS", "Total cobrado", "S/ 4,500.00", "Total devuelto", "S/ 500.00",
                "Ingresos netos", "S/ 4,000.00", "Cantidad de pagos", "Devoluciones pendientes", "S/ 200.00",
                "#P1", "Ángela Peña Núñez", "YAPE", "PAGADO", "Detalle de devoluciones", "#D1",
                "Cambio de plan", "COMPLETADA", "PENDIENTE", "Página 1 de 1");
    }

    @Test
    void reporteMembresiasMuestraTotalYResumenPorEstado() throws IOException {
        byte[] pdf = generador.generarMembresias(membresias(4));

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        String t = texto(pdf);
        assertThat(t).contains("REPORTE DE MEMBRESÍAS", "Total de membresías", "Activas", "Vencidas",
                "#M4", "José Ñañez", "MENSUAL", "Sin promoción", "01/10/2026", "31/10/2026");
    }

    @Test
    void conMuchasFilasPaginaYRepiteElEncabezado() throws IOException {
        byte[] pdf = generador.generarPagos(pagos(80));

        PdfReader lector = new PdfReader(pdf);
        try {
            assertThat(lector.getNumberOfPages()).isGreaterThan(1);
            PdfTextExtractor extractor = new PdfTextExtractor(lector);
            assertThat(extractor.getTextFromPage(2)).contains("Fecha de pago");   
        } finally {
            lector.close();
        }
    }

    @Test
    void sinRegistrosGeneraElPdfConMensajeVacio() throws IOException {
        String t = texto(generador.generarPagos(pagos(0)));

        assertThat(t).contains("No hay pagos registrados.", "No hay devoluciones registradas.", "Ingresos netos", "S/ 0.00");
    }
}
