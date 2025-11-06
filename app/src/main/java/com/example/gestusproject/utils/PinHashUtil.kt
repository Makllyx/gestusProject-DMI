package com.example.gestusproject.utils

import java.security.MessageDigest

/**
 * Utility class para hashear PINs usando SHA-256
 * 
 * Esta clase proporciona funciones seguras para:
 * 1. Hashear un PIN de 4 dígitos usando SHA-256
 * 2. Validar un PIN ingresado contra un hash almacenado
 * 
 * Explicación de seguridad:
 * - SHA-256 es una función hash criptográfica unidireccional
 * - No se puede recuperar el PIN original desde el hash
 * - El mismo PIN siempre produce el mismo hash
 */
object PinHashUtil {
    
    /**
     * Hashea un PIN usando SHA-256
     * 
     * @param pin El PIN de 4 dígitos a hashear
     * @return El hash hexadecimal del PIN, o null si hay error
     * 
     * Ejemplo de uso:
     * val hash = PinHashUtil.hashPin("1234")
     * // Resultado: "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
     */
    fun hashPin(pin: String): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
            
            // Convertir bytes a hexadecimal
            val hexString = StringBuilder()
            for (byte in hashBytes) {
                val hex = Integer.toHexString(0xff and byte.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            
            hexString.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Valida un PIN ingresado contra un hash almacenado
     * 
     * @param pin El PIN ingresado por el usuario
     * @param storedHash El hash almacenado en la base de datos
     * @return true si el PIN coincide con el hash, false en caso contrario
     * 
     * Explicación línea por línea:
     * 1. Hashea el PIN ingresado
     * 2. Compara el hash resultante con el hash almacenado
     * 3. Retorna true solo si coinciden exactamente
     */
    fun validatePin(pin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrEmpty()) {
            return false
        }
        
        val pinHash = hashPin(pin)
        return pinHash != null && pinHash == storedHash
    }
    
    /**
     * Valida que un PIN tenga exactamente 4 dígitos numéricos
     * 
     * @param pin El PIN a validar
     * @return true si el PIN es válido (4 dígitos), false en caso contrario
     */
    fun isValidPinFormat(pin: String): Boolean {
        return pin.matches(Regex("^[0-9]{4}$"))
    }
}

