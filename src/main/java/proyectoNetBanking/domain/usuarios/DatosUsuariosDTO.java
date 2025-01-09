package proyectoNetBanking.domain.usuarios;

import jakarta.validation.constraints.*;

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
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto inicial no puede ser menor a cero")//se usa inclusive para asegurarnos se acepte el valor inicial como un cero
        Double montoInicial
) {
}
