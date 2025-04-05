package proyectoNetBanking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proyectoNetBanking.dto.productos.ProductosClienteDTO;
import proyectoNetBanking.service.usuarios.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("netbanking/cliente/productos")
public class ClienteHomeController {

    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/{clienteId}/lista-productos")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<ProductosClienteDTO>> listarProductosActivos(@PathVariable Long clienteId) {
        List<ProductosClienteDTO> productos = usuarioService.obtenerProductosCliente(clienteId);
        return ResponseEntity.ok(productos);
    }


}
