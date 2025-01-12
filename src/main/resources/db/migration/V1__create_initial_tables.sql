-- TABLATIPO CATALOGO QUE ALMACENA LOS ESTADOS DE UN PRODUCTO

CREATE TABLE `estado_productos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime(6) DEFAULT NULL,
  `last_modified` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  `nombre_estado` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- TABLA TIPO CATALOGO QUE ALMACENA LOS TIPOS DE USUARIOS

CREATE TABLE `tipo_usuarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre_tipo_usuario` varchar(255) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- TABLA DE USUARIOS

CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `cedula` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `tipo_usuario_id` bigint(20) DEFAULT NULL,
  `monto_inicial` double NOT NULL,
  `activo` bit(1) NOT NULL,
  `created` datetime(6) DEFAULT NULL,
  `last_modified` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKefovjjo5q5jlsa0f9eoptdjly` (`cedula`),
  UNIQUE KEY `UKcdmw5hxlfj78uf4997i3qyyw5` (`correo`),
  KEY `FKjtobhh8orj18ckydcxqngeqos` (`tipo_usuario_id`),
  CONSTRAINT `FKjtobhh8orj18ckydcxqngeqos` FOREIGN KEY (`tipo_usuario_id`) REFERENCES `tipo_usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- TABLA DE BENEFICIRIOS

CREATE TABLE `beneficiarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `num_cuenta_beneficiario` varchar(255) DEFAULT NULL,
  `usuario_id` bigint(20) DEFAULT NULL,
  `nombre_beneficiario` varchar(255) DEFAULT NULL,
  `created` datetime(6) DEFAULT NULL,
  `last_modified` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8n18a6nyiojmflkhpdh2m94vy` (`usuario_id`),
  CONSTRAINT `FK8n18a6nyiojmflkhpdh2m94vy` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- TABLA DE CUENTAS DE AHORRO

CREATE TABLE `cuentas_ahorro` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_producto` varchar(255) NOT NULL,
  `saldo_disponible` double NOT NULL,
  `es_principal` bit(1) NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `estado_producto_id` bigint(20) DEFAULT NULL,
  `created` datetime(6) DEFAULT NULL,
  `last_modified` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhx57ytog6now5uij2wm52c155` (`id_producto`),
  UNIQUE KEY `UKhkxei0mmi8x7557y0n4dj9wg1` (`estado_producto_id`),
  KEY `FKexvmvkebkfod7difpysc9yyp1` (`usuario_id`),
  CONSTRAINT `FKexvmvkebkfod7difpysc9yyp1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKmj83a8aiqwlulojgio26wkwcn` FOREIGN KEY (`estado_producto_id`) REFERENCES `estado_productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- TABLAS DE PRESTAMOS

CREATE TABLE `prestamos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_producto` varchar(255) NOT NULL,
  `monto_apagar` double NOT NULL,
  `monto_pagado` double NOT NULL,
  `monto_prestamo` double NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `estado_producto_id` bigint(20) DEFAULT NULL,
  `created` datetime(6) DEFAULT NULL,
  `last_modified` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpos33kmsc3mvqerbbo9v680i4` (`id_producto`),
  UNIQUE KEY `UK7sv8559fsjxq3pbm71b34luvd` (`estado_producto_id`),
  KEY `FKeqd1t799y0x5ck9mdeltepy1w` (`usuario_id`),
  CONSTRAINT `FKc804eq4c11d0ob9fqgbsb47m8` FOREIGN KEY (`estado_producto_id`) REFERENCES `estado_productos` (`id`),
  CONSTRAINT `FKeqd1t799y0x5ck9mdeltepy1w` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- TABLA DE TARJETAS DE CREDITO

CREATE TABLE `tarjetas_credito` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_producto` varchar(255) NOT NULL,
  `limite_credito` double NOT NULL,
  `saldo_disponible` double NOT NULL,
  `saldo_por_pagar` double NOT NULL,
  `usuario_id` bigint(20) NOT NULL,
  `estado_producto` bigint(20) DEFAULT NULL,
  `created` datetime(6) DEFAULT NULL,
  `last_modified` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnfkog3klotkvm0xqmdyiem1d3` (`id_producto`),
  UNIQUE KEY `UKncxm96771rqao82w0uygo2emq` (`estado_producto`),
  KEY `FK2k571o9whwcwqg34rg8xvx515` (`usuario_id`),
  CONSTRAINT `FK2k571o9whwcwqg34rg8xvx515` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKq17h7qrb8y3wghrm8utl6mth2` FOREIGN KEY (`estado_producto`) REFERENCES `estado_productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- TABLA DE TRANSACCIONES

CREATE TABLE `transacciones` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tipo_transaccion` tinyint(4) DEFAULT NULL,
  `usuario_origen` bigint(20) DEFAULT NULL,
  `usuario_destino` bigint(20) DEFAULT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `descripcion_transaccion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKto1yy2ibwvcbcvsad5n56ccdb` (`usuario_destino`),
  KEY `FK3nmjw2w14hniq2s1te57gnj9o` (`usuario_origen`),
  CONSTRAINT `FK3nmjw2w14hniq2s1te57gnj9o` FOREIGN KEY (`usuario_origen`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKto1yy2ibwvcbcvsad5n56ccdb` FOREIGN KEY (`usuario_destino`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;