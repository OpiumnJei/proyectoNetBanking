package proyectoNetBanking.domain.pagos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.dto.pagos.ResponsePagoTarjetaDTO;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.repository.TarjetaRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.dto.pagos.DatosPagoTarjetaDTO;
import proyectoNetBanking.service.pagos.PagoTarjetaService;
import proyectoNetBanking.service.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.SaldoInsuficienteException;
import proyectoNetBanking.infra.errors.TarjetaNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoTarjetaServiceTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Mock
    private TransaccionService transaccionService;

    @InjectMocks
    private PagoTarjetaService pagoTarjetaService;

    @Test
    void testRealizarPagoTarjetaCreditoExitoso() {
        // Datos de prueba
        Long idTarjeta = 1L;
        Long idCuenta = 2L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoTarjetaDTO datosPagoTarjetaDTO = new DatosPagoTarjetaDTO(idCuenta, montoPago);

        // Simular tarjeta de crédito
        TarjetaCredito tarjetaCredito = new TarjetaCredito();
        tarjetaCredito.setId(idTarjeta);
        tarjetaCredito.setSaldoPorPagar(new BigDecimal("150.00"));
        tarjetaCredito.setCreditoDisponible(new BigDecimal("500.00"));

        // Simular cuenta de ahorro
        CuentaAhorro cuentaAhorro = new CuentaAhorro();
        cuentaAhorro.setId(idCuenta);
        cuentaAhorro.setSaldoDisponible(new BigDecimal("200.00"));

        // Simular transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);

        // Configurar mocks
        when(tarjetaRepository.findById(idTarjeta)).thenReturn(Optional.of(tarjetaCredito));
        when(cuentaAhorroRepository.findById(idCuenta)).thenReturn(Optional.of(cuentaAhorro));
        when(transaccionService.registrarTransaccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(transaccion);

        // Ejecutar el método
        ResponsePagoTarjetaDTO resultado = pagoTarjetaService.realizarPagoTarjetaCredito(idTarjeta, datosPagoTarjetaDTO);

        // Verificar resultados
        assertNotNull(resultado);
        assertEquals(new BigDecimal("100.00"), cuentaAhorro.getSaldoDisponible()); // 200 - 100 = 100
        assertEquals(new BigDecimal("50.00"), tarjetaCredito.getSaldoPorPagar()); // 150 - 100 = 50
        assertEquals(new BigDecimal("600.00"), tarjetaCredito.getCreditoDisponible()); // 500 + 100 = 600

        // Verificar que se llamaron los métodos simulados
        verify(tarjetaRepository, times(1)).findById(idTarjeta);
        verify(cuentaAhorroRepository, times(1)).findById(idCuenta);

        //se espera que el metodo para las transacciones haya sido llamado con los siguientes parametros
        verify(transaccionService, times(1)).registrarTransaccion(
                TipoTransaccion.PAGO_TARJETA,
                cuentaAhorro,
                null,
                tarjetaCredito,
                null,
                montoPago,
                "Se realizo un pago a la tarjeta de credito"
        );
    }

    @Test
    void testRealizarPagoTarjetaCreditoSaldoInsuficiente() {
        // Datos de prueba
        Long idTarjeta = 1L;
        Long idCuenta = 2L;
        BigDecimal montoPago = new BigDecimal("300.00");

        DatosPagoTarjetaDTO datosPagoTarjetaDTO = new DatosPagoTarjetaDTO(idCuenta, montoPago);

        // Simular cuenta de ahorro con saldo insuficiente
        CuentaAhorro cuentaAhorro = new CuentaAhorro();
        cuentaAhorro.setId(idCuenta);
        cuentaAhorro.setSaldoDisponible(new BigDecimal("200.00"));

        // simular tarjeta
        TarjetaCredito tarjetaCredito = new TarjetaCredito();
        tarjetaCredito.setId(idTarjeta);

        // Configurar mocks
        when(cuentaAhorroRepository.findById(idCuenta)).thenReturn(Optional.of(cuentaAhorro));
        when(tarjetaRepository.findById(idTarjeta)).thenReturn(Optional.of(tarjetaCredito));

        // Ejecutar el método y verificar excepción
        SaldoInsuficienteException exception = Assertions.assertThrows(
                SaldoInsuficienteException.class,
                () -> {
                    pagoTarjetaService.realizarPagoTarjetaCredito(idTarjeta, datosPagoTarjetaDTO);
                });

        // Verificar mensaje de excepción
        Assertions.assertEquals("La cuenta no dispone de el saldo suficiente para realizar el pago.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testTarjetaCreditoNoEncontrada() {
        // Datos de prueba
        Long idTarjeta = 1L;
        Long idCuenta = 2L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoTarjetaDTO datosPagoTarjetaDTO = new DatosPagoTarjetaDTO(idCuenta, montoPago);

        // Configurar mock para lanzar excepción
        when(tarjetaRepository.findById(idTarjeta)).thenReturn(Optional.empty());

        // Ejecutar el método y verificar excepción
        TarjetaNotFoundException exception = Assertions.assertThrows(
                TarjetaNotFoundException.class,
                () -> {
                    pagoTarjetaService.realizarPagoTarjetaCredito(idTarjeta, datosPagoTarjetaDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("Tarjeta de credito no encontrada", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCuentaAhorroNoEncontrada() {
        // Datos de prueba para el dto
        Long idTarjeta = 1L;
        Long idCuenta = 2L;
        BigDecimal montoPago = new BigDecimal("100.00");

        DatosPagoTarjetaDTO datosPagoTarjetaDTO = new DatosPagoTarjetaDTO(idCuenta, montoPago);

        // Simular tarjeta de crédito
        TarjetaCredito tarjetaCredito = new TarjetaCredito();
        tarjetaCredito.setId(idTarjeta);
        tarjetaCredito.setSaldoPorPagar(new BigDecimal("150.00"));

        // Configurar mocks
        when(tarjetaRepository.findById(idTarjeta)).thenReturn(Optional.of(tarjetaCredito));
        when(cuentaAhorroRepository.findById(idCuenta)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        CuentaNotFoundException exception = Assertions.assertThrows(
                CuentaNotFoundException.class, //clase de la excepcion que se espera sea lanzada
                () -> {
                    pagoTarjetaService.realizarPagoTarjetaCredito(idTarjeta, datosPagoTarjetaDTO);
                }
        );

        // Verificar mensaje de excepción
        Assertions.assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }
}