package proyectoNetBanking.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyectoNetBanking.dto.cuentasAhorro.CuentaResponseDTO;
import proyectoNetBanking.dto.cuentasAhorro.DatosCuentaAhorroDTO;
import proyectoNetBanking.dto.cuentasAhorro.DatosEliminarCuentaDTO;
import proyectoNetBanking.dto.prestamos.DatosPrestamoDTO;
import proyectoNetBanking.dto.prestamos.PrestamoResponseDTO;
import proyectoNetBanking.dto.tarjetasCredito.DatosTarjetaDTO;
import proyectoNetBanking.dto.tarjetasCredito.TarjetaCreditoResponseDTO;
import proyectoNetBanking.dto.usuarios.*;
import proyectoNetBanking.service.cuentasAhorro.CuentaAhorroService;
import proyectoNetBanking.service.prestamos.PrestamoService;
import proyectoNetBanking.service.tarjetasCredito.TarjetaCreditoService;
import proyectoNetBanking.service.usuarios.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class AdminController {


    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CuentaAhorroService cuentaAhorroService;

    @Autowired
    private TarjetaCreditoService tarjetaCreditoService;

    @Autowired
    private PrestamoService prestamoService;

    @PostMapping("/nuevo-usuario")
    public ResponseEntity<String> crearUsuario(@RequestBody @Valid DatosUsuarioDTO datosUsuarioDTO) {
        usuarioService.crearUsuario(datosUsuarioDTO);
        return ResponseEntity.ok("Usuario creado exitosamente");
    }

    @GetMapping("/listar")
    public ResponseEntity<Page<ListaUsuariosDTO>> listarUsuarios(Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(pageable));
    }

    @PutMapping("/activar-usuario/{usuarioId}")
    public ResponseEntity<String> activarUsuario(@PathVariable Long usuarioId) {
        usuarioService.activarUsuario(usuarioId);
        return ResponseEntity.ok("Usuario activado exitosamente");
    }

    @DeleteMapping("/inactivar-usuario/{usuarioId}")
    public ResponseEntity<String> inactivarUsuario(@PathVariable Long usuarioId) {
        usuarioService.inactivarUsuario(usuarioId);
        return ResponseEntity.ok("Usuario inactivado exitosamente");
    }

    @PutMapping("/actualizar/clientes/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable Long id,
            @RequestBody @Valid ActualizarDatosUsuarioDTO clienteActualizadoDTO) { //@Valid se usa en el dto para que las validaciones funcionen correctamente

        ClienteResponseDTO clienteActualizado = usuarioService.actualizarDatosCliente(id, clienteActualizadoDTO);
        return ResponseEntity.ok(clienteActualizado);
    }

    @PutMapping("/actualizar/administradores/{id}")
    public ResponseEntity<AdminResponseDTO> actualizarAdministrador(@PathVariable Long id, @RequestBody @Valid ActualizarDatosUsuarioDTO adminUpdateDTO) {
        AdminResponseDTO usuarioActualizado = usuarioService.actualizarDatosAdmin(id, adminUpdateDTO);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PostMapping("/crear-cuenta-ahorro")
    public ResponseEntity<CuentaResponseDTO> crearCuentaAhorro(@RequestBody @Valid  DatosCuentaAhorroDTO datosCuentaAhorroDTO) {
        CuentaResponseDTO cuentaResponseDTO = cuentaAhorroService.CrearCuentaAhorroSecundaria(datosCuentaAhorroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaResponseDTO);
    }

    @DeleteMapping("/eliminar-cuenta")
    public ResponseEntity<String> eliminarCuentaAhorro(@RequestBody DatosEliminarCuentaDTO datosEliminarCuentaDTO) {
        cuentaAhorroService.eliminarCuenta(datosEliminarCuentaDTO);
        return ResponseEntity.ok("Cuenta eliminada exitosamente");
    }

    @PostMapping("/crear-tarjeta-credito/{usuarioId}")
    public ResponseEntity<TarjetaCreditoResponseDTO> crearTarjetaCredito(@PathVariable Long usuarioId,@RequestBody @Valid DatosTarjetaDTO datosTarjetaDTO) {
        TarjetaCreditoResponseDTO TarjetaResponse = tarjetaCreditoService.crearTarjetaCredito(usuarioId,datosTarjetaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(TarjetaResponse);
    }

    @PostMapping("/crear-prestamo/{usuarioId}")
    public ResponseEntity<PrestamoResponseDTO> crearPrestamo(@PathVariable Long usuarioId, @RequestBody @Valid DatosPrestamoDTO datosPrestamoDTO) {
        PrestamoResponseDTO prestamoResponseDTO = prestamoService.crearPrestamo(usuarioId,datosPrestamoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoResponseDTO);
    }

}
