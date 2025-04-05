package proyectoNetBanking.dto.pagos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

//Datos para pagar un prestamo
public record DatosPagoPrestamoDTO(
        @NotNull(message = "El id del usuario es un campo obligatorio.")
        Long usuarioId,
        @NotNull(message = "El id de la cuenta es un campo obligatorio.")
        Long cuentaUsuarioId,
        @NotNull(message = "Debe especificar el monto del pago ha realizar.")
        @DecimalMin(value = "50.0", inclusive = true, message = "El monto minimo aceptado como forma de pago es de 50.0 DOP")
        BigDecimal montoPago
) {

}
