package proyectoNetBanking.dto.pagos;

import proyectoNetBanking.domain.transacciones.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponsePagoExpresoDTO(
        Long transaccionId,
        TipoTransaccion tipoTransaccion,
        LocalDateTime fechaTransaccion,
        Long cuentaOrigenId,
        String cuentaDestino,
        BigDecimal montoPago,
        String mensaje
){
}
