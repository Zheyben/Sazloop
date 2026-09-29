package com.sazloop.identidad.service;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** Codifica contraseñas con sal aleatoria y PBKDF2; permite verificar el acceso posterior. */
@Component
public class CodificadorContrasena {
    private static final int ITERACIONES = 210_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private final SecureRandom aleatorio = new SecureRandom();

    public String codificar(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        aleatorio.nextBytes(sal);
        byte[] hash = derivar(contrasena, sal, ITERACIONES);
        return "pbkdf2-sha256$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public boolean coincide(String contrasena, String valorGuardado) {
        if (contrasena == null || valorGuardado == null) {
            return false;
        }
        String[] partes = valorGuardado.split("\\$", -1);
        if (partes.length != 4 || !"pbkdf2-sha256".equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            if (iteraciones < 1) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(contrasena, sal, iteraciones));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] derivar(String contrasena, byte[] sal, int iteraciones) {
        char[] caracteres = contrasena.toCharArray();
        PBEKeySpec clave = new PBEKeySpec(caracteres, sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(clave).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo proteger la contraseña", ex);
        } finally {
            clave.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
