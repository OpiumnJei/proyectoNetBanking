package proyectoNetBanking.domain.cuentasAhorro;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder()
public record DatosCuentasAhorroDTO(
        @NotNull(message = "Se necesita el id del usuario")
        Long usuarioId,
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto inicial no puede ser menor a cero")
        BigDecimal montoCuenta,
        @NotBlank()
        String proposito
)
{
    //constructor para gestionar los datos proporcionados por el usuario
    public DatosCuentasAhorroDTO(Long usuarioId, BigDecimal montoCuenta, String proposito) {
        this.usuarioId = usuarioId;                            //BigDecimal = 0
        this.montoCuenta = montoCuenta != null ? montoCuenta : BigDecimal.ZERO; //expresion ternaria encargada de verificar si el monto enviado es diferente de nulo
        this.proposito = proposito != null && !proposito.isBlank() ? proposito : "No especificado";
    }

}
