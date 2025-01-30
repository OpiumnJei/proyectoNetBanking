package proyectoNetBanking.domain.pagos;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.beneficiarios.Beneficiario;
import proyectoNetBanking.domain.beneficiarios.BeneficiarioRepository;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.BeneficiarioNotFoundException;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;

import java.math.BigDecimal;

@Service
public class PagoBeneficiarioService {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Transactional
    public Transaccion realizarPagoBeneficiario(Long idBeneficiario, @Valid DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO) {

        Beneficiario beneficiario = obtenerBeneficiario(idBeneficiario);

        String numCuenta = beneficiario.getNumCuentaBeneficiario();

        //verificar que la cuenta del beneficiario exista

        CuentaAhorro cuentaBeneficiario = obtenerCuentaAhorroBeneficiario(numCuenta);

        Long cuentaOrigen = datosPagoBeneficiarioDTO.idcuentaOrigen();

        //verificar cuenta origen
        CuentaAhorro cuentaUsuario = obtenerCuentaAhorroUsuario(cuentaOrigen);

        BigDecimal montoPago = datosPagoBeneficiarioDTO.montoPago();

        validarSaldoDisponible(cuentaUsuario, montoPago); //verificar que exista saldo disponible para la transferencia

        //guardar el pago
        registrarPagoBeneficiario(cuentaBeneficiario, cuentaUsuario, montoPago);

        return transaccionService.registrarTransaccion(
                TipoTransaccion.PAGO_BENEFICIARIO,
                cuentaUsuario,
                cuentaBeneficiario,
                null,
                null,
                montoPago,
                ("Se realizo un pago a un beneficiario")
        );
    }

    private void registrarPagoBeneficiario(CuentaAhorro cuentaBeneficiario, CuentaAhorro cuentaUsuario, BigDecimal montoPago) {
        //se le suma el monto a la cuenta del beneficiario
        cuentaBeneficiario.setSaldoDisponible(cuentaUsuario.getSaldoDisponible().add(montoPago));
        cuentaAhorroRepository.save(cuentaBeneficiario);

        //se le resta el monto pago a la cuenta usuario
        cuentaUsuario.setSaldoDisponible(cuentaUsuario.getSaldoDisponible().subtract(montoPago));
        cuentaAhorroRepository.save(cuentaUsuario);
    }

    //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
    private void validarSaldoDisponible(CuentaAhorro cuentaAhorro, BigDecimal montoPago) {
        if (cuentaAhorro.getSaldoDisponible().compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException("La cuenta no dispone de el saldo suficiente para realizar el pago.");
        }
    }

    private Beneficiario obtenerBeneficiario(Long idBeneficiario) {
        return beneficiarioRepository.findById(idBeneficiario)
                .orElseThrow(() -> new BeneficiarioNotFoundException("Beneficiario no encontrado"));
    }

    private CuentaAhorro obtenerCuentaAhorroBeneficiario(String numeroCuenta) {
        return cuentaAhorroRepository.findByIdProducto(numeroCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }

    private CuentaAhorro obtenerCuentaAhorroUsuario(Long idCuenta) {
        return cuentaAhorroRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta no ha sido encontrada."));
    }
}
