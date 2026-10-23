package com.cibertec.controlfit.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegacyAwarePasswordEncoderTest {

    private final LegacyAwarePasswordEncoder encoder = new LegacyAwarePasswordEncoder();

    @Test
    void codificaConBcryptYLoVerifica() {
        String hash = encoder.encode("Clave123");

        assertTrue(hash.startsWith("$2"));
        assertNotEquals("Clave123", hash);
        assertTrue(encoder.matches("Clave123", hash));
        assertFalse(encoder.matches("otra", hash));
        assertFalse(encoder.upgradeEncoding(hash));
    }

    @Test
    void aceptaClaveHeredadaEnTextoPlanoYPideMigrarla() {
        assertTrue(encoder.matches("plano123", "plano123"));
        assertFalse(encoder.matches("incorrecta", "plano123"));
        assertTrue(encoder.upgradeEncoding("plano123"));
    }

    @Test
    void rechazaValoresNulosOVacios() {
        assertFalse(encoder.matches(null, "x"));
        assertFalse(encoder.matches("x", null));
        assertFalse(encoder.matches("x", ""));
    }
}
