package com.cibertec.controlfit.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cibertec.controlfit.service.ReporteService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCIONISTA')")
@RequiredArgsConstructor
public class ReporteController extends ControllerSupport {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final ReporteService reporteService;
    private final Clock clock;

    @GetMapping
    public String index(Authentication autenticacion, Model model) {
        boolean esAdmin = autenticacion.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("rolEtiqueta", esAdmin ? "ADMIN" : "RECEPCIÓN");
        model.addAttribute("rolIcono", esAdmin ? "fa-shield-halved" : "fa-headset");
        model.addAttribute("pagos", reporteService.datosPagos(autenticacion.getName()));
        model.addAttribute("membresias", reporteService.datosMembresias(autenticacion.getName()));
        return "reportes/index";
    }

    @GetMapping("/pagos.pdf")
    public String pagosPdf(Authentication autenticacion, HttpServletResponse respuesta,
                           RedirectAttributes ra) throws IOException {
        return descargar("pagos", autenticacion, respuesta, ra, reporteService::generarPdfPagos);
    }

    @GetMapping("/membresias.pdf")
    public String membresiasPdf(Authentication autenticacion, HttpServletResponse respuesta,
                                RedirectAttributes ra) throws IOException {
        return descargar("membresias", autenticacion, respuesta, ra, reporteService::generarPdfMembresias);
    }

    private String descargar(String nombre, Authentication autenticacion, HttpServletResponse respuesta,
                             RedirectAttributes ra, Function<String, byte[]> generador) throws IOException {
        byte[] pdf;
        try {
            pdf = generador.apply(autenticacion.getName());
        } catch (RuntimeException e) {
            log.error("No se pudo generar el reporte de {}", nombre, e);
            error(ra, "No se pudo generar el reporte. Intenta nuevamente.");
            return "redirect:/reportes";
        }

        String archivo = "reporte-" + nombre + "-" + SELLO.format(LocalDateTime.now(clock)) + ".pdf";
        respuesta.setContentType(MediaType.APPLICATION_PDF_VALUE);
        respuesta.setContentLength(pdf.length);
        respuesta.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(archivo, StandardCharsets.UTF_8).build().toString());
        respuesta.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        respuesta.getOutputStream().write(pdf);
        log.info("Reporte de {} descargado por {} ({} bytes)", nombre, autenticacion.getName(), pdf.length);
        return null;  
    }
}
