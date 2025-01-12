package proyectoNetBanking.domain.usuarios;

import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;

public record DatosUsuariosDTO(
        @NotBlank
        String nombre,
        @NotBlank
        String apellido,
        @NotBlank
        String cedula,
        @NotBlank
        @Email(message = "El correo electronico es obligatorio")
        String correo,
        @NotBlank
        String password,
        @NotEmpty
        Long tipoUsuarioId,
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto inicial no puede ser menor a cero")//se usa inclusive para asegurarnos se acepte el valor inicial como un cer
        @Value("0.0") //valor por defecto en caso de que no se especifique un monto inicial
        Double montoInicial
) {
}
