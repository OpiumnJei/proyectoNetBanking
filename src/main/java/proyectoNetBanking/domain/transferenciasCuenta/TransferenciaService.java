package proyectoNetBanking.domain.transferenciasCuenta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;

import java.math.BigDecimal;

@Service
public class TransferenciaService {

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private TransaccionService transaccionService;

    public Transaccion realizarTransferenciaEnCuentas(DatosTransferenciaDTO datosTransferenciaDTO) {

        CuentaAhorro cuentaOrigen = obtenerCuentaAhorro(datosTransferenciaDTO.idCuentaOrigen());
        CuentaAhorro cuentaDestino = obtenerCuentaAhorro(datosTransferenciaDTO.idCuentaDestino());

        BigDecimal montoPago = datosTransferenciaDTO.montoPago();

        validarSaldoDisponible(cuentaOrigen, datosTransferenciaDTO.montoPago());

        registrarTransferencia(cuentaDestino, cuentaOrigen, datosTransferenciaDTO.montoPago());

        return transaccionService.registrarTransaccion(
                TipoTransaccion.TRANSFERENCIA,
                cuentaOrigen,
                cuentaDestino,
                null,
                null,
                montoPago,
                "Se realizo una transferencia entre cuentas"
        );

    }

    private void registrarTransferencia(CuentaAhorro cuentaDestino, CuentaAhorro cuentaOrigen, BigDecimal montoPago) {
        //se le suma el monto a la cuenta del beneficiario
        cuentaDestino.setSaldoDisponible(cuentaDestino.getSaldoDisponible().add(montoPago));
        cuentaAhorroRepository.save(cuentaDestino);

        //se le resta el monto pago a la cuenta usuario
        cuentaOrigen.setSaldoDisponible(cuentaOrigen.getSaldoDisponible().subtract(montoPago));
        cuentaAhorroRepository.save(cuentaOrigen);
    }


    //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
    private void validarSaldoDisponible(CuentaAhorro cuentaAhorro, BigDecimal montoPago) {
        if (cuentaAhorro.getSaldoDisponible().compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException("La cuenta no dispone de el saldo suficiente para realizar el pago.");
        }
    }


    private CuentaAhorro obtenerCuentaAhorro(Long idCuenta) {
        return cuentaAhorroRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }


}
