package com.cibertec.controlfit.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class WebBindingAdvice {

    private static final String[] CAMPOS_TEXTO = {
        "dni", "nombres", "apellidos", "telefono", "correo",
        "usuario", "nombre", "descripcion"
    };

    @InitBinder
    public void recortarTextos(WebDataBinder binder) {
        StringTrimmerEditor editor = new StringTrimmerEditor(true);
        for (String campo : CAMPOS_TEXTO) {
            binder.registerCustomEditor(String.class, campo, editor);
        }
    }
}
