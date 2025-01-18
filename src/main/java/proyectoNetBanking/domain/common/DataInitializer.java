package proyectoNetBanking.domain.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.usuarios.TipoUsuario;
import proyectoNetBanking.domain.usuarios.TipoUsuarioRepository;

//CLASE USADA PARA LA INICIALIZACION DE LOS DATOS CRITICOS/ESENCIALES
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    //inicializacion
    @Override
    public void run(String... args) throws Exception {
        inicializarTipoUsuario();
        inicializarEstadoProducto();
    }

    //INICIALIZAR TIPOS DE USUARIOS
    private void inicializarTipoUsuario() {
        guardarTipoUsuario("Administrador", "Puede acceder a las funcionalidades de un administrador");
        guardarTipoUsuario("Cliente", "Puede acceder a las funcionalidades de un cliente");
    }

    //metodo para guarda/verificar si existen esos tipos de usuarios en la bd
    private void guardarTipoUsuario(String nombreTipoUsuario, String descripcion) {

        if (!tipoUsuarioRepository.existsByNombreTipoUsuario(nombreTipoUsuario)) { //se verifica que el nombre no exista en la bd
            TipoUsuario tipoUsuario = new TipoUsuario();
            tipoUsuario.setNombreTipoUsuario(nombreTipoUsuario);
            tipoUsuario.setDescripcion(descripcion);
            tipoUsuarioRepository.save(tipoUsuario);
        }

    }

    //INICIALIZAR PRODUCTOS
    private void inicializarEstadoProducto () {
        // Ejemplo: "Activo", "Inactivo", "Bloqueado"
        guardarEstadoProducto("Activo");
        guardarEstadoProducto("Inactivo");
        guardarEstadoProducto("Bloqueado");
        guardarEstadoProducto("Saldado");
    }

    //metodo para guarda/verificar si existen esos tipos de usuarios en la bd
    private void guardarEstadoProducto (String nombreEstado){

        if (!estadoProductoRepository.existsByNombreEstado(nombreEstado)) { //se verifica que el nombre no exista en la bd
            EstadoProducto estadoProducto = new EstadoProducto();
            estadoProducto.setNombreEstado(nombreEstado);
            estadoProductoRepository.save(estadoProducto);
        }
    }
}
