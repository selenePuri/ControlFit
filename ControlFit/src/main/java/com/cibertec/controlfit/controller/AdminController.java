package com.cibertec.controlfit.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cibertec.controlfit.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("resumen", dashboardService.resumenAdmin());
        return "admin/dashboard";
    }
}
