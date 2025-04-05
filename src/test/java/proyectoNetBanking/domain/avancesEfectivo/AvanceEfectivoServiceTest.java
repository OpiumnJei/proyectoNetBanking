package proyectoNetBanking.domain.avancesEfectivo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.dto.tarjetasCredito.ResponseAvanceEfectivoDTO;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.repository.TarjetaRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;
import proyectoNetBanking.dto.tarjetasCredito.AvanceEfectivoDTO;
import proyectoNetBanking.service.tarjetasCredito.AvanceEfectivoService;
import proyectoNetBanking.service.transacciones.TransaccionService;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.TarjetaNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvanceEfectivoServiceTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Mock
    private TransaccionService transaccionService;

    @InjectMocks
    private AvanceEfectivoService avanceEfectivoService;

    @Test
    void testRealizarAvanceEfectivoExitoso() {
        // Datos de prueba
        Long tarjetaCreditoId = 1L;
        Long cuentaAhorroId = 2L;
        BigDecimal montoAvanceEfectivo = new BigDecimal("100.00");

        AvanceEfectivoDTO avanceEfectivoDTO = new AvanceEfectivoDTO(tarjetaCreditoId, cuentaAhorroId, montoAvanceEfectivo);

        // Simular tarjeta de crédito
        TarjetaCredito tarjetaCredito = new TarjetaCredito();
        tarjetaCredito.setId(tarjetaCreditoId);
        tarjetaCredito.setLimiteCredito(BigDecimal.valueOf(2000.00));
        tarjetaCredito.setCreditoDisponible(new BigDecimal("500.00"));
        tarjetaCredito.setSaldoPorPagar(new BigDecimal("200.00"));

        // Simular cuenta de ahorro
        CuentaAhorro cuentaAhorro = new CuentaAhorro();
        cuentaAhorro.setId(cuentaAhorroId);
        cuentaAhorro.setSaldoDisponible(new BigDecimal("300.00"));

        // Simular transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setId(1L);

        // Configurar mocks
        when(tarjetaRepository.findById(tarjetaCreditoId)).thenReturn(Optional.of(tarjetaCredito));
        when(cuentaAhorroRepository.findById(cuentaAhorroId)).thenReturn(Optional.of(cuentaAhorro));
        when(transaccionService.registrarTransaccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(transaccion);

        // Ejecutar el metodo
        ResponseAvanceEfectivoDTO resultado = avanceEfectivoService.realizarAvanceEfectivo(avanceEfectivoDTO);

        // Verificar resultados
        assertNotNull(resultado);
        assertEquals(new BigDecimal("400.00"), cuentaAhorro.getSaldoDisponible()); // 300 + 100 = 400
        assertEquals(new BigDecimal("400.00"), tarjetaCredito.getCreditoDisponible()); // 500 - 100 = 400
        assertEquals(new BigDecimal("306.25"), tarjetaCredito.getSaldoPorPagar()); // 200 + 100 + 6.25 = 306.25

        // Verificar que se llamaron los métodos simulados
        verify(tarjetaRepository, times(1)).findById(tarjetaCreditoId);
        verify(cuentaAhorroRepository, times(1)).findById(cuentaAhorroId);
        verify(transaccionService, times(1)).registrarTransaccion(
                TipoTransaccion.AVANCE_EFECTIVO,
                cuentaAhorro,
                null,
                tarjetaCredito,
                null,
                montoAvanceEfectivo,
                "Se realizo un avance de afectivo desde una tarjea de credito"
        );
    }

    @Test
    void testMontoSuperiorAlCreditoDisponible() {
        // Datos de prueba
        Long tarjetaCreditoId = 1L;
        Long cuentaAhorroId = 2L;
        BigDecimal montoAvanceEfectivo = new BigDecimal("600.00");

        AvanceEfectivoDTO avanceEfectivoDTO = new AvanceEfectivoDTO(tarjetaCreditoId, cuentaAhorroId, montoAvanceEfectivo);

        // Simular tarjeta de crédito
        TarjetaCredito tarjetaCredito = new TarjetaCredito();
        tarjetaCredito.setId(tarjetaCreditoId);
        tarjetaCredito.setCreditoDisponible(new BigDecimal("500.00"));

        // Simular cuenta ahorro
        CuentaAhorro cuentaAhorro = new CuentaAhorro();
        cuentaAhorro.setId(cuentaAhorroId);

        // Configurar mocks
        when(tarjetaRepository.findById(tarjetaCreditoId)).thenReturn(Optional.of(tarjetaCredito));
        when(cuentaAhorroRepository.findById(cuentaAhorroId)).thenReturn(Optional.of(cuentaAhorro));

        // Ejecutar el metodo y verificar excepción
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    avanceEfectivoService.realizarAvanceEfectivo(avanceEfectivoDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("El monto del avance supera el crédito disponible de la tarjeta.", exception.getMessage());
        System.out.println(exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarAvanceEfectivoTarjetaNoEncontrada() {
        // Datos de prueba
        Long tarjetaCreditoId = 1L;
        Long cuentaAhorroId = 2L;
        BigDecimal montoAvanceEfectivo = new BigDecimal("100.00");

        AvanceEfectivoDTO avanceEfectivoDTO = new AvanceEfectivoDTO(tarjetaCreditoId, cuentaAhorroId, montoAvanceEfectivo);

        // Configurar mock para lanzar excepción
        when(tarjetaRepository.findById(tarjetaCreditoId)).thenReturn(Optional.empty());

        // Ejecutar el método y verificar excepción
        TarjetaNotFoundException exception = assertThrows(
                TarjetaNotFoundException.class,
                () -> {
                    avanceEfectivoService.realizarAvanceEfectivo(avanceEfectivoDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("Tarjeta de crédito no encontrada.", exception.getMessage());
        System.out.println(exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRealizarAvanceEfectivoCuentaNoEncontrada() {
        // Datos de prueba
        Long tarjetaCreditoId = 1L;
        Long cuentaAhorroId = 2L;
        BigDecimal montoAvanceEfectivo = new BigDecimal("100.00");

        AvanceEfectivoDTO avanceEfectivoDTO = new AvanceEfectivoDTO(tarjetaCreditoId, cuentaAhorroId, montoAvanceEfectivo);

        // Simular tarjeta de crédito
        TarjetaCredito tarjetaCredito = new TarjetaCredito();
        tarjetaCredito.setId(tarjetaCreditoId);
        tarjetaCredito.setCreditoDisponible(new BigDecimal("500.00"));

        // Simular cuenta ahorro
        CuentaAhorro cuentaAhorro = new CuentaAhorro();
        cuentaAhorro.setId(cuentaAhorroId);

        // Configurar mocks
        when(tarjetaRepository.findById(tarjetaCreditoId)).thenReturn(Optional.of(tarjetaCredito));
        when(cuentaAhorroRepository.findById(cuentaAhorroId)).thenReturn(Optional.empty());

        // Ejecutar el metodo y verificar excepción
        CuentaNotFoundException exception = assertThrows(
                CuentaNotFoundException.class,
                () -> {
                    avanceEfectivoService.realizarAvanceEfectivo(avanceEfectivoDTO);
                });

        // Verificar mensaje de excepción
        assertEquals("La cuenta no ha sido encontrada.", exception.getMessage());

        // Verificar que no se llamó a registrarTransaccion
        verify(transaccionService, never()).registrarTransaccion(any(), any(), any(), any(), any(), any(), any());
    }
}