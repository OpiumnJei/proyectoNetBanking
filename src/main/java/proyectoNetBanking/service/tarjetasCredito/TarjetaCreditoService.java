package proyectoNetBanking.service.tarjetasCredito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.dto.tarjetasCredito.TarjetaCreditoResponseDTO;
import proyectoNetBanking.infra.errors.DatosInvalidosException;
import proyectoNetBanking.infra.errors.UsuarioInactivoException;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.dto.tarjetasCredito.DatosTarjetaDTO;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.repository.TarjetaRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.repository.UsuarioRepository;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class TarjetaCreditoService {

    //monto minimo para el limite de credito
    private final BigDecimal MONTO_MINIMO_LIMITE_CREDITO = BigDecimal.valueOf(2000);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private GeneradorId generadorId; //inyectamos el generador de ids

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    @Transactional
    public TarjetaCreditoResponseDTO crearTarjetaCredito(Long usuarioId, DatosTarjetaDTO datosTarjetaDTO) {

        if (usuarioId == null || usuarioId <= 0) { //validar que numero no sea negativo ni nulo
            throw new DatosInvalidosException("El ID del usuario no puede ser nulo ni un número negativo");
        }

        //verificar que el usuario exista en la bd
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException());

        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario introducido se encuentra inactivo");
        }
        //validar que el limite de credito sea mayor o igual 2000
        if (datosTarjetaDTO.limiteCredito().compareTo(MONTO_MINIMO_LIMITE_CREDITO) < 0) {
            throw new RuntimeException("El limite de credito introducido es menor al monto minimo aceptado.");
        }

        TarjetaCredito tarjetaCreada = asignarTarjetaCredito(usuario, datosTarjetaDTO.limiteCredito());

        return new TarjetaCreditoResponseDTO(
                tarjetaCreada.getUsuario().getId(),
                tarjetaCreada.getIdProducto(),
                tarjetaCreada.getLimiteCredito(),
                tarjetaCreada.getCreditoDisponible(),
                tarjetaCreada.getCreated()
        );

    }

    private TarjetaCredito asignarTarjetaCredito(Usuario usuario, BigDecimal limiteCredito) {

        TarjetaCredito tarjetaUsuario = new TarjetaCredito();
        tarjetaUsuario.setIdProducto(generarIdUnicoProducto());
        tarjetaUsuario.setLimiteCredito(limiteCredito);
        tarjetaUsuario.setCreditoDisponible(limiteCredito);
        tarjetaUsuario.setSaldoPorPagar(BigDecimal.ZERO); //al crear una nueva tarjeta el saldo por pagar debe ser 0
        tarjetaUsuario.setUsuario(usuario);
        tarjetaUsuario.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.ACTIVO.name()));
        return tarjetaRepository.save(tarjetaUsuario);
    }

    private EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }

    //generar id del producto
    private String generarIdUnicoProducto() {

        return generadorId.generarIdUnicoProducto(cuentaAhorroRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }


}
