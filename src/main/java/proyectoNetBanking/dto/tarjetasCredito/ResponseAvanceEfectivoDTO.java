package proyectoNetBanking.dto.tarjetasCredito;

import proyectoNetBanking.domain.transacciones.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseAvanceEfectivoDTO(
        Long transaccionId,
        TipoTransaccion tipoTransaccion,
        LocalDateTime fechaTransaccion,
        Long tarjetaId,
        Long cuentaAhorroId,
        BigDecimal montoAvanceEfectivo,
        String mensaje
) {
}
