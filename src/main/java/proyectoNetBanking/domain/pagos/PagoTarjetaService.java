package proyectoNetBanking.domain.pagos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.transacciones.TransaccionService;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;
import proyectoNetBanking.infra.errors.TarjetaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class PagoTarjetaService {

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private TransaccionService transaccionService;



    //pago de tarjeta de credito
    @Transactional
    public Transaccion realizarPagoTarjetaCredito(Long idTarjeta, DatosPagoTarjetaDTO datosPagoTarjetaDTO) {

        //verificar que la tarjeta de credito exista
        TarjetaCredito tarjetaCredito = obtenerTarjetaCredito(idTarjeta);

        CuentaAhorro cuentaAhorro = obtenerCuentaAhorroUsuario(datosPagoTarjetaDTO.cuentaId());

        BigDecimal montoPago = datosPagoTarjetaDTO.montoPago(); //cantidad a pagar

        validarSaldoDisponible(cuentaAhorro, montoPago); //validar que el  monto este disponible en la cuenta de ahorro

        //igualar el monto a pagar con el monto de la deuda
        ajustarMontoPago(tarjetaCredito, montoPago);

        //registar el pago
        registrarPagoTarjeta(tarjetaCredito, cuentaAhorro, montoPago);

        //registrar transaccion
        return transaccionService.registrarTransaccion(
                TipoTransaccion.PAGO_TARJETA,
                cuentaAhorro,
                null,
                tarjetaCredito,
                null,
                montoPago,
                "Se realizo un pago a la tarjeta de credito"
        );
    }

    private void registrarPagoTarjeta(TarjetaCredito tarjetaCredito, CuentaAhorro cuentaAhorro, BigDecimal montoPago) {

        cuentaAhorro.setSaldoDisponible(cuentaAhorro.getSaldoDisponible().subtract(montoPago)); //restar monto del pago
        cuentaAhorroRepository.save(cuentaAhorro);

        // Reducir la deuda pendiente de la tarjeta
        tarjetaCredito.setSaldoPorPagar(tarjetaCredito.getSaldoPorPagar().subtract(montoPago));

        // Ajustar el crédito disponible en la tarjeta
        tarjetaCredito.setCreditoDisponible(tarjetaCredito.getCreditoDisponible().add(montoPago));
        tarjetaRepository.save(tarjetaCredito);

    }

    //metodo gestiona la igualdad entre el monto pagado y el monto a pagar
    private void ajustarMontoPago(TarjetaCredito tarjetaCredito, BigDecimal montoPago) {

        BigDecimal saldoPorPagar = tarjetaCredito.getSaldoPorPagar(); //saldo a pagar

        if (montoPago.compareTo(saldoPorPagar) > 0) {//si la cantidad a pagar es mayor que el saldo a pagar
            //se paga lo que se debe lo demás no se usa
            montoPago = saldoPorPagar; //la cantidad a pagar sera igual al saldo a pagar

        }
    }

    private CuentaAhorro obtenerCuentaAhorroUsuario(Long idCuenta) {
        return cuentaAhorroRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }

    private TarjetaCredito obtenerTarjetaCredito(Long idTarjeta) {
        return tarjetaRepository.findById(idTarjeta)
                .orElseThrow(() -> new TarjetaNotFoundException("Tarjeta de credito no encontrada"));
    }


    //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
    private void validarSaldoDisponible(CuentaAhorro cuentaAhorro, BigDecimal montoPago) {
        if (cuentaAhorro.getSaldoDisponible().compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException("La cuenta no dispone de el saldo suficiente para realizar el pago.");
        }
    }
}
