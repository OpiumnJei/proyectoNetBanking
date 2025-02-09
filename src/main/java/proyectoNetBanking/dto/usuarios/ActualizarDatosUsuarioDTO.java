package proyectoNetBanking.dto.usuarios;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ActualizarDatosUsuarioDTO(
        @NotBlank
        String nuevoNombre,
        @NotBlank
        String nuevoApellido,
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "La cédula debe contener exactamente 11 dígitos numéricos.") //usamos una expresion regular para controlar la cantidad de digitos de la cedula
        String nuevaCedula,
        @NotBlank
        @Email(message = "El nuevoCorreo electronico es obligatorio")
        String nuevoCorreo,
        @NotBlank
        @Size(min = 5, message = "La contraseña debe tener al menos 5 caracteres.")
        String newPassword,
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto adicional no puede ser menor a cero")//se usa inclusive para asegurarnos se acepte el valor inicial como un cero
        BigDecimal montoAdicinal
) {
}
