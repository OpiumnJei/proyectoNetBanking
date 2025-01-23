package proyectoNetBanking.domain.usuarios;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ActualizarDatosUsuarioDTO(
        @NotBlank
        String nuevoNombre,
        @NotBlank
        String nuevoApellido,
        @NotBlank
                @Size(max = 11, message = "La cedula no debe contener mas de 11 caracteres")
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
