package proyectoNetBanking.service.transacciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.repository.TransaccionRepository;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransaccionService {

    @Autowired
    private TransaccionRepository transaccionRepository;


    //metodo generico pa las transacciones
    public Transaccion registrarTransaccion(TipoTransaccion tipoTransaccion, CuentaAhorro cuentaOrigen, CuentaAhorro cuentaDestino, TarjetaCredito tarjetaCredito, Prestamo prestamo, BigDecimal montoTransaccion, String descripcionTransaccion) {
        Transaccion transaccion = Transaccion.builder()
                .tipoTransaccion(tipoTransaccion)
                .cuentaOrigen(cuentaOrigen)
                .cuentaDestino(cuentaDestino)
                .tarjetaCredito(tarjetaCredito)
                .prestamo(prestamo)
                .montoTransaccion(montoTransaccion)
                .descripcionTransaccion(descripcionTransaccion)
                .fecha(LocalDateTime.now()) //hora en la que se hizo la transaccion
                .build();
        return transaccionRepository.save(transaccion);
    }

}
