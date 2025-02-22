package proyectoNetBanking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyectoNetBanking.dto.productos.ProductoUsuarioDTO;
import proyectoNetBanking.dto.usuarios.ActualizarDatosUsuarioDTO;
import proyectoNetBanking.dto.usuarios.AdminResponseDTO;
import proyectoNetBanking.dto.usuarios.ClienteResponseDTO;
import proyectoNetBanking.dto.usuarios.DatosUsuarioDTO;
import proyectoNetBanking.service.usuarios.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {


    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/nuevo-usuario")
    public ResponseEntity<String> crearUsuario(@RequestBody DatosUsuarioDTO datosUsuarioDTO) {
        usuarioService.crearUsuario(datosUsuarioDTO);
        return ResponseEntity.ok("Usuario creado exitosamente");
    }

    @GetMapping("/{usuarioId}/productos-activos")
    public ResponseEntity<List<ProductoUsuarioDTO>> obtenerProductosActivos(@PathVariable Long usuarioId) {
        List<ProductoUsuarioDTO> productos = usuarioService.obtenerProductosUsuario(usuarioId);
        return ResponseEntity.ok(productos);
    }

    @PutMapping("/inactivar/{usuarioId}")
    public ResponseEntity<String> inactivarUsuario(@PathVariable Long usuarioId) {
        usuarioService.inactivarUsuario(usuarioId);
        return ResponseEntity.ok("Usuario inactivado exitosamente");
    }

    @PutMapping("/actualizar/clientes/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(@PathVariable Long id, @RequestBody ActualizarDatosUsuarioDTO clienteActualizadoDTO) {
        ClienteResponseDTO clienteActualizado = usuarioService.actualizarDatosCliente(id, clienteActualizadoDTO);
        return ResponseEntity.ok(clienteActualizado);
    }

    @PutMapping("/actualizar/administradores/{id}")
    public ResponseEntity<AdminResponseDTO> actualizarAdministrador(@PathVariable Long id, @RequestBody ActualizarDatosUsuarioDTO adminUpdateDTO) {
        AdminResponseDTO usuarioActualizado = usuarioService.actualizarDatosAdmin(id, adminUpdateDTO);
        return ResponseEntity.ok(usuarioActualizado);
    }



}
