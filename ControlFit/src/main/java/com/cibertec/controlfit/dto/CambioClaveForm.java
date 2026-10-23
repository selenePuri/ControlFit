package com.cibertec.controlfit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioClaveForm {

    private String claveActual;
    private String claveNueva;
    private String claveConfirmar;
}
