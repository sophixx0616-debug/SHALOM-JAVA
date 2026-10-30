package com.shalom.config;

import com.shalom.model.Role;
import com.shalom.model.Service;
import com.shalom.model.User;
import com.shalom.repository.RoleRepository;
import com.shalom.repository.ServiceRepository;
import com.shalom.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ============================================================
            // ROLES
            // ============================================================

            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role role = new Role();
                role.setName("ADMIN");
                return roleRepository.save(role);
            });

            Role clientRole = roleRepository.findByName("CLIENTE").orElseGet(() -> {
                Role role = new Role();
                role.setName("CLIENTE");
                return roleRepository.save(role);
            });

            // ============================================================
            // USUARIO ADMINISTRADOR
            // ============================================================

            if (!userRepository.existsByEmail("admin@shalom.com")) {

                User admin = new User();

                admin.setName("Administrador");
                admin.setEmail("admin@shalom.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(adminRole);

                userRepository.save(admin);
            }

            // ============================================================
            // CABELLO
            // ============================================================

            agregarServicio(
                    "Corte de cabello dama",
                    "Corte de cabello para dama.",
                    25000,
                    45,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Corte de flequillo",
                    "Diseño y corte de flequillo.",
                    12000,
                    20,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Lavado premium",
                    "Lavado profesional con productos especializados.",
                    18000,
                    30,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Secado blower",
                    "Secado profesional con blower.",
                    20000,
                    30,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Planchado cabello",
                    "Planchado profesional del cabello.",
                    22000,
                    30,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Ondas y rizos",
                    "Creación de ondas o rizos profesionales.",
                    30000,
                    45,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Peinado casual",
                    "Peinado casual para diferentes ocasiones.",
                    30000,
                    45,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Peinado gala",
                    "Peinado elegante para eventos especiales.",
                    55000,
                    75,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Peinado novia",
                    "Peinado profesional para novia.",
                    80000,
                    90,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Peinado quinceañera",
                    "Peinado especial para quinceañera.",
                    70000,
                    90,
                    "Cabello",
                    serviceRepository
            );

            agregarServicio(
                    "Tinte completo",
                    "Aplicación de color en todo el cabello.",
                    85000,
                    120,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Retoque raíz",
                    "Retoque de crecimiento en raíces.",
                    55000,
                    90,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Tinte sin amoniaco",
                    "Coloración sin amoniaco.",
                    95000,
                    120,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Mechas clásicas",
                    "Mechas tradicionales para iluminar el cabello.",
                    120000,
                    150,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Balayage",
                    "Técnica balayage personalizada.",
                    180000,
                    180,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Babylights",
                    "Iluminaciones finas y naturales.",
                    150000,
                    180,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Ombré californianas",
                    "Degradado de color estilo californiano.",
                    160000,
                    180,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Matiz toner",
                    "Matización y neutralización de tonos.",
                    50000,
                    60,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Decoloración global",
                    "Decoloración completa del cabello.",
                    150000,
                    180,
                    "Color",
                    serviceRepository
            );

            agregarServicio(
                    "Alisado keratina",
                    "Alisado profesional con keratina.",
                    180000,
                    180,
                    "Tratamientos capilares",
                    serviceRepository
            );

            agregarServicio(
                    "Alisado japonés",
                    "Alisado permanente estilo japonés.",
                    220000,
                    240,
                    "Tratamientos capilares",
                    serviceRepository
            );

            agregarServicio(
                    "Hidratación profunda",
                    "Tratamiento intensivo de hidratación.",
                    55000,
                    60,
                    "Tratamientos capilares",
                    serviceRepository
            );

            agregarServicio(
                    "Nutrición aceites",
                    "Tratamiento nutritivo con aceites.",
                    60000,
                    60,
                    "Tratamientos capilares",
                    serviceRepository
            );

            agregarServicio(
                    "Reconstrucción plex",
                    "Reconstrucción profunda con tecnología plex.",
                    75000,
                    75,
                    "Tratamientos capilares",
                    serviceRepository
            );

            agregarServicio(
                    "Exfoliación capilar detox",
                    "Exfoliación y limpieza profunda del cuero cabelludo.",
                    50000,
                    45,
                    "Tratamientos capilares",
                    serviceRepository
            );

            agregarServicio(
                    "Extensiones clip cortina",
                    "Instalación de extensiones tipo clip o cortina.",
                    100000,
                    90,
                    "Extensiones",
                    serviceRepository
            );

            agregarServicio(
                    "Retiro mantenimiento extensiones",
                    "Retiro y mantenimiento de extensiones.",
                    70000,
                    60,
                    "Extensiones",
                    serviceRepository
            );

            // ============================================================
            // UÑAS
            // ============================================================

            agregarServicio(
                    "Manicura express",
                    "Manicura rápida y sencilla.",
                    18000,
                    30,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Manicura clásica",
                    "Manicura tradicional completa.",
                    25000,
                    45,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Manicura semipermanente",
                    "Manicura con esmalte semipermanente.",
                    45000,
                    60,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Manicura rusa",
                    "Manicura rusa de precisión.",
                    60000,
                    75,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Manicura spa",
                    "Manicura spa con exfoliación e hidratación.",
                    55000,
                    75,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Pedicura express",
                    "Pedicura rápida y sencilla.",
                    25000,
                    30,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Pedicura clásica",
                    "Pedicura tradicional completa.",
                    35000,
                    45,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Pedicura profunda podológica",
                    "Pedicura profunda con cuidado especializado.",
                    60000,
                    75,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Pedicura spa",
                    "Pedicura spa con exfoliación e hidratación.",
                    65000,
                    90,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Pedicura semipermanente",
                    "Pedicura con esmalte semipermanente.",
                    55000,
                    60,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Uñas acrílicas set",
                    "Aplicación completa de uñas acrílicas.",
                    120000,
                    150,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Uñas gel set",
                    "Aplicación completa de uñas en gel.",
                    110000,
                    150,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Uñas polygel",
                    "Aplicación profesional de uñas polygel.",
                    120000,
                    150,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Retoque relleno acrílico gel",
                    "Retoque y relleno de uñas acrílicas o gel.",
                    70000,
                    90,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Baño acrílico gel",
                    "Baño protector con acrílico o gel.",
                    60000,
                    75,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Retiro acrílico gel",
                    "Retiro seguro de material acrílico o gel.",
                    30000,
                    45,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Nail art",
                    "Diseños personalizados para las uñas.",
                    15000,
                    30,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Cristales pedrería",
                    "Aplicación de cristales y pedrería.",
                    20000,
                    30,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Efectos especiales uñas",
                    "Aplicación de efectos especiales para uñas.",
                    20000,
                    30,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Parafina manos pies",
                    "Tratamiento de parafina para manos y pies.",
                    35000,
                    45,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Reparación uña rota",
                    "Reparación de una uña rota.",
                    10000,
                    20,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Exfoliación sales marinas",
                    "Exfoliación con sales marinas.",
                    30000,
                    30,
                    "Uñas",
                    serviceRepository
            );

            agregarServicio(
                    "Esmaltado infantil",
                    "Esmaltado especial para niñas.",
                    20000,
                    30,
                    "Uñas",
                    serviceRepository
            );

            // ============================================================
            // FACIAL
            // ============================================================

            agregarServicio(
                    "Limpieza facial básica",
                    "Limpieza facial básica.",
                    50000,
                    60,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Limpieza facial profunda",
                    "Limpieza facial profunda.",
                    80000,
                    90,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Hidratación facial intensiva",
                    "Tratamiento intensivo de hidratación facial.",
                    65000,
                    60,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Tratamiento antiedad colágeno",
                    "Tratamiento facial antiedad con colágeno.",
                    90000,
                    90,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Peeling químico",
                    "Tratamiento de peeling químico.",
                    100000,
                    75,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Microdermabrasión punta diamante",
                    "Microdermoabrasión con punta de diamante.",
                    90000,
                    75,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Tratamiento acné",
                    "Tratamiento especializado para piel con acné.",
                    85000,
                    75,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Tratamiento despigmentante",
                    "Tratamiento para mejorar manchas y pigmentación.",
                    95000,
                    90,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Lifting facial radiofrecuencia",
                    "Tratamiento facial con radiofrecuencia.",
                    110000,
                    90,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Mascarilla oro casmara",
                    "Tratamiento facial con mascarilla de oro.",
                    85000,
                    60,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Nutrición facial vitaminas",
                    "Tratamiento nutritivo con vitaminas.",
                    70000,
                    60,
                    "Facial",
                    serviceRepository
            );

            // ============================================================
            // MAQUILLAJE
            // ============================================================

            agregarServicio(
                    "Maquillaje social día",
                    "Maquillaje profesional para eventos de día.",
                    70000,
                    60,
                    "Maquillaje",
                    serviceRepository
            );

            agregarServicio(
                    "Maquillaje social noche",
                    "Maquillaje profesional para eventos de noche.",
                    85000,
                    75,
                    "Maquillaje",
                    serviceRepository
            );

            agregarServicio(
                    "Maquillaje novias",
                    "Maquillaje profesional especializado para novias.",
                    150000,
                    120,
                    "Maquillaje",
                    serviceRepository
            );

            agregarServicio(
                    "Maquillaje fotografía tv",
                    "Maquillaje profesional para fotografía y televisión.",
                    120000,
                    90,
                    "Maquillaje",
                    serviceRepository
            );

            agregarServicio(
                    "Pestañas postizas tira",
                    "Aplicación de pestañas postizas en tira.",
                    30000,
                    30,
                    "Maquillaje",
                    serviceRepository
            );

            agregarServicio(
                    "Automaquillaje personalizado",
                    "Clase personalizada de automaquillaje.",
                    90000,
                    90,
                    "Maquillaje",
                    serviceRepository
            );

            // ============================================================
            // CEJAS Y PESTAÑAS
            // ============================================================

            agregarServicio(
                    "Diseño cejas pinza",
                    "Diseño de cejas con pinza.",
                    20000,
                    30,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Depilación cejas cera",
                    "Depilación y diseño de cejas con cera.",
                    22000,
                    30,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Tintura cejas henna",
                    "Tintura de cejas con henna.",
                    40000,
                    45,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Planchado laminado cejas",
                    "Laminado y diseño profesional de cejas.",
                    55000,
                    60,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Pestañas pelo a pelo",
                    "Extensiones de pestañas técnica pelo a pelo.",
                    120000,
                    150,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Pestañas volumen ruso",
                    "Extensiones de pestañas con técnica volumen ruso.",
                    150000,
                    180,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Pestañas efecto híbrido",
                    "Extensiones de pestañas con efecto híbrido.",
                    135000,
                    165,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Retoque extensiones pestañas",
                    "Retoque de extensiones de pestañas.",
                    80000,
                    90,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Retiro extensiones pestañas",
                    "Retiro profesional de extensiones de pestañas.",
                    35000,
                    45,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Lifting pestañas",
                    "Lifting profesional de pestañas.",
                    60000,
                    60,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Permanente pestañas",
                    "Permanente profesional de pestañas.",
                    60000,
                    60,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Tinte pestañas",
                    "Tintura profesional de pestañas.",
                    30000,
                    30,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Lash lift tinte",
                    "Lash lift combinado con tinte de pestañas.",
                    75000,
                    75,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Botox pestañas",
                    "Tratamiento nutritivo de botox para pestañas.",
                    65000,
                    60,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Diseño cejas hilo",
                    "Diseño profesional de cejas con hilo.",
                    25000,
                    30,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Depilación bozo hilo",
                    "Depilación del bozo con hilo.",
                    15000,
                    20,
                    "Depilación",
                    serviceRepository
            );

            agregarServicio(
                    "Microblading cejas",
                    "Diseño semipermanente de cejas mediante microblading.",
                    250000,
                    150,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Microshading cejas",
                    "Diseño semipermanente de cejas mediante microshading.",
                    280000,
                    180,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Retoque cejas semipermanentes",
                    "Retoque de pigmentación de cejas semipermanentes.",
                    120000,
                    90,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Remoción pigmento cejas",
                    "Remoción profesional de pigmento de cejas.",
                    100000,
                    90,
                    "Cejas y pestañas",
                    serviceRepository
            );

            agregarServicio(
                    "Mascarilla contorno ojos",
                    "Tratamiento para hidratar y cuidar el contorno de ojos.",
                    45000,
                    45,
                    "Facial",
                    serviceRepository
            );

            agregarServicio(
                    "Maquillaje express ojos",
                    "Maquillaje rápido y profesional para ojos.",
                    40000,
                    30,
                    "Maquillaje",
                    serviceRepository
            );
        };
    }

    // ============================================================
    // CREAR O ACTUALIZAR SERVICIO
    // ============================================================

    private void agregarServicio(
            String nombre,
            String descripcion,
            int precio,
            int duracion,
            String categoria,
            ServiceRepository serviceRepository) {

        Service service = serviceRepository.findByName(nombre).orElse(null);

        if (service == null) {
            service = new Service();
        }

        service.setName(nombre);
        service.setDescription(descripcion);
        service.setPrice(BigDecimal.valueOf(precio));
        service.setDuration(duracion);
        service.setCategory(categoria);
        service.setStatus(true);

        String imagen = obtenerImagen(nombre);

        service.setImage("/img/servicios/" + imagen);

        serviceRepository.save(service);
    }

    // ============================================================
    // IMÁGENES
    // ============================================================

    private String obtenerImagen(String nombre) {

        return switch (nombre) {

            // ========================================================
            // CABELLO
            // ========================================================

            case "Corte de cabello dama" ->
                    "corte-cabello-dama.jpg";

            case "Corte de flequillo" ->
                    "corte-flequillo.jpg";

            case "Lavado premium" ->
                    "lavado-premium.jpg";

            case "Secado blower" ->
                    "secado-blower.jpg";

            case "Planchado cabello" ->
                    "planchado-cabello.jpg";

            case "Ondas y rizos" ->
                    "ondas-rizos.jpg";

            case "Peinado casual" ->
                    "peinado-casual.jpg";

            case "Peinado gala" ->
                    "peinado-gala.jpg";

            case "Peinado novia" ->
                    "peinado-novia.jpg";

            case "Peinado quinceañera" ->
                    "peinado-quinceanera.jpg";

            case "Tinte completo" ->
                    "tinte-completo.jpg";

            case "Retoque raíz" ->
                    "retoque-raiz.jpg";

            case "Tinte sin amoniaco" ->
                    "tinte-sin-amoniaco.jpg";

            case "Mechas clásicas" ->
                    "mechas-clasicas.jpg";

            case "Balayage" ->
                    "balayage.jpg";

            case "Babylights" ->
                    "babylights.jpg";

            case "Ombré californianas" ->
                    "ombre-californianas.jpg";

            case "Matiz toner" ->
                    "matiz-toner.jpg";

            case "Decoloración global" ->
                    "decoloracion-global.jpg";

            case "Alisado keratina" ->
                    "alisado-keratina.jpg";

            case "Alisado japonés" ->
                    "alisado-japones.jpg";

            case "Hidratación profunda" ->
                    "hidratacion-profunda.jpg";

            case "Nutrición aceites" ->
                    "nutricion-aceites.jpg";

            case "Reconstrucción plex" ->
                    "reconstruccion-plex.jpg";

            case "Exfoliación capilar detox" ->
                    "exfoliacion-capilar-detox.jpg";

            case "Extensiones clip cortina" ->
                    "extensiones-clip-cortina.jpg";

            case "Retiro mantenimiento extensiones" ->
                    "retiro-mantenimiento-extensiones.jpg";

            // ========================================================
            // UÑAS
            // ========================================================

            case "Manicura express" ->
                    "manicura-express.jpg";

            case "Manicura clásica" ->
                    "manicura-clasica.jpg";

            case "Manicura semipermanente" ->
                    "manicura-semipermanente.jpg";

            case "Manicura rusa" ->
                    "manicura-rusa.jpg";

            case "Manicura spa" ->
                    "manicura-spa.jpg";

            case "Pedicura express" ->
                    "pedicura-express.jpg";

            case "Pedicura clásica" ->
                    "pedicura-clasica.jpg";

            case "Pedicura profunda podológica" ->
                    "pedicura-profunda-podologica.jpg";

            case "Pedicura spa" ->
                    "pedicura-spa.jpg";

            case "Pedicura semipermanente" ->
                    "pedicura-semipermanente.jpg";

            case "Uñas acrílicas set" ->
                    "unas-acrilicas-set.jpg";

            case "Uñas gel set" ->
                    "unas-gel-set.jpg";

            case "Uñas polygel" ->
                    "unas-polygel.jpg";

            case "Retoque relleno acrílico gel" ->
                    "retoque-relleno-acrilico-gel.jpg";

            case "Baño acrílico gel" ->
                    "bano-acrilico-gel.jpg";

            case "Retiro acrílico gel" ->
                    "retiro-acrilico-gel.jpg";

            case "Nail art" ->
                    "nail-art.jpg";

            case "Cristales pedrería" ->
                    "cristales-pedreria.jpg";

            case "Efectos especiales uñas" ->
                    "efectos-especiales-unas.jpg";

            case "Parafina manos pies" ->
                    "parafina-manos-pies.jpg";

            case "Reparación uña rota" ->
                    "reparacion-una-rota.jpg";

            case "Exfoliación sales marinas" ->
                    "exfoliacion-sales-marinas.jpg";

            case "Esmaltado infantil" ->
                    "esmaltado-infantil.jpg";

            // ========================================================
            // FACIAL
            // ========================================================

            case "Limpieza facial básica" ->
                    "limpieza-facial-basica.jpg";

            case "Limpieza facial profunda" ->
                    "limpieza-facial-profunda.jpg";

            case "Hidratación facial intensiva" ->
                    "hidratacion-facial-intensiva.jpg";

            case "Tratamiento antiedad colágeno" ->
                    "tratamiento-antiedad-colageno.jpg";

            case "Peeling químico" ->
                    "peeling-quimico.jpg";

            case "Microdermabrasión punta diamante" ->
                    "microdermabrasion-punta-diamante.jpg";

            case "Tratamiento acné" ->
                    "tratamiento-acne.jpg";

            case "Tratamiento despigmentante" ->
                    "tratamiento-despigmentante.jpg";

            case "Lifting facial radiofrecuencia" ->
                    "lifting-facial-radiofrecuencia.jpg";

            case "Mascarilla oro casmara" ->
                    "mascarilla-oro-casmara.jpg";

            case "Nutrición facial vitaminas" ->
                    "nutricion-facial-vitaminas.jpg";

            case "Mascarilla contorno ojos" ->
                    "mascarilla-contorno-ojos.jpg";

            // ========================================================
            // MAQUILLAJE
            // ========================================================

            case "Maquillaje social día" ->
                    "maquillaje-social-dia.jpg";

            case "Maquillaje social noche" ->
                    "maquillaje-social-noche.jpg";

            case "Maquillaje novias" ->
                    "maquillaje-novias.jpg";

            case "Maquillaje fotografía tv" ->
                    "maquillaje-fotografia-tv.jpg";

            case "Pestañas postizas tira" ->
                    "pestanas-postizas-tira.jpg";

            case "Automaquillaje personalizado" ->
                    "automaquillaje-personalizado.jpg";

            case "Maquillaje express ojos" ->
                    "maquillaje-express-ojos.jpg";

            // ========================================================
            // CEJAS Y PESTAÑAS
            // ========================================================

            case "Diseño cejas pinza" ->
                    "diseno-cejas-pinza.jpg";

            case "Depilación cejas cera" ->
                    "depilacion-cejas-cera.jpg";

            case "Tintura cejas henna" ->
                    "tintura-cejas-henna.jpg";

            case "Planchado laminado cejas" ->
                    "planchado-laminado-cejas.jpg";

            case "Pestañas pelo a pelo" ->
                    "pestanas-pelo-a-pelo.jpg";

            case "Pestañas volumen ruso" ->
                    "pestanas-volumen-ruso.jpg";

            case "Pestañas efecto híbrido" ->
                    "pestanas-efecto-hibrido.jpg";

            case "Retoque extensiones pestañas" ->
                    "retoque-extensiones-pestanas.jpg";

            case "Retiro extensiones pestañas" ->
                    "retiro-extensiones-pestanas.jpg";

            case "Lifting pestañas" ->
                    "lifting-pestanas.jpg";

            case "Permanente pestañas" ->
                    "permanente-pestanas.jpg";

            case "Tinte pestañas" ->
                    "tinte-pestanas.jpg";

            case "Lash lift tinte" ->
                    "lash-lift-tinte.jpg";

            case "Botox pestañas" ->
                    "botox-pestanas.jpg";

            case "Diseño cejas hilo" ->
                    "diseno-cejas-hilo.jpg";

            case "Depilación bozo hilo" ->
                    "depilacion-bozo-hilo.jpg";

            case "Microblading cejas" ->
                    "microblading-cejas.jpg";

            case "Microshading cejas" ->
                    "microshading-cejas.jpg";

            case "Retoque cejas semipermanentes" ->
                    "retoque-cejas-semipermanentes.jpg";

            case "Remoción pigmento cejas" ->
                    "remocion-pigmento-cejas.jpg";

            default ->
                    "corte-cabello-dama.jpg";
        };
    }
}