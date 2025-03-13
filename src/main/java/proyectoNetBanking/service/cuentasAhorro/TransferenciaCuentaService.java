package proyectoNetBanking.service.cuentasAhorro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.dto.cuentasAhorro.ResponseTransferenciaDTO;
import proyectoNetBanking.infra.errors.CuentaInactivaException;
import proyectoNetBanking.infra.errors.EstadoProductoNotFoundException;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.dto.cuentasAhorro.DatosTransferenciaCuentaDTO;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.service.transacciones.TransaccionService;

import java.math.BigDecimal;

@Service
public class TransferenciaCuentaService {

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    public ResponseTransferenciaDTO realizarTransferenciaEnCuentas(DatosTransferenciaCuentaDTO datosTransferenciaDTO) {

        CuentaAhorro cuentaOrigen = obtenerCuentaAhorro(datosTransferenciaDTO.cuentaOrigenId());

        //validar si la cuenta se encuentra activa
        validarEstadoCuenta(cuentaOrigen);

        CuentaAhorro cuentaDestino = obtenerCuentaAhorro(datosTransferenciaDTO.cuentaDestinoId());

        //validar si la cuenta se encuentra activa
        validarEstadoCuenta(cuentaDestino);

        BigDecimal montoPago = datosTransferenciaDTO.montoTransferencia();

        validarSaldoDisponible(cuentaOrigen, datosTransferenciaDTO.montoTransferencia());

        registrarTransferencia(cuentaDestino, cuentaOrigen, datosTransferenciaDTO.montoTransferencia());

        Transaccion transaccion = transaccionService.registrarTransaccion(
                TipoTransaccion.TRANSFERENCIA,
                cuentaOrigen,
                cuentaDestino,
                null,
                null,
                montoPago,
                "Se realizo una transferencia entre cuentas"
        );

        return new ResponseTransferenciaDTO(
                transaccion.getId(),
                transaccion.getTipoTransaccion(),
                transaccion.getFecha(),
                transaccion.getCuentaOrigen().getId(),
                transaccion.getCuentaDestino().getId(),
                transaccion.getMontoTransaccion()
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

    private void validarEstadoCuenta(CuentaAhorro cuenta) {

        String estado = "Activo";
        EstadoProducto estadoActivo = estadoProductoRepository.findByNombreEstadoIgnoreCase(estado)
                .orElseThrow(() -> new EstadoProductoNotFoundException("Estado no encontrado"));

        boolean estadoCuentaAhorro = cuentaAhorroRepository.existsByIdAndEstadoProducto(cuenta.getId(), estadoActivo);

        if (!estadoCuentaAhorro) {
            throw new CuentaInactivaException("No se ha podido completar el pago, la cuenta con id: " + cuenta.getId() + " se encuentra inactiva.");
        }
    }

    //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
    private void validarSaldoDisponible(CuentaAhorro cuentaAhorro, BigDecimal montoPago) {
        if (cuentaAhorro.getSaldoDisponible().compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException("La cuenta no dispone de el saldo suficiente para realizar la transferencia.");
        }
    }

    private CuentaAhorro obtenerCuentaAhorro(Long idCuenta) {
        return cuentaAhorroRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }
}
