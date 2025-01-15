package proyectoNetBanking.domain.prestamos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class PrestamoService {

    //monto minimo del prestamo
    private final BigDecimal MONTO_MINIMO_PRESTAMO = BigDecimal.valueOf(1000);
    @Autowired
    private PrestamoRepository prestamoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GeneradorId generadorId;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Transactional
    public void crearPrestamo(DatosPrestamoDTO datosPrestamoDTO){

        //verificar que el usuario exista en la bd
        Usuario usuario = usuarioRepository.findById(datosPrestamoDTO.idUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException()); // en lugar de usar la lambda tambien se puede usar UsuarioNotFoundException::new

        // Validar que el monto solicitado sea mayor o igual a 1,000 DOP
        if(datosPrestamoDTO.montoPrestamo().compareTo(MONTO_MINIMO_PRESTAMO) < 0 ){
            throw new RuntimeException("El monto introducido es menor al monto minimo aceptado.");
        }

        //crear instancia de un prestamo
        Prestamo prestamo = new Prestamo();
        prestamo.setIdProducto(generarIdUnicoProducto());
        prestamo.setMontoPrestamo(datosPrestamoDTO.montoPrestamo());
        prestamo.setMontoApagar(datosPrestamoDTO.montoPrestamo());
        prestamo.setMontoPagado(BigDecimal.ZERO);
        prestamo.setUsuario(usuario);

        trasladarMontoACuentaPrincipal(datosPrestamoDTO.montoPrestamo(), usuario.getId());//se obtienen el monto y el id del usuario
    }

    public void trasladarMontoACuentaPrincipal(BigDecimal monto, Long idUsuario){

        //buscar cuenta principa
        CuentaAhorro cuentaPrincipal = cuentaAhorroRepository.findByUsuarioId(idUsuario)
                .stream()
                .filter(CuentaAhorro::isEsPrincipal)
                .findFirst()//extrae el primer registro en donde esPrincipal = true
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró una cuenta principal para este usuario"));

        // Transferir monto del prestamo a la cuenta principal
        cuentaPrincipal.setSaldoDisponible(cuentaPrincipal.getSaldoDisponible().add(monto));

    }


    //generar id del producto
    public String generarIdUnicoProducto() {
       /*
       Esta linea -> return generadorId.generarIdUnicoProducto(id -> prestamo.existsByIdProducto(id));
       Hace lo mismo que la linea de abajo:
       *  */
        return generadorId.generarIdUnicoProducto(prestamoRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }


}
