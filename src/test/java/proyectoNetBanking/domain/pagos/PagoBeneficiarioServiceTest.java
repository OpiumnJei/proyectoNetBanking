package proyectoNetBanking.domain.pagos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyectoNetBanking.domain.beneficiarios.Beneficiario;
import proyectoNetBanking.dto.pagos.ResponsePagoBeneficiarioDTO;
import proyectoNetBanking.repository.BeneficiarioRepository;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.dto.pagos.DatosPagoBeneficiarioDTO;
import proyectoNetBanking.service.pagos.PagoBeneficiarioService;
import proyectoNetBanking.service.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.BeneficiarioNotFoundException;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoBeneficiarioServiceTest {

    @Mock
    private TransaccionService transaccionService;

    @Mock
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @InjectMocks
    private PagoBeneficiarioService pagoBeneficiarioService;

    @Test
    void testRealizarPagoBeneficiarioExitoso() {
        // Datos de prueba
        Long idBeneficiario = 1L;
        Long idCuentaUsuario = 2L;
        String numeroCuentaBeneficiario = "123456";
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO = new DatosPagoBeneficiarioDTO(idCuentaUsuario, montoPago);

        // Simular beneficiario
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(idBeneficiario);
        beneficiario.setNumCuentaBeneficiario(numeroCuentaBeneficiario);

        // Simular cuenta del beneficiario
        CuentaAhorro cuentaBeneficiario = new CuentaAhorro();
        cuentaBeneficiario.setIdProducto(numeroCuentaBeneficiario);
        cuentaBeneficiario.setSaldoDisponible(new BigDecimal("50.00"));

        // Simular cuenta del usuario
        CuentaAhorro cuentaUsuario = new CuentaAhorro();
        cuentaUsuario.setId(idCuentaUsuario);
        cuentaUsuario.setSaldoDisponible(new BigDecimal("200.00"));

        // Simular transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);

        // Configurar mocks
        when(beneficiarioRepository.findById(idBeneficiario)).thenReturn(Optional.of(beneficiario));
        when(cuentaAhorroRepository.findByIdProducto(numeroCuentaBeneficiario)).thenReturn(Optional.of(cuentaBeneficiario));
        when(cuentaAhorroRepository.findById(idCuentaUsuario)).thenReturn(Optional.of(cuentaUsuario));
        when(transaccionService.registrarTransaccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(transaccion);

        // Ejecutar el metodo
        ResponsePagoBeneficiarioDTO resultado = pagoBeneficiarioService.realizarPagoBeneficiario(idBeneficiario, datosPagoBeneficiarioDTO);

        // Verificar resultados
        assertNotNull(resultado);
        assertEquals(new BigDecimal("100.00"), cuentaUsuario.getSaldoDisponible()); // 200 - 100 = 100
        assertEquals(new BigDecimal("150.00"), cuentaBeneficiario.getSaldoDisponible()); // 50 + 100 = 150

        // Verificar que se llamaron los métodos simulados
        verify(beneficiarioRepository, times(1)).findById(idBeneficiario);
        verify(cuentaAhorroRepository, times(1)).findByIdProducto(numeroCuentaBeneficiario);
        verify(cuentaAhorroRepository, times(1)).findById(idCuentaUsuario);
        verify(transaccionService, times(1)).registrarTransaccion(
                TipoTransaccion.PAGO_BENEFICIARIO,
                cuentaUsuario,
                cuentaBeneficiario,
                null,
                null,
                montoPago,
                "Se realizo un pago a un beneficiario"
        );
    }

    @Test
    void testRealizarPagoBeneficiarioSaldoInsuficiente() {
        // Datos de prueba
        Long idBeneficiario = 1L;
        Long idCuentaUsuario = 2L;
        String numeroCuentaBeneficiario = "123456";
        BigDecimal montoPago = new BigDecimal("300.00"); //monto que se le enviara al beneficiario

        DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO = new DatosPagoBeneficiarioDTO(idCuentaUsuario, montoPago);

        // Simular beneficiario
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(idBeneficiario);
        beneficiario.setNumCuentaBeneficiario(numeroCuentaBeneficiario);

        // Simular cuenta del beneficiario
        CuentaAhorro cuentaBeneficiario = new CuentaAhorro();
        cuentaBeneficiario.setIdProducto(numeroCuentaBeneficiario);
        cuentaBeneficiario.setSaldoDisponible(new BigDecimal("50.00")); //saldo disponible en la cuenta origen

        // Simular cuenta del usuario con saldo insuficiente
        CuentaAhorro cuentaUsuario = new CuentaAhorro();
        cuentaUsuario.setId(idCuentaUsuario);
        cuentaUsuario.setSaldoDisponible(new BigDecimal("200.00"));

        // Configurar mocks
        when(beneficiarioRepository.findById(idBeneficiario)).thenReturn(Optional.of(beneficiario));
        when(cuentaAhorroRepository.findByIdProducto(numeroCuentaBeneficiario)).thenReturn(Optional.of(cuentaBeneficiario));
        when(cuentaAhorroRepository.findById(idCuentaUsuario)).thenReturn(Optional.of(cuentaUsuario));

        // Ejecutar el método y verificar excepción
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> {
                    pagoBeneficiarioService.realizarPagoBeneficiario(idBeneficiario, datosPagoBeneficiarioDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no dispone de el saldo suficiente para realizar el pago.", exception.getMessage());

        System.out.println(exception.getMessage());
        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarPagoBeneficiarioNoEncontrado() {
        // Datos de prueba
        Long idBeneficiario = 1L;
        Long idCuentaUsuario = 2L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO = new DatosPagoBeneficiarioDTO(idCuentaUsuario, montoPago);

        // Configurar mock para lanzar excepción
        when(beneficiarioRepository.findById(idBeneficiario)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        BeneficiarioNotFoundException exception = assertThrows(
                BeneficiarioNotFoundException.class,
                () -> {
                    pagoBeneficiarioService.realizarPagoBeneficiario(idBeneficiario, datosPagoBeneficiarioDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("Beneficiario no encontrado", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCuentaBeneficiarioNoEncontrada() {
        // Datos de prueba
        Long idBeneficiario = 1L;
        Long idCuentaUsuario = 2L;
        String numeroCuentaBeneficiario = "123456";
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO = new DatosPagoBeneficiarioDTO(idCuentaUsuario, montoPago);

        // Simular beneficiario
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(idBeneficiario);
        beneficiario.setNumCuentaBeneficiario(numeroCuentaBeneficiario);

        // Configurar mocks
        when(beneficiarioRepository.findById(idBeneficiario)).thenReturn(Optional.of(beneficiario));
        when(cuentaAhorroRepository.findByIdProducto(numeroCuentaBeneficiario)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        CuentaNotFoundException exception = assertThrows(
                CuentaNotFoundException.class,
                () -> {
                    pagoBeneficiarioService.realizarPagoBeneficiario(idBeneficiario, datosPagoBeneficiarioDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());


        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCuentaUsuarioNoEncontrada() {
        // Datos de prueba
        Long idBeneficiario = 1L;
        Long idCuentaUsuario = 2L;
        String numeroCuentaBeneficiario = "123456";
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoBeneficiarioDTO datosPagoBeneficiarioDTO = new DatosPagoBeneficiarioDTO(idCuentaUsuario, montoPago);

        // Simular beneficiario
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId(idBeneficiario);
        beneficiario.setNumCuentaBeneficiario(numeroCuentaBeneficiario);

        // Simular cuenta del beneficiario
        CuentaAhorro cuentaBeneficiario = new CuentaAhorro();
        cuentaBeneficiario.setIdProducto(numeroCuentaBeneficiario);
        cuentaBeneficiario.setSaldoDisponible(new BigDecimal("50.00"));

        // Configurar mocks
        when(beneficiarioRepository.findById(idBeneficiario)).thenReturn(Optional.of(beneficiario));
        when(cuentaAhorroRepository.findByIdProducto(numeroCuentaBeneficiario)).thenReturn(Optional.of(cuentaBeneficiario));
        when(cuentaAhorroRepository.findById(idCuentaUsuario)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        CuentaNotFoundException exception = assertThrows(
                CuentaNotFoundException.class,
                () -> {
                    pagoBeneficiarioService.realizarPagoBeneficiario(idBeneficiario, datosPagoBeneficiarioDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }
}