package proyectoNetBanking.domain.prestamos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosPrestamoDTO(

        @NotNull(message = "El id del usuario no puede estar vacio.")
        Long idUsuario,
        @DecimalMin(value = "1000.0", inclusive = true, message = "El monto minimo de un prestamo es de 1,000 DOP. ")
        BigDecimal montoPrestamo
) {

}
