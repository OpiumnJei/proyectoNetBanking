package proyectoNetBanking.dto.authentication;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "La cedula es un campo requerido")
        String cedula,
        @NotBlank(message = "La contraseña es un campo requerido")
        String password
) {
}
