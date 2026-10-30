package com.shalom.controller;

import com.shalom.model.Appointment;
import com.shalom.model.LoyaltyCard;
import com.shalom.model.Service;
import com.shalom.model.User;
import com.shalom.repository.AppointmentRepository;
import com.shalom.repository.LoyaltyCardRepository;
import com.shalom.repository.ServiceRepository;
import com.shalom.repository.SpecialistRepository;
import com.shalom.repository.UserRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final SpecialistRepository specialistRepository;
    private final UserRepository userRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;

    /*
     * HORARIO REAL DEL SALÓN
     *
     * Lunes a sábado
     * 8:00 AM - 7:00 PM
     *
     * Domingo cerrado.
     */

    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(19, 0);

    public AppointmentController(
            AppointmentRepository appointmentRepository,
            ServiceRepository serviceRepository,
            SpecialistRepository specialistRepository,
            UserRepository userRepository,
            LoyaltyCardRepository loyaltyCardRepository) {

        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.specialistRepository = specialistRepository;
        this.userRepository = userRepository;
        this.loyaltyCardRepository = loyaltyCardRepository;
    }

    /*
     * Verifica si el usuario es administrador.
     */
    private boolean isAdmin(User user) {

        return user.getRole() != null
                && "admin".equalsIgnoreCase(user.getRole().getName());
    }

    /*
     * ============================================================
     * VALIDACIÓN PRINCIPAL DE HORARIO
     * ============================================================
     *
     * Esta función comprueba:
     *
     * 1. La fecha no sea anterior a hoy.
     * 2. Si es hoy, la hora no haya pasado.
     * 3. Domingo esté cerrado.
     * 4. La cita empiece después de las 8:00 AM.
     * 5. La cita termine antes de las 7:00 PM.
     * 6. La cita tenga duración válida.
     */

    private String validateBusinessHours(
            LocalDate date,
            LocalTime startTime,
            Integer durationMinutes) {

        if (date == null || startTime == null) {
            return "Debes seleccionar una fecha y una hora.";
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        /*
         * No permitir fechas anteriores.
         */
        if (date.isBefore(today)) {

            return "No puedes reservar una cita en una fecha anterior a hoy.";
        }

        /*
         * No permitir horas pasadas si la cita es para hoy.
         */
        if (date.equals(today) && !startTime.isAfter(now)) {

            return "No puedes reservar una hora que ya pasó.";
        }

        /*
         * Domingo cerrado.
         */
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {

            return "El salón no atiende los domingos.";
        }

        /*
         * Duración por defecto.
         */
        if (durationMinutes == null || durationMinutes <= 0) {

            durationMinutes = 30;
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        /*
         * La cita debe comenzar dentro del horario.
         */
        if (startTime.isBefore(OPENING_TIME)) {

            return "El salón abre a las 8:00 a. m.";
        }

        /*
         * La cita completa debe terminar antes del cierre.
         */
        if (endTime.isAfter(CLOSING_TIME)) {

            return "La cita no puede terminar después de las 7:00 p. m.";
        }

        return null;
    }

    /*
     * ============================================================
     * VALIDACIÓN DE CRUCE DE CITAS
     * ============================================================
     *
     * Comprueba si una nueva cita se cruza con otra existente.
     *
     * Las citas canceladas NO bloquean el horario.
     *
     * Si hay especialista seleccionada:
     * se comprueba específicamente ese especialista.
     *
     * Si no hay especialista:
     * se comprueba el horario general del salón.
     */

    private boolean hasConflict(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String worker,
            Long appointmentId) {

        List<Appointment> appointments =
                appointmentRepository.findAll();

        for (Appointment existing : appointments) {

            /*
             * No comparar la cita consigo misma al editar.
             */
            if (appointmentId != null
                    && existing.getId().equals(appointmentId)) {

                continue;
            }

            /*
             * Solo citas del mismo día.
             */
            if (!date.equals(existing.getDate())) {

                continue;
            }

            /*
             * Las citas canceladas no bloquean.
             */
            if ("cancelada".equalsIgnoreCase(existing.getStatus())) {

                continue;
            }

            /*
             * Si ambas tienen especialista,
             * deben ser la misma para que exista conflicto.
             *
             * Si la nueva cita no tiene especialista,
             * consideramos ocupado el horario general.
             */

            if (worker != null && !worker.isBlank()) {

                if (existing.getWorker() == null
                        || !worker.equalsIgnoreCase(existing.getWorker())) {

                    continue;
                }

            }

            /*
             * Duración de la cita existente.
             */
            int existingDuration = 30;

            if (existing.getService() != null
                    && existing.getService().getDuration() != null
                    && existing.getService().getDuration() > 0) {

                existingDuration =
                        existing.getService().getDuration();
            }

            LocalTime existingStart = existing.getTime();

            LocalTime existingEnd =
                    existingStart.plusMinutes(existingDuration);

            /*
             * Regla de cruce:
             *
             * nueva empieza antes de que termine la existente
             * Y
             * nueva termina después de que empiece la existente.
             */

            boolean overlaps =
                    startTime.isBefore(existingEnd)
                            && endTime.isAfter(existingStart);

            if (overlaps) {

                return true;
            }
        }

        return false;
    }

    /*
     * ============================================================
     * LISTADO DE CITAS
     * ============================================================
     */

    @GetMapping
    public String index(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user =
                userRepository
                        .findByEmail(userDetails.getUsername())
                        .orElseThrow();

        boolean admin = isAdmin(user);

        model.addAttribute("isAdmin", admin);

        if (admin) {

            model.addAttribute(
                    "appointments",
                    appointmentRepository
                            .findAllByOrderByDateDescTimeDesc());

        } else {

            List<Appointment> citas =
                    appointmentRepository
                            .findByUserIdOrderByDateDescTimeDesc(
                                    user.getId());

            model.addAttribute("appointments", citas);

            LocalDate today = LocalDate.now();

            List<Appointment> proximas =
                    citas.stream()
                            .filter(c ->
                                    c.getDate() != null
                                            && !"cancelada".equalsIgnoreCase(
                                            c.getStatus())
                                            && !c.getDate().isBefore(today))
                            .sorted(
                                    Comparator
                                            .comparing(Appointment::getDate)
                                            .thenComparing(
                                                    Appointment::getTime,
                                                    Comparator.nullsLast(
                                                            Comparator.naturalOrder())))
                            .toList();

            List<Appointment> historial =
                    citas.stream()
                            .filter(c ->
                                    c.getDate() == null
                                            || c.getDate().isBefore(today)
                                            || "cancelada".equalsIgnoreCase(
                                            c.getStatus()))
                            .sorted(
                                    Comparator
                                            .comparing(
                                                    Appointment::getDate,
                                                    Comparator.nullsLast(
                                                            Comparator.reverseOrder()))
                                            .thenComparing(
                                                    Appointment::getTime,
                                                    Comparator.nullsLast(
                                                            Comparator.naturalOrder())))
                            .toList();

            model.addAttribute(
                    "proximasCitas",
                    proximas);

            model.addAttribute(
                    "historialCitas",
                    historial);
        }

        return "appointments/index";
    }

    /*
     * ============================================================
     * FORMULARIO CREAR
     * ============================================================
     */

    @GetMapping("/create")
    public String create(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user =
                userRepository
                        .findByEmail(userDetails.getUsername())
                        .orElseThrow();

        boolean admin = isAdmin(user);

        model.addAttribute("isAdmin", admin);

        model.addAttribute(
                "services",
                serviceRepository.findAll()
                        .stream()
                        .filter(Service::isStatus)
                        .toList());

        model.addAttribute(
                "specialists",
                specialistRepository.findAll());

        /*
         * Información del horario para la vista.
         */
        model.addAttribute(
                "openingTime",
                OPENING_TIME);

        model.addAttribute(
                "closingTime",
                CLOSING_TIME);

        model.addAttribute(
                "today",
                LocalDate.now());

        if (admin) {

            List<User> clients =
                    userRepository.findAll()
                            .stream()
                            .filter(u -> !isAdmin(u))
                            .toList();

            model.addAttribute(
                    "clients",
                    clients);
        }

        return "appointments/create";
    }

    /*
     * ============================================================
     * CREAR CITA
     * ============================================================
     */

    @PostMapping
    public String store(
            @AuthenticationPrincipal UserDetails userDetails,

            @RequestParam(required = false)
            Long user_id,

            @RequestParam
            Long service_id,

            @RequestParam
            String date,

            @RequestParam
            String time,

            @RequestParam(required = false)
            String worker,

            RedirectAttributes redirectAttributes) {

        User loggedUser =
                userRepository
                        .findByEmail(userDetails.getUsername())
                        .orElseThrow();

        User user = loggedUser;

        /*
         * Solo el administrador puede seleccionar
         * otro cliente.
         */
        if (isAdmin(loggedUser) && user_id != null) {

            user =
                    userRepository
                            .findById(user_id)
                            .orElseThrow();
        }

        LocalDate appointmentDate;

        LocalTime appointmentTime;

        try {

            appointmentDate =
                    LocalDate.parse(date);

            appointmentTime =
                    LocalTime.parse(time);

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La fecha o la hora seleccionada no son válidas.");

            return "redirect:/appointments/create";
        }

        Service service =
                serviceRepository
                        .findById(service_id)
                        .orElseThrow();

        /*
         * Duración del servicio.
         */
        int duration =
                service.getDuration() != null
                        && service.getDuration() > 0
                        ? service.getDuration()
                        : 30;

        LocalTime endTime =
                appointmentTime.plusMinutes(duration);

        /*
         * VALIDAR HORARIO DEL SALÓN
         */
        String businessHourError =
                validateBusinessHours(
                        appointmentDate,
                        appointmentTime,
                        duration);

        if (businessHourError != null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    businessHourError);

            return "redirect:/appointments/create";
        }

        /*
         * VALIDAR CRUCE CON OTRA CITA
         */
        if (hasConflict(
                appointmentDate,
                appointmentTime,
                endTime,
                worker,
                null)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El horario seleccionado ya está ocupado. Elige otra hora.");

            return "redirect:/appointments/create";
        }

        /*
         * CREAR CITA
         */
        Appointment appointment =
                new Appointment();

        appointment.setUser(user);
        appointment.setService(service);
        appointment.setDate(appointmentDate);
        appointment.setTime(appointmentTime);
        appointment.setWorker(worker);
        appointment.setStatus("pendiente");

        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute(
                "success",
                "Cita creada correctamente.");

        return "redirect:/appointments";
    }

    /*
     * ============================================================
     * EDITAR CITA
     * ============================================================
     */

    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Appointment appointment =
                appointmentRepository
                        .findById(id)
                        .orElseThrow();

        User user =
                userRepository
                        .findByEmail(userDetails.getUsername())
                        .orElseThrow();

        if (!isAdmin(user)
                && !appointment
                .getUser()
                .getId()
                .equals(user.getId())) {

            return "redirect:/appointments";
        }

        model.addAttribute(
                "appointment",
                appointment);

        model.addAttribute(
                "services",
                serviceRepository.findAll()
                        .stream()
                        .filter(Service::isStatus)
                        .toList());

        model.addAttribute(
                "specialists",
                specialistRepository.findAll());

        model.addAttribute(
                "openingTime",
                OPENING_TIME);

        model.addAttribute(
                "closingTime",
                CLOSING_TIME);

        model.addAttribute(
                "today",
                LocalDate.now());

        return "appointments/edit";
    }

    /*
     * ============================================================
     * ACTUALIZAR CITA
     * ============================================================
     */

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,

            @AuthenticationPrincipal UserDetails userDetails,

            @RequestParam Long service_id,

            @RequestParam String date,

            @RequestParam String time,

            @RequestParam(required = false)
            String worker,

            @RequestParam(required = false)
            String status,

            RedirectAttributes redirectAttributes) {

        Appointment appointment =
                appointmentRepository
                        .findById(id)
                        .orElseThrow();

        User user =
                userRepository
                        .findByEmail(userDetails.getUsername())
                        .orElseThrow();

        if (!isAdmin(user)
                && !appointment
                .getUser()
                .getId()
                .equals(user.getId())) {

            return "redirect:/appointments";
        }

        LocalDate appointmentDate;
        LocalTime appointmentTime;

        try {

            appointmentDate =
                    LocalDate.parse(date);

            appointmentTime =
                    LocalTime.parse(time);

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La fecha o la hora no son válidas.");

            return "redirect:/appointments/"
                    + id
                    + "/edit";
        }

        Service service =
                serviceRepository
                        .findById(service_id)
                        .orElseThrow();

        int duration =
                service.getDuration() != null
                        && service.getDuration() > 0
                        ? service.getDuration()
                        : 30;

        LocalTime endTime =
                appointmentTime.plusMinutes(duration);

        /*
         * No permitir modificar una cita a un horario inválido.
         */
        String businessHourError =
                validateBusinessHours(
                        appointmentDate,
                        appointmentTime,
                        duration);

        if (businessHourError != null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    businessHourError);

            return "redirect:/appointments/"
                    + id
                    + "/edit";
        }

        /*
         * IMPORTANTE:
         *
         * Al editar ignoramos la cita actual
         * para que no se detecte como conflicto consigo misma.
         */
        if (hasConflict(
                appointmentDate,
                appointmentTime,
                endTime,
                worker,
                appointment.getId())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nuevo horario está ocupado. Selecciona otra hora.");

            return "redirect:/appointments/"
                    + id
                    + "/edit";
        }

        appointment.setService(service);
        appointment.setDate(appointmentDate);
        appointment.setTime(appointmentTime);
        appointment.setWorker(worker);

        if (isAdmin(user)
                && status != null
                && !status.isBlank()) {

            appointment.setStatus(status);
        }

        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute(
                "success",
                "Cita actualizada correctamente.");

        return "redirect:/appointments";
    }

    /*
     * ============================================================
     * CAMBIAR ESTADO
     * ============================================================
     */

    @PostMapping("/{id}/status")
    public String quickStatus(
            @PathVariable Long id,

            @RequestParam String status,

            @AuthenticationPrincipal UserDetails userDetails,

            RedirectAttributes redirectAttributes) {

        User user =
                userRepository
                        .findByEmail(userDetails.getUsername())
                        .orElseThrow();

        if (!isAdmin(user)) {

            return "redirect:/appointments";
        }

        Appointment appointment =
                appointmentRepository
                        .findById(id)
                        .orElseThrow();

        appointment.setStatus(status);

        /*
         * Lógica de tarjeta de fidelidad.
         */
        if ("confirmada".equalsIgnoreCase(status)
                && appointment.getLoyaltyStampedAt() == null) {

            if (loyaltyCardRepository
                    .findByUserId(
                            appointment.getUser().getId())
                    .isEmpty()) {

                LoyaltyCard nc =
                        new LoyaltyCard();

                nc.setUser(
                        appointment.getUser());

                nc.setStamps(0);
                nc.setAvailableRewards(0);

                loyaltyCardRepository.save(nc);
            }

            LoyaltyCard card =
                    loyaltyCardRepository
                            .findByUserId(
                                    appointment
                                            .getUser()
                                            .getId())
                            .orElseThrow();

            int rewardsBefore =
                    card.getAvailableRewards();

            card.addStamp();

            loyaltyCardRepository.save(card);

            appointment.setLoyaltyStampedAt(
                    LocalDateTime.now());

            appointmentRepository.save(
                    appointment);

            if (card.getAvailableRewards()
                    > rewardsBefore) {

                redirectAttributes.addFlashAttribute(
                        "success",
                        "Cita confirmada. La tarjeta se completó y la clienta tiene un servicio gratuito.");

            } else {

                redirectAttributes.addFlashAttribute(
                        "success",
                        "Cita confirmada correctamente.");
            }

        } else {

            appointmentRepository.save(
                    appointment);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado actualizado correctamente.");
        }

        return "redirect:/appointments";
    }
}