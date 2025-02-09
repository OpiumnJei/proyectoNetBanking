package proyectoNetBanking.dto.cuentasAhorro;

import jakarta.validation.constraints.NotNull;

//Datos enviados por el cliente para la eliminacion de cuentas
public record DatosEliminarCuentaDTO(
        @NotNull(message = "El id del usuario no puede estar vacio.")
        Long idUsuario,
        @NotNull(message = "El id de la cuenta no puede estar vacio.")
        Long idCuenta
) {
}
