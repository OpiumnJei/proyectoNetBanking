package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proyectoNetBanking.dto.pagos.DatosPagoExpresoDTO;
import proyectoNetBanking.dto.pagos.ResponsePagoExpresoDTO;
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
        ResponsePagoExpresoDTO nuevoPago = pagoExpresoService.realizarPagoExpreso(datosPagoExpresoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPago);
    }

}
