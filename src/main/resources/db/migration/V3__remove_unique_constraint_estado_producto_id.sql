
ALTER TABLE cuentas_ahorro DROP FOREIGN KEY FKmj83a8aiqwlulojgio26wkwcn ;  -- eliminar primero la relacion con entre cuenta_ahorro y estado_producto
ALTER TABLE cuentas_ahorro DROP INDEX UKhkxei0mmi8x7557y0n4dj9wg1; -- eliminar el indice unico de estado_producto
