package com.cibertec.controlfit.service.pdf;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
//generar pdf

@Component
public class ReportePdfGenerador {

    private static final Color ROJO = new Color(0xE5, 0x09, 0x14);
    private static final Color OSCURO = new Color(0x12, 0x12, 0x14);
    private static final Color TEXTO = new Color(0x1E, 0x1E, 0x24);
    private static final Color GRIS = new Color(0x6B, 0x6B, 0x78);
    private static final Color BORDE = new Color(0xD9, 0xD9, 0xDF);
    private static final Color ZEBRA = new Color(0xF5, 0xF5, 0xF8);
    private static final Color CAJA = new Color(0xFA, 0xFA, 0xFC);
    private static final Color VERDE = new Color(0x15, 0x80, 0x3D);
    private static final Color AMBAR = new Color(0xB4, 0x53, 0x09);
    private static final Color ROJO_OSCURO = new Color(0xB9, 0x1C, 0x1C);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarPagos(ReporteDatos.Pagos datos) {
        return construir("Reporte de pagos", datos.generadoEn(), datos.generadoPor(), documento -> {
            documento.add(resumen(
                    caja("Total cobrado (pagos PAGADO)", soles(datos.totalCobrado()), VERDE),
                    caja("Total devuelto (devoluciones completadas)", soles(datos.totalDevuelto()), ROJO_OSCURO),
                    caja("Ingresos netos", soles(datos.ingresosNetos()), ROJO)));
            documento.add(espacio(4));
            documento.add(resumen(
                    caja("Cantidad de pagos", String.valueOf(datos.cantidad()), TEXTO),
                    cajaEstado(datos.porEstado(), "PAGADO", "Pagados"),
                    cajaEstado(datos.porEstado(), "PENDIENTE", "Pendientes"),
                    cajaEstado(datos.porEstado(), "ANULADO", "Anulados"),
                    caja("Devoluciones pendientes", soles(montoDe(datos.devolucionesPorEstado(), "PENDIENTE")), AMBAR)));
            documento.add(nota("Total cobrado: suma de los pagos en estado PAGADO. Total devuelto: suma de las devoluciones "
                    + "COMPLETADAS. Ingresos netos = total cobrado - total devuelto. Los pagos pendientes o anulados y las "
                    + "devoluciones pendientes o anuladas no se cuentan. Cancelar una membresía no modifica sus pagos."));

            documento.add(subtitulo("Detalle de pagos"));
            PdfPTable tabla = tabla(new float[] {7, 8, 22, 9, 14, 10, 10, 11, 9},
                    "N° pago", "Membresía", "Cliente", "DNI", "Fecha de pago", "Monto", "Devuelto", "Método", "Estado");
            if (datos.filas().isEmpty()) {
                sinDatos(tabla, 9, "No hay pagos registrados.");
            }
            int n = 0;
            for (ReporteDatos.FilaPago f : datos.filas()) {
                Color fondo = (n++ % 2 == 1) ? ZEBRA : Color.WHITE;
                tabla.addCell(celda(f.idPago(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.idMembresia(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.cliente(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.dni(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(fechaHora(f.fechaPago()), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(soles(f.monto()), Element.ALIGN_RIGHT, fondo, null));
                tabla.addCell(celda(f.devuelto() != null && f.devuelto().signum() > 0 ? soles(f.devuelto()) : "-",
                        Element.ALIGN_RIGHT, fondo, null));
                tabla.addCell(celda(f.metodo(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.estado(), Element.ALIGN_LEFT, fondo, colorEstado(f.estado())));
            }
            documento.add(tabla);

            documento.add(subtitulo("Detalle de devoluciones"));
            PdfPTable devoluciones = tabla(new float[] {8, 8, 28, 16, 12, 18, 10},
                    "ID", "Pago", "Cliente", "Fecha", "Importe", "Motivo", "Estado");
            if (datos.devoluciones().isEmpty()) {
                sinDatos(devoluciones, 7, "No hay devoluciones registradas.");
            }
            int m = 0;
            for (ReporteDatos.FilaDevolucion f : datos.devoluciones()) {
                Color fondo = (m++ % 2 == 1) ? ZEBRA : Color.WHITE;
                devoluciones.addCell(celda(f.id(), Element.ALIGN_LEFT, fondo, null));
                devoluciones.addCell(celda(f.idPago(), Element.ALIGN_LEFT, fondo, null));
                devoluciones.addCell(celda(f.cliente(), Element.ALIGN_LEFT, fondo, null));
                devoluciones.addCell(celda(fechaHora(f.fecha()), Element.ALIGN_LEFT, fondo, null));
                devoluciones.addCell(celda(soles(f.monto()), Element.ALIGN_RIGHT, fondo, null));
                devoluciones.addCell(celda(f.motivo(), Element.ALIGN_LEFT, fondo, null));
                devoluciones.addCell(celda(f.estado(), Element.ALIGN_LEFT, fondo, colorEstado(f.estado())));
            }
            documento.add(devoluciones);
        });
    }

    private static BigDecimal montoDe(List<ReporteDatos.ConteoEstado> conteos, String estado) {
        return conteos.stream().filter(c -> c.estado().equals(estado))
                .map(ReporteDatos.ConteoEstado::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public byte[] generarMembresias(ReporteDatos.Membresias datos) {
        return construir("Reporte de membresías", datos.generadoEn(), datos.generadoPor(), documento -> {
            documento.add(resumen(
                    caja("Total de membresías", String.valueOf(datos.total()), ROJO),
                    cajaEstado(datos.porEstado(), "PENDIENTE", "Pendientes"),
                    cajaEstado(datos.porEstado(), "ACTIVA", "Activas"),
                    cajaEstado(datos.porEstado(), "VENCIDA", "Vencidas"),
                    cajaEstado(datos.porEstado(), "CANCELADA", "Canceladas")));

            documento.add(subtitulo("Detalle de membresías"));
            PdfPTable tabla = tabla(new float[] {6, 22, 9, 10, 15, 9, 9, 10, 10},
                    "ID", "Cliente", "DNI", "Plan", "Promoción", "Inicio", "Fin", "Precio final", "Estado");
            if (datos.filas().isEmpty()) {
                sinDatos(tabla, 9, "No hay membresías registradas.");
            }
            int n = 0;
            for (ReporteDatos.FilaMembresia f : datos.filas()) {
                Color fondo = (n++ % 2 == 1) ? ZEBRA : Color.WHITE;
                tabla.addCell(celda(f.id(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.cliente(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.dni(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.plan(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(f.promocion(), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(fecha(f.inicio()), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(fecha(f.fin()), Element.ALIGN_LEFT, fondo, null));
                tabla.addCell(celda(soles(f.precioFinal()), Element.ALIGN_RIGHT, fondo, null));
                tabla.addCell(celda(f.estado(), Element.ALIGN_LEFT, fondo, colorEstado(f.estado())));
            }
            documento.add(tabla);
        });
    }


    @FunctionalInterface
    private interface Contenido {
        void dibujar(Document documento) throws DocumentException;
    }

    private byte[] construir(String titulo, LocalDateTime generadoEn, String generadoPor, Contenido contenido) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Rectangle hoja = new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth());
        Document documento = new Document(hoja, 36, 36, 30, 48);
        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            escritor.setPageEvent(new PieDePagina(titulo));
            documento.addTitle(titulo + " - ControlFit");
            documento.addAuthor("ControlFit");
            documento.addCreator("ControlFit");
            documento.open();

            documento.add(cabecera(titulo));
            documento.add(espacio(6));
            documento.add(new Paragraph("Generado el " + fechaHora(generadoEn)
                    + (generadoPor == null || generadoPor.isBlank() ? "" : " por " + generadoPor),
                    fuente(8.5f, Font.NORMAL, GRIS)));
            documento.add(espacio(8));

            contenido.dibujar(documento);
            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF: " + titulo, e);
        }
        return salida.toByteArray();
    }

    private PdfPTable cabecera(String titulo) throws DocumentException {
        PdfPTable banda = new PdfPTable(2);
        banda.setWidthPercentage(100);
        banda.setWidths(new float[] {1, 1});

        Phrase marca = new Phrase();
        marca.add(new Chunk("CONTROL", fuente(22, Font.BOLD, Color.WHITE)));
        marca.add(new Chunk("FIT", fuente(22, Font.BOLD, ROJO)));
        PdfPCell izquierda = new PdfPCell(marca);
        izquierda.setBackgroundColor(OSCURO);
        izquierda.setBorder(Rectangle.NO_BORDER);
        izquierda.setPadding(14);
        izquierda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        banda.addCell(izquierda);

        PdfPCell derecha = new PdfPCell(new Phrase(titulo.toUpperCase(Locale.ROOT), fuente(14, Font.BOLD, Color.WHITE)));
        derecha.setBackgroundColor(OSCURO);
        derecha.setBorder(Rectangle.NO_BORDER);
        derecha.setPadding(14);
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        derecha.setVerticalAlignment(Element.ALIGN_MIDDLE);
        banda.addCell(derecha);

        PdfPCell linea = new PdfPCell(new Phrase(" ", fuente(2, Font.NORMAL, ROJO)));
        linea.setColspan(2);
        linea.setBackgroundColor(ROJO);
        linea.setBorder(Rectangle.NO_BORDER);
        linea.setFixedHeight(3);
        banda.addCell(linea);
        return banda;
    }

    private PdfPTable resumen(PdfPCell... cajas) throws DocumentException {
        PdfPTable fila = new PdfPTable(cajas.length);
        fila.setWidthPercentage(100);
        for (PdfPCell c : cajas) {
            fila.addCell(c);
        }
        return fila;
    }

    private PdfPCell caja(String etiqueta, String valor, Color colorValor) {
        Phrase contenido = new Phrase();
        contenido.add(new Chunk(etiqueta + "\n", fuente(8, Font.NORMAL, GRIS)));
        contenido.add(new Chunk(valor, fuente(16, Font.BOLD, colorValor)));
        PdfPCell celda = new PdfPCell(contenido);
        celda.setBackgroundColor(CAJA);
        celda.setBorderColor(BORDE);
        celda.setBorderWidth(0.8f);
        celda.setPadding(9);
        celda.setLeading(0, 1.25f);
        return celda;
    }

    private PdfPCell cajaEstado(List<ReporteDatos.ConteoEstado> conteos, String estado, String etiqueta) {
        int cantidad = conteos.stream().filter(c -> c.estado().equals(estado))
                .mapToInt(ReporteDatos.ConteoEstado::cantidad).sum();
        return caja(etiqueta, String.valueOf(cantidad), colorEstado(estado));
    }

    private Paragraph nota(String texto) {
        Paragraph p = new Paragraph(texto, fuente(7.5f, Font.ITALIC, GRIS));
        p.setSpacingBefore(5);
        return p;
    }

    private Paragraph subtitulo(String texto) {
        Paragraph p = new Paragraph(texto, fuente(11, Font.BOLD, TEXTO));
        p.setSpacingBefore(14);
        p.setSpacingAfter(6);
        return p;
    }

    private Paragraph espacio(float alto) {
        Paragraph p = new Paragraph(" ", fuente(alto, Font.NORMAL, Color.WHITE));
        p.setLeading(alto);
        return p;
    }

    private PdfPTable tabla(float[] anchos, String... encabezados) throws DocumentException {
        PdfPTable t = new PdfPTable(anchos.length);
        t.setWidthPercentage(100);
        t.setWidths(anchos);
        for (String e : encabezados) {
            PdfPCell c = new PdfPCell(new Phrase(e, fuente(8.5f, Font.BOLD, Color.WHITE)));
            c.setBackgroundColor(ROJO);
            c.setBorderColor(ROJO);
            c.setPadding(6);
            c.setHorizontalAlignment(("Monto".equals(e) || "Devuelto".equals(e) || "Importe".equals(e) || "Precio final".equals(e))
                    ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            t.addCell(c);
        }
        t.setHeaderRows(1);       
        t.setSplitLate(false);
        return t;
    }

    private PdfPCell celda(String texto, int alineacion, Color fondo, Color colorTexto) {
        boolean resaltar = colorTexto != null;
        PdfPCell c = new PdfPCell(new Phrase(texto == null || texto.isEmpty() ? "-" : texto,
                fuente(8, resaltar ? Font.BOLD : Font.NORMAL, resaltar ? colorTexto : TEXTO)));
        c.setBackgroundColor(fondo);
        c.setBorderColor(BORDE);
        c.setBorderWidth(0.5f);
        c.setPadding(5);
        c.setHorizontalAlignment(alineacion);
        return c;
    }

    private void sinDatos(PdfPTable tabla, int columnas, String mensaje) {
        PdfPCell c = new PdfPCell(new Phrase(mensaje, fuente(9, Font.ITALIC, GRIS)));
        c.setColspan(columnas);
        c.setBorderColor(BORDE);
        c.setPadding(14);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(c);
    }

    private static Font fuente(float tamano, int estilo, Color color) {
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }

    private static Color colorEstado(String estado) {
        if (estado == null) {
            return TEXTO;
        }
        return switch (estado) {
            case "PAGADO", "ACTIVA", "COMPLETADA" -> VERDE;
            case "PENDIENTE" -> AMBAR;
            case "ANULADO", "ANULADA", "CANCELADA" -> ROJO_OSCURO;
            default -> GRIS;   
        };
    }


    private static String soles(BigDecimal monto) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "S/ " + formato.format(monto == null ? BigDecimal.ZERO : monto);
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? "-" : FECHA.format(fecha);
    }

    private static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "-" : FECHA_HORA.format(fecha);
    }

    private static final class PieDePagina extends PdfPageEventHelper {

        private final String titulo;
        private BaseFont base;
        private PdfTemplate totalPaginas;

        PieDePagina(String titulo) {
            this.titulo = titulo;
        }

        @Override
        public void onOpenDocument(PdfWriter escritor, Document documento) {
            try {
                base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (DocumentException | IOException e) {
                throw new IllegalStateException(e);
            }
            totalPaginas = escritor.getDirectContent().createTemplate(30, 12);
        }

        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            PdfContentByte lienzo = escritor.getDirectContent();
            float izquierda = documento.leftMargin();
            float derecha = documento.getPageSize().getWidth() - documento.rightMargin();
            float y = documento.bottomMargin() - 22;

            lienzo.setColorStroke(BORDE);
            lienzo.setLineWidth(0.6f);
            lienzo.moveTo(izquierda, y + 12);
            lienzo.lineTo(derecha, y + 12);
            lienzo.stroke();

            lienzo.beginText();
            lienzo.setFontAndSize(base, 8);
            lienzo.setColorFill(GRIS);
            lienzo.setTextMatrix(izquierda, y);
            lienzo.showText("ControlFit  ·  " + titulo);
            lienzo.endText();

            String texto = "Página " + escritor.getPageNumber() + " de ";
            float anchoTexto = base.getWidthPoint(texto, 8);
            float xTotal = derecha - 12;
            lienzo.beginText();
            lienzo.setFontAndSize(base, 8);
            lienzo.setColorFill(GRIS);
            lienzo.setTextMatrix(xTotal - anchoTexto, y);
            lienzo.showText(texto);
            lienzo.endText();
            lienzo.addTemplate(totalPaginas, xTotal, y);
        }

        @Override
        public void onCloseDocument(PdfWriter escritor, Document documento) {
            totalPaginas.beginText();
            totalPaginas.setFontAndSize(base, 8);
            totalPaginas.setColorFill(GRIS);
            totalPaginas.setTextMatrix(0, 0);
            totalPaginas.showText(String.valueOf(escritor.getPageNumber() - 1));
            totalPaginas.endText();
        }
    }
}
