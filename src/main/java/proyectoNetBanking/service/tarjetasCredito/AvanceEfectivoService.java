package proyectoNetBanking.service.tarjetasCredito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.dto.tarjetasCredito.AvanceEfectivoDTO;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.repository.TarjetaRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.service.transacciones.TransaccionService;
import proyectoNetBanking.repository.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.TarjetaNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AvanceEfectivoService {

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransaccionService transaccionService;

    // funcionalidad avance de efectivo
    @Transactional
    public Transaccion realizarAvanceEfectivo(AvanceEfectivoDTO avanceEfectivoDTO) {

        // Validar que la tarjeta de crédito exista
        TarjetaCredito tarjetaCredito = obtenerTarjetaCredito(avanceEfectivoDTO.tarjetaCreditoId());

        // Validar que la cuenta de ahorro exista
        CuentaAhorro cuentaAhorro = obtenerCuentaAhorro(avanceEfectivoDTO.cuentaAhorroId());

        BigDecimal montoAvanceEfectivo = avanceEfectivoDTO.montoAvanceEfectivo();//monto solicitado por el usuario

        validarMontoAvance(montoAvanceEfectivo, tarjetaCredito);

        // Calcular el monto total con interés
        BigDecimal deudaTotal = calcularMontoInteres(montoAvanceEfectivo);

        // Actualizar saldo de la cuenta de ahorro
        cuentaAhorro.setSaldoDisponible(cuentaAhorro.getSaldoDisponible().add(montoAvanceEfectivo));
        cuentaAhorroRepository.save(cuentaAhorro);

        // Actualizar deuda en la tarjeta de crédito
        actualizarSaldoPorPagar(tarjetaCredito, montoAvanceEfectivo, deudaTotal);

        return transaccionService.registrarTransaccion(
                TipoTransaccion.AVANCE_EFECTIVO,
                cuentaAhorro,
                null,
                tarjetaCredito,
                null,
                montoAvanceEfectivo,
                "Se realizo un avance de afectivo desde una tarjea de credito"
        );
    }

    private BigDecimal calcularMontoInteres(BigDecimal montoAvanceEfectivo) {
        BigDecimal interes =
                montoAvanceEfectivo
                        .multiply(BigDecimal
                                .valueOf(0.0625)) // 6.25 % del monto tomado como avance
                        .setScale(2, RoundingMode.HALF_UP); // redondeado a dos decimales hacia arriba

        //se retorna el monto total, montoAvanceEfectio + interes
        return montoAvanceEfectivo.add(interes);
    }

    private void validarMontoAvance(BigDecimal montoAvanceEfectivo, TarjetaCredito tarjetaCredito) {
        if (montoAvanceEfectivo.compareTo(tarjetaCredito.getCreditoDisponible()) > 0) {// si el monto supera el credito disponible en la tarjeta
            throw new RuntimeException("El monto del avance supera el crédito disponible de la tarjeta.");
        }
    }

    private TarjetaCredito obtenerTarjetaCredito(Long tarjetaId) {
        return tarjetaRepository.findById(tarjetaId)
                .orElseThrow(() -> new TarjetaNotFoundException("Tarjeta de crédito no encontrada."));
    }

    private CuentaAhorro obtenerCuentaAhorro(Long idCuenta) {
        return cuentaAhorroRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }

    private void actualizarSaldoPorPagar(TarjetaCredito tarjetaCredito, BigDecimal montoAvanceEfectivo, BigDecimal deudaTotal) {

        tarjetaCredito.setCreditoDisponible(tarjetaCredito.getCreditoDisponible().subtract(montoAvanceEfectivo)); //restar el avance de credito al credito disponible
        tarjetaCredito.setSaldoPorPagar(tarjetaCredito.getSaldoPorPagar().add(deudaTotal)); //sumar el monto tomado + intereses
        tarjetaRepository.save(tarjetaCredito);
    }
}
