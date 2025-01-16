package proyectoNetBanking.domain.common;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Predicate;

@Component
public class GeneradorId{

    //metodo para generar el identificador unico del producto
    private String generarIdProducto() {

        /*
        * SecureRandom es una clase en Java utilizada para generar números aleatorios de alta calidad criptográfica.
        * */
        // Generar número como cadena
        return String.valueOf(100_000_000 + new SecureRandom().nextInt(900_000_000));
    }

    /*
    * Para comprobar que el id del producto no exista en la base datos se usa
    * una interfaz funcional (Predicate<String>) que recibe un parametro del tipo String y retorna
    * un booleano.
    *
    * */

    //generar id del producto y verificar que no exista un producto con ese id
    public String generarIdUnicoProducto(Predicate<String> verificarId) {
        String idGenerado;
        do {
            idGenerado = generarIdProducto();//id generado
        }
        while (verificarId.test(idGenerado)); //validar que el id del producto generado no exista en la bd
        return idGenerado;
    }
}
