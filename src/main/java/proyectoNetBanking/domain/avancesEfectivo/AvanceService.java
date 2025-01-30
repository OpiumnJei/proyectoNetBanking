package proyectoNetBanking.domain.avancesEfectivo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.transacciones.TransaccionService;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.TarjetaNotFoundException;

import java.math.BigDecimal;

@Service
public class AvanceService {

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
        TarjetaCredito tarjetaCredito = tarjetaRepository.findById(avanceEfectivoDTO.tarjetaCreditoId())
                .orElseThrow(() -> new TarjetaNotFoundException("Tarjeta de crédito no encontrada."));

        // Validar que la cuenta de ahorro exista
        CuentaAhorro cuentaAhorro = cuentaAhorroRepository.findById(avanceEfectivoDTO.cuentaAhorroId())
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta de ahorro no existe."));

        BigDecimal montoAvanceEfectivo = avanceEfectivoDTO.montoAvanceEfectivo();//monto solicitado por el usuario

        if (montoAvanceEfectivo.compareTo(tarjetaCredito.getCreditoDisponible()) > 0) {// si el monto supera el credito disponible en la tarjeta
            throw new RuntimeException("El monto del avance supera el crédito disponible de la tarjeta.");
        }

        // Calcular el monto total con interés
        BigDecimal interes = montoAvanceEfectivo.multiply(BigDecimal.valueOf(0.0625));// 6.25 % del monto tomado como avance
        BigDecimal deudaTotal = montoAvanceEfectivo.add(interes);

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

    private void actualizarSaldoPorPagar(TarjetaCredito tarjetaCredito, BigDecimal montoAvanceEfectivo, BigDecimal deudaTotal) {

        tarjetaCredito.setCreditoDisponible(tarjetaCredito.getCreditoDisponible().subtract(montoAvanceEfectivo)); //restar el avance de credito al credito disponible
        tarjetaCredito.setSaldoPorPagar(tarjetaCredito.getSaldoPorPagar().add(deudaTotal)); //sumar el monto tomado + intereses
        tarjetaRepository.save(tarjetaCredito);
    }
}
