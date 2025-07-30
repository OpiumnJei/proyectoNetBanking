-- V6__Insert_initial_catalogs.sql
INSERT INTO tipo_usuarios (id, nombre_tipo_usuario, descripcion) VALUES(1, 'Administrador', 'Puede acceder a las funcionalidades de un administrador');
INSERT INTO tipo_usuarios (id, nombre_tipo_usuario, descripcion) VALUES(2, 'Cliente', 'Puede acceder a las funcionalidades de un cliente');

-- Inserta aquí cualquier otro dato maestro que necesites, como los estados de productos, etc.
INSERT INTO estado_productos (id, nombre_estado) VALUES (1, 'Activo');
INSERT INTO estado_productos (id, nombre_estado) VALUES (2, 'Inactivo');
INSERT INTO estado_productos (id, nombre_estado) VALUES (3, 'Bloqueado');
INSERT INTO estado_productos (id, nombre_estado) VALUES (4, 'Saldado');

