package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proyectoNetBanking.dto.tarjetasCredito.AvanceEfectivoDTO;
import proyectoNetBanking.dto.tarjetasCredito.ResponseAvanceEfectivoDTO;
import proyectoNetBanking.service.tarjetasCredito.AvanceEfectivoService;

@RestController
@RequestMapping("netbanking/")
public class AvanceEfectivoController {

    @Autowired
    private AvanceEfectivoService avanceEfectivoService;

    @PostMapping("realizar-avance-efectivo")
    public ResponseEntity<ResponseAvanceEfectivoDTO> realizarAvanceEfectivo(@RequestBody @Valid AvanceEfectivoDTO avanceEfectivoDTO){
        ResponseAvanceEfectivoDTO nuevoAvance = avanceEfectivoService.realizarAvanceEfectivo(avanceEfectivoDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAvance);
    }
}
