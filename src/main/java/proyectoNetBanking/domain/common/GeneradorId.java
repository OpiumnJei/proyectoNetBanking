package proyectoNetBanking.domain.common;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeneradorId {

    //metodo para generar el identificador unico del producto
    public String generarIdProducto() {
        String idProducto;

        // Generar número como cadena
        idProducto = String.valueOf(100_000_000 + new SecureRandom().nextInt(900_000_000));
        return idProducto;
    }
}
