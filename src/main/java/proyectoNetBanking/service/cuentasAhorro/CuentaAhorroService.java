package proyectoNetBanking.service.cuentasAhorro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.dto.cuentasAhorro.CuentaResponseDTO;
import proyectoNetBanking.dto.cuentasAhorro.DatosCuentaAhorroDTO;
import proyectoNetBanking.dto.cuentasAhorro.DatosEliminarCuentaDTO;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioInactivoException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.repository.UsuarioRepository;

import java.math.BigDecimal;

@Service
public class CuentaAhorroService {

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GeneradorId generadorId; //inyectamos el generador de ids

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    //crear una cuenta de ahorro
    @Transactional
    public CuentaResponseDTO CrearCuentaAhorroSecundaria(DatosCuentaAhorroDTO datosCuentasAhorroDTO) {

        //verificar si el usuario existe
        Usuario usuario = obtenerUsuario(datosCuentasAhorroDTO.usuarioId());

        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario introducido se encuentra inactivo");
        }

        validarLimiteCuentasUsuario(usuario.getId());
        // Validar límite de cuentas

       CuentaAhorro cuentaSecundaria = guardarDatosCuenta(usuario, datosCuentasAhorroDTO);

        return new CuentaResponseDTO(
                cuentaSecundaria.getUsuario().getId(),
                cuentaSecundaria.getSaldoDisponible(),
                cuentaSecundaria.getProposito(),
                cuentaSecundaria.getCreated()
        );
    }

    private CuentaAhorro guardarDatosCuenta(Usuario usuario, DatosCuentaAhorroDTO datosCuentasAhorroDTO) {

        CuentaAhorro cuentaSecundaria = new CuentaAhorro();
        cuentaSecundaria.setIdProducto(generarIdUnicoProducto());
        cuentaSecundaria.setUsuario(usuario);
        cuentaSecundaria.setSaldoDisponible((datosCuentasAhorroDTO.montoCuenta()));
        cuentaSecundaria.setProposito(datosCuentasAhorroDTO.proposito());
        cuentaSecundaria.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.ACTIVO.name()));

        return cuentaAhorroRepository.save(cuentaSecundaria);
    }

    private void validarLimiteCuentasUsuario(Long usuarioId) {
        int maxCuentas = 5; // Límite máximo permitido
        if (cuentaAhorroRepository.countByUsuarioId(usuarioId) >= maxCuentas) {
            throw new RuntimeException("El usuario ya alcanzo el límite de cuentas permitidas.");
        }
    }

    @Transactional
    public void eliminarCuenta(DatosEliminarCuentaDTO datosEliminarCuenta) {

        Usuario usuario = obtenerUsuario(datosEliminarCuenta.usuarioId());

        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario introducido se encuentra inactivo");
        }

        CuentaAhorro cuentaSecundaria = obtenerCuenta(datosEliminarCuenta.cuentaId());

        // Verificar que la cuenta pertenece al usuario
        if (!cuentaSecundaria.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("La cuenta no pertenece al usuario especificado");
        }

        // Verificar si es la cuenta principal
        if (cuentaSecundaria.isEsPrincipal()) {
            throw new RuntimeException("Es la cuenta principal del usuario, no puede ser eliminada");
        }

        // Obtener la cuenta principal del usuario
        CuentaAhorro cuentaPrincipal = obtenerCuentaPrincipal(datosEliminarCuenta.usuarioId());

        // Transferir saldo si la cuenta secundaria tiene saldo mayor que cero
        if (cuentaSecundaria.getSaldoDisponible().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal nuevoSaldoPrincipal = cuentaPrincipal.getSaldoDisponible().add(cuentaSecundaria.getSaldoDisponible());
            cuentaPrincipal.setSaldoDisponible(nuevoSaldoPrincipal);
            cuentaAhorroRepository.save(cuentaPrincipal); // Guardar cambios en la cuenta principal
        }

        desactivarCuentaSecundaria(cuentaSecundaria);
    }

    private void desactivarCuentaSecundaria(CuentaAhorro cuentaSecundaria) {
        // Inactivar la cuenta secundaria
        cuentaSecundaria.setSaldoDisponible(BigDecimal.ZERO); // Establecer saldo a cero
        cuentaSecundaria.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name())); // Inactivar la cuenta
        cuentaAhorroRepository.save(cuentaSecundaria); // Guardar cambios en la cuenta secundaria
    }


    //metodo para obtener la cuenta principal del usuario
    private CuentaAhorro obtenerCuentaPrincipal(Long usuarioId) {
        return cuentaAhorroRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(CuentaAhorro::isEsPrincipal)
                .findFirst()//extrae el primer registro en donde esPrincipal = true
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró una cuenta principal para este usuario"));
    }

    private CuentaAhorro obtenerCuenta(Long cuentaId) {
        return cuentaAhorroRepository.findById(cuentaId)
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta de ahorro no existe"));
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));
    }

    //metodo encargado de la gestion de estados
    private EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }

    //generar id del producto
    private String generarIdUnicoProducto() {

        return generadorId.generarIdUnicoProducto(cuentaAhorroRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }
}


