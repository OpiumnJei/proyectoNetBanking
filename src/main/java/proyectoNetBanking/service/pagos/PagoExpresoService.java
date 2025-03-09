package proyectoNetBanking.service.pagos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.dto.pagos.ResponsePagoExpresoDTO;
import proyectoNetBanking.infra.errors.CuentaInactivaException;
import proyectoNetBanking.infra.errors.EstadoProductoNotFoundException;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.dto.pagos.DatosPagoExpresoDTO;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.service.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;

import java.math.BigDecimal;

@Service
public class PagoExpresoService {

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    public ResponsePagoExpresoDTO realizarPagoExpreso(DatosPagoExpresoDTO datosPagoExpresoDTO ){

        CuentaAhorro cuentaOrigen = obtenerCuentaAhorroOrigen(datosPagoExpresoDTO.cuentaOrigenId());

        validarEstadoCuentaOrigen(cuentaOrigen);

        CuentaAhorro cuentaDestino = obtenerCuentaAhorroDestino(datosPagoExpresoDTO.cuentaDestino());

        BigDecimal montoPago = datosPagoExpresoDTO.montoPago();

        validarSaldoDisponible(cuentaOrigen, datosPagoExpresoDTO.montoPago());
        registrarPago(cuentaDestino, cuentaOrigen, datosPagoExpresoDTO.montoPago());

        Transaccion transaccion = transaccionService.registrarTransaccion(
                TipoTransaccion.PAGO_EXPRESO,
                cuentaOrigen,
                cuentaDestino,
                null,
                null,
                montoPago,
                "Se realizo un pago expreso"
        );

        return new ResponsePagoExpresoDTO(
                transaccion.getId(),
                transaccion.getTipoTransaccion(),
                transaccion.getFecha(),
                transaccion.getCuentaOrigen().getId(),
                transaccion.getCuentaDestino().getIdProducto(),
                transaccion.getMontoTransaccion(),
                "El pago expreso se realizó correctamente"
        );
    }

    private void validarEstadoCuentaOrigen(CuentaAhorro cuentaOrigen) {

        String estado = "Activo";
        EstadoProducto estadoActivo = estadoProductoRepository.findByNombreEstadoIgnoreCase(estado)
                .orElseThrow(() -> new EstadoProductoNotFoundException("Estado no encontrado"));

        boolean estadoCuentaAhorro = cuentaAhorroRepository.existsByIdAndEstadoProducto(cuentaOrigen.getId(), estadoActivo);

        if(!estadoCuentaAhorro){
            throw new CuentaInactivaException("No se ha podido completar el pago, la cuenta con id: "+cuentaOrigen.getId()+" se encuentra inactiva.");
        }
    }


    private void registrarPago(CuentaAhorro cuentaDestino, CuentaAhorro cuentaOrigen, BigDecimal montoPago) {
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

    private CuentaAhorro obtenerCuentaAhorroDestino(String numeroCuenta) {
        return cuentaAhorroRepository.findByIdProducto(numeroCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }

    private CuentaAhorro obtenerCuentaAhorroOrigen(Long idCuenta) {
        return cuentaAhorroRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }
}
