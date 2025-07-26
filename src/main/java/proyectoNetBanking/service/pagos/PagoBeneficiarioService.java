package proyectoNetBanking.service.pagos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.beneficiarios.Beneficiario;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.dto.pagos.DatosPagoBeneficiarioDTO;
import proyectoNetBanking.dto.pagos.ResponsePagoBeneficiarioDTO;
import proyectoNetBanking.infra.errors.*;
import proyectoNetBanking.repository.BeneficiarioRepository;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.service.transacciones.TransaccionService;

import java.math.BigDecimal;

@Service
public class PagoBeneficiarioService {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    /**
     *  Realizar pago beneficiario
     * @param beneficiarioId - id del beneficiario al que se le realizara el pago
     *
     * */

    @Transactional
    public ResponsePagoBeneficiarioDTO realizarPagoBeneficiario(Long beneficiarioId, DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO) {

        Beneficiario beneficiario = obtenerBeneficiario(beneficiarioId);

        String numCuenta = beneficiario.getNumCuentaBeneficiario();

        //verificar que la cuenta del beneficiario exista
        CuentaAhorro cuentaBeneficiario = obtenerCuentaAhorroBeneficiario(numCuenta);

        validarCuentaBeneficiarioActiva(cuentaBeneficiario);

        Long cuentaOrigen = datosPagoBeneficiarioDTO.cuentaOrigenId();

        //verificar cuenta origen
        CuentaAhorro cuentaUsuario = obtenerCuentaAhorroUsuario(cuentaOrigen);

        validarCuentaOrigenActiva(cuentaUsuario);

        //validar que el usuario que desea realizar el pago tenga al beneficiario previamente agregado
        validarBeneficiarioUsuario(beneficiario, cuentaUsuario);

        BigDecimal montoPago = datosPagoBeneficiarioDTO.montoPago();

        validarSaldoDisponible(cuentaUsuario, montoPago); //verificar que exista saldo disponible para la transferencia

        //guardar el pago
        registrarPagoBeneficiario(cuentaBeneficiario, cuentaUsuario, montoPago);

        Transaccion transaccion = transaccionService.registrarTransaccion(
                TipoTransaccion.PAGO_BENEFICIARIO,
                cuentaUsuario,
                cuentaBeneficiario,
                null,
                null,
                montoPago,
                ("Se realizo un pago a un beneficiario")
        );

        return new ResponsePagoBeneficiarioDTO(
                transaccion.getId(),
                transaccion.getTipoTransaccion(),
                transaccion.getFecha(),
                transaccion.getCuentaOrigen().getId(),
                transaccion.getCuentaDestino().getId(),
                transaccion.getMontoTransaccion(),
                "El pago al beneficiario se realizo correctamente."
        );
    }

    private void registrarPagoBeneficiario(CuentaAhorro cuentaBeneficiario, CuentaAhorro cuentaUsuario, BigDecimal montoPago) {
        //se le suma el monto a la cuenta del beneficiario
        cuentaBeneficiario.setSaldoDisponible(cuentaBeneficiario.getSaldoDisponible().add(montoPago));
        cuentaAhorroRepository.save(cuentaBeneficiario);

        //se le resta el monto pago a la cuenta usuario
        cuentaUsuario.setSaldoDisponible(cuentaUsuario.getSaldoDisponible().subtract(montoPago));
        cuentaAhorroRepository.save(cuentaUsuario);
    }

    // Asumiendo que el beneficiario tiene una referencia al usuario propietario
    private void validarBeneficiarioUsuario(Beneficiario beneficiario, CuentaAhorro cuentaUsuario) {
        Usuario usuarioDelBeneficiario = beneficiario.getUsuario();
        Usuario usuarioDeLaCuenta = cuentaUsuario.getUsuario();

        // Verifica si alguno de los usuarios es nulo o si sus IDs no coinciden
        if (usuarioDelBeneficiario == null || usuarioDeLaCuenta == null || !usuarioDelBeneficiario.getId().equals(usuarioDeLaCuenta.getId())) {
            throw new BeneficiarioNoPerteneceAUsuarioException("El beneficiario especificado no pertenece al usuario de la cuenta origen.");
        }
    }

    //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
    private void validarSaldoDisponible(CuentaAhorro cuentaAhorro, BigDecimal montoPago) {
        if (cuentaAhorro.getSaldoDisponible().compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException("La cuenta no dispone de el saldo suficiente para realizar el pago.");
        }
    }

    /**
     * Valida que la cuenta de origen esté activa
     *
     * @param cuentaUsuario La cuenta del usuario que realiza la operación
     * @throws CuentaInactivaException Si la cuenta está inactiva
     */
    private void validarCuentaOrigenActiva(CuentaAhorro cuentaUsuario) {
        // Asumiendo que la cuenta tiene un campo estadoProducto que es una entidad
        if (!cuentaUsuario.getEstadoProducto().getNombreEstado().equalsIgnoreCase("ACTIVO")) {
            throw new CuentaInactivaException("La cuenta de origen no está activa. No se pueden realizar operaciones.");
        }
    }

    /**
     * Valida que la cuenta del beneficiario este activa
     *
     * @param cuentaBeneficiario - la cuenta del beneficiario
     * @throws CuentaInactivaException - si al cuenta esta inactiva
     */
    private void validarCuentaBeneficiarioActiva(CuentaAhorro cuentaBeneficiario) {
        if (!cuentaBeneficiario.getEstadoProducto().getNombreEstado().equalsIgnoreCase("ACTIVO")) {
            throw new CuentaInactivaException("La cuenta del beneficiario no está activa. No se pueden realizar operaciones.");
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
