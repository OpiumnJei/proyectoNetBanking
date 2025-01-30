package proyectoNetBanking.domain.pagos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

//Datos para pagar un prestamo
public record DatosPagoPrestamoDTO(
        @NotNull
        Long idUsuario,
        @NotNull
        Long idCuentaUsuario,
        @DecimalMin(value = "50.0", inclusive = true, message = "El monto minimo aceptado como forma de pago es de 50.0 DOP")
        BigDecimal montoPago
) {

}
