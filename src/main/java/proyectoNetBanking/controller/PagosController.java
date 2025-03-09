package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyectoNetBanking.dto.pagos.DatosPagoExpresoDTO;
import proyectoNetBanking.dto.pagos.DatosPagoTarjetaDTO;
import proyectoNetBanking.dto.pagos.ResponsePagoExpresoDTO;
import proyectoNetBanking.dto.pagos.ResponsePagoTarjetaDTO;
import proyectoNetBanking.service.pagos.PagoBeneficiarioService;
import proyectoNetBanking.service.pagos.PagoExpresoService;
import proyectoNetBanking.service.pagos.PagoPrestamoService;
import proyectoNetBanking.service.pagos.PagoTarjetaService;

@RestController
@RequestMapping("netbanking/pagos")
public class PagosController {

    @Autowired
    private PagoExpresoService pagoExpresoService;

    @Autowired
    private PagoTarjetaService pagoTarjetaService;

    @Autowired
    private PagoPrestamoService pagoPrestamoService;

    @Autowired
    private PagoBeneficiarioService PagoBeneficiarioService;

    @PostMapping("/realizar-pago-expreso")
    public ResponseEntity<ResponsePagoExpresoDTO> realizarPagoExpreso(@RequestBody @Valid DatosPagoExpresoDTO datosPagoExpresoDTO) {
        ResponsePagoExpresoDTO nuevoPagoPrestamo = pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPagoPrestamo);
    }

    @PostMapping("/realizar-pago-tarjeta/{tarjetaId}")
    public ResponseEntity<ResponsePagoTarjetaDTO> realizarPagoTarjeta(@PathVariable Long tarjetaId, @RequestBody @Valid DatosPagoTarjetaDTO datosPagoTarjetaDTO) {
        ResponsePagoTarjetaDTO nuevoPagoTarjeta = pagoTarjetaService.realizarPagoTarjetaCredito(tarjetaId,datosPagoTarjetaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPagoTarjeta);
    }

}
