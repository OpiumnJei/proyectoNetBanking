package proyectoNetBanking.domain.tarjetasCredito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.TarjetaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class TarjetaCreditoService {

    //monto minimo para el limite de credito
    private final BigDecimal MONTO_MINIMO_LIMITE_CREDITO = BigDecimal.valueOf(2000);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Transactional
    public void crearTarjetaCredito(Long usuarioId, DatosTarjetaDTO datosTarjetaDTO) {

        if (usuarioId == null || usuarioId <= 0) { //validar que numero no sea negativo ni nulo
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo ni un número negativo");
        }

        //verificar que el usuario exista en la bd
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException());

        //validar que el limite de credito sea mayor o igual 2000
        if (datosTarjetaDTO.limiteCredito().compareTo(MONTO_MINIMO_LIMITE_CREDITO) < 0) {
            throw new RuntimeException("El limite de credito introducido es menor al monto minimo aceptado.");
        }

        asignarTarjetaCredito(usuario, datosTarjetaDTO.limiteCredito());
    }

    private void asignarTarjetaCredito(Usuario usuario, BigDecimal limiteCredito) {

        TarjetaCredito tarjetaUsuario = new TarjetaCredito();
        tarjetaUsuario.setLimiteCredito(limiteCredito);
        tarjetaUsuario.setCreditoDisponible(limiteCredito);
        tarjetaUsuario.setUsuario(usuario);
        tarjetaRepository.save(tarjetaUsuario);
    }

    //pago de tarjeta de credito
    public void pagarTarjetaCredito(Long tarjetaId, DatosPagoTarjetaDTO datosPagoTarjetaDTO) {

        //verificar que la tarjeta de credito exista
        TarjetaCredito tarjetaCredito = tarjetaRepository.findById(tarjetaId)
                .orElseThrow(() -> new TarjetaNotFoundException("Tarjeta de credito no encontrada"));

        // Verificar que el saldo disponible en la cuenta sea suficiente para el pago
        CuentaAhorro cuentaAhorro = cuentaAhorroRepository.findById(datosPagoTarjetaDTO.cuentaId())
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta de ahorro no existe."));


        BigDecimal montoPago = datosPagoTarjetaDTO.montoPago(); //cantidad a pagar

        //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
        if (cuentaAhorro.getSaldoDisponible().compareTo(montoPago) < 0) {
            throw new RuntimeException("La cuenta no dispone de el saldo suficiente para realizar el pago.");
        }

        BigDecimal saldoPorPagar = tarjetaCredito.getSaldoPorPagar(); //saldo a pagar
        if (montoPago.compareTo(saldoPorPagar) > 0) {//si la cantidad a pagar es mayor que el saldo a pagar

            //se paga lo que se debe lo demás no se usa
            montoPago = saldoPorPagar; //la cantidad a pagar sera igual al saldo a pagar

        }

        registrarPagoTarjeta(tarjetaCredito, cuentaAhorro, montoPago);
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

    //funcionalidad avance de efectivo
    public void realizarAvanceEfectivo(AvanceEfectivoDTO avanceEfectivoDTO) {

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
    }

    private void actualizarSaldoPorPagar(TarjetaCredito tarjetaCredito, BigDecimal montoAvanceEfectivo, BigDecimal deudaTotal) {

        tarjetaCredito.setCreditoDisponible(tarjetaCredito.getCreditoDisponible().subtract(montoAvanceEfectivo)); //restar el avance de credito al credito disponible
        tarjetaCredito.setSaldoPorPagar(tarjetaCredito.getSaldoPorPagar().add(deudaTotal)); //sumar el monto tomado + intereses
        tarjetaRepository.save(tarjetaCredito);
    }

}
