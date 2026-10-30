package com.shalom.controller;

import com.shalom.model.*;
import com.shalom.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class DashboardController {

    public record ServicePopularity(Service service, long count, int percentage) {}

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final InventoryRepository inventoryRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;

    public DashboardController(UserRepository userRepository,
                               AppointmentRepository appointmentRepository,
                               ServiceRepository serviceRepository,
                               InventoryRepository inventoryRepository,
                               LoyaltyCardRepository loyaltyCardRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.inventoryRepository = inventoryRepository;
        this.loyaltyCardRepository = loyaltyCardRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", user);

        boolean isAdmin = user.getRole() != null && "admin".equalsIgnoreCase(user.getRole().getName());
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            long totalClientes = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && !"admin".equalsIgnoreCase(u.getRole().getName()))
                    .count();

            LocalDate today = LocalDate.now();
            List<Appointment> allAppointments = appointmentRepository.findAll();
            long totalCitas = allAppointments.size();

            long citasHoy = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().isEqual(today))
                    .count();

            long serviciosRealizados = allAppointments.stream()
                    .filter(a -> "confirmada".equalsIgnoreCase(a.getStatus()) || "completada".equalsIgnoreCase(a.getStatus()))
                    .count();

            long totalProductos = inventoryRepository.count();
            long totalServicios = serviceRepository.count();

            // Conteo real por estado
            long confirmadas = allAppointments.stream()
                    .filter(a -> "confirmada".equalsIgnoreCase(a.getStatus()))
                    .count();
            long pendientes = allAppointments.stream()
                    .filter(a -> "pendiente".equalsIgnoreCase(a.getStatus()))
                    .count();
            long canceladas = allAppointments.stream()
                    .filter(a -> "cancelada".equalsIgnoreCase(a.getStatus()))
                    .count();

            // Citas del día desglosadas por estado
            long citasHoyConfirmadas = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().isEqual(today) && "confirmada".equalsIgnoreCase(a.getStatus()))
                    .count();
            long citasHoyPendientes = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().isEqual(today) && "pendiente".equalsIgnoreCase(a.getStatus()))
                    .count();
            long citasHoyCanceladas = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().isEqual(today) && "cancelada".equalsIgnoreCase(a.getStatus()))
                    .count();

            // Distribución real de citas por día (últimos 8 días) para el gráfico
            LocalDate start = today.minusDays(7);
            List<LocalDate> chartDays = new ArrayList<>();
            Map<LocalDate, Long> byDay = new LinkedHashMap<>();
            for (int i = 0; i <= 7; i++) {
                LocalDate d = start.plusDays(i);
                chartDays.add(d);
                byDay.put(d, 0L);
            }
            for (Appointment a : allAppointments) {
                if (a.getDate() != null && byDay.containsKey(a.getDate())) {
                    byDay.merge(a.getDate(), 1L, Long::sum);
                }
            }
            List<Long> chartCounts = chartDays.stream().map(byDay::get).toList();
            List<String> chartLabels = chartDays.stream()
                    .map(d -> d.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "ES")))
                    .toList();

            // Tendencia real: citas de hoy vs las del mismo día en la semana pasada (si existen)
            long citasSemanaAnterior = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().isEqual(today.minusWeeks(1)))
                    .count();
            String trendCitas;
            boolean trendUp;
            if (citasSemanaAnterior > 0) {
                long diff = citasHoy - citasSemanaAnterior;
                long pct = Math.abs(diff) * 100 / citasSemanaAnterior;
                trendUp = diff >= 0;
                trendCitas = (trendUp ? "+" : "-") + pct + "% vs hace una semana";
            } else {
                trendUp = citasHoy > 0;
                trendCitas = citasHoy > 0 ? "Nuevas citas hoy" : "Sin cambios";
            }

            // Cálculo dinámico de ventas reales
            BigDecimal totalVentas = allAppointments.stream()
                    .filter(a -> "confirmada".equalsIgnoreCase(a.getStatus()) || "completada".equalsIgnoreCase(a.getStatus()))
                    .map(a -> a.getService() != null && a.getService().getPrice() != null ? a.getService().getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal ticketPromedio = BigDecimal.ZERO;
            long divisor = serviciosRealizados;
            if (divisor > 0 && totalVentas.compareTo(BigDecimal.ZERO) > 0) {
                ticketPromedio = totalVentas.divide(BigDecimal.valueOf(divisor), 0, java.math.RoundingMode.HALF_UP);
            }

            DecimalFormat currencyFormatter = new DecimalFormat("$#,###", new DecimalFormatSymbols(new Locale("es", "CO")));
            String ventasFormateadas = currencyFormatter.format(totalVentas);
            String ticketFormateado = currencyFormatter.format(ticketPromedio);

            // Citas para mostrar en la tabla (de hoy si existen, o las más recientes)
            List<Appointment> citasHoyList = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().isEqual(today))
                    .sorted(Comparator.comparing(Appointment::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            List<Appointment> ultimasCitas = !citasHoyList.isEmpty() ? citasHoyList : appointmentRepository.findAllByOrderByDateDescTimeDesc().stream().limit(5).toList();

            List<Inventory> productosBajoStock = inventoryRepository.findLowStock().stream()
                    .limit(5).toList();

            List<Service> servicios = serviceRepository.findAll();
            long maxCount = 1;
            for (Service s : servicios) {
                long count = allAppointments.stream().filter(a -> a.getService() != null && s.getId().equals(a.getService().getId())).count();
                if (count > maxCount) maxCount = count;
            }
            final long fMax = maxCount;
            List<ServicePopularity> serviciosPopulares = servicios.stream()
                    .map(s -> {
                        long count = allAppointments.stream().filter(a -> a.getService() != null && s.getId().equals(a.getService().getId())).count();
                        int pct = (int) Math.round((double) count * 100.0 / fMax);
                        return new ServicePopularity(s, count, pct);
                    })
                    .limit(5)
                    .toList();

            model.addAttribute("usuarios", totalClientes);
            model.addAttribute("citas", totalCitas);
            model.addAttribute("servicios", totalServicios);
            model.addAttribute("productos", totalProductos);
            model.addAttribute("citasHoy", citasHoy);
            model.addAttribute("serviciosRealizados", serviciosRealizados);
            model.addAttribute("totalVentas", ventasFormateadas);
            model.addAttribute("ticketPromedio", ticketFormateado);
            model.addAttribute("ultimasCitas", ultimasCitas);
            model.addAttribute("productosBajoStock", productosBajoStock);
            model.addAttribute("serviciosPopulares", serviciosPopulares);
            model.addAttribute("confirmadas", confirmadas);
            model.addAttribute("pendientes", pendientes);
            model.addAttribute("canceladas", canceladas);
            model.addAttribute("citasHoyConfirmadas", citasHoyConfirmadas);
            model.addAttribute("citasHoyPendientes", citasHoyPendientes);
            model.addAttribute("citasHoyCanceladas", citasHoyCanceladas);
            model.addAttribute("chartLabels", chartLabels);
            model.addAttribute("chartCounts", chartCounts);
            model.addAttribute("trendCitas", trendCitas);
            model.addAttribute("trendCitasUp", trendUp);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            model.addAttribute("fechaHoy", today.format(formatter));

            return "dashboard";
        } else {
            List<Appointment> misCitas = appointmentRepository.findByUserIdOrderByDateDescTimeDesc(user.getId());
            model.addAttribute("misCitas", misCitas);
            model.addAttribute("totalCitas", misCitas.size());

            long completadas = misCitas.stream()
                    .filter(c -> "confirmada".equalsIgnoreCase(c.getStatus()) || "completada".equalsIgnoreCase(c.getStatus()))
                    .count();
            model.addAttribute("citasCompletadas", completadas);

            LocalDate today = LocalDate.now();
            Appointment proximaCita = misCitas.stream()
                    .filter(c -> c.getDate() != null && !c.getDate().isBefore(today) && !"cancelada".equalsIgnoreCase(c.getStatus()))
                    .min(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);
            model.addAttribute("proximaCita", proximaCita);

            LoyaltyCard card = loyaltyCardRepository.findByUserId(user.getId()).orElseGet(() -> {
                LoyaltyCard nc = new LoyaltyCard();
                nc.setUser(user);
                nc.setStamps(0);
                nc.setAvailableRewards(0);
                return loyaltyCardRepository.save(nc);
            });
            model.addAttribute("card", card);
            model.addAttribute("remainingStamps", Math.max(0, 9 - card.getStamps()));
            model.addAttribute("visitas", completadas);

            model.addAttribute("servicios", serviceRepository.findAll().stream().limit(6).toList());

            return "client/dashboard";
        }
    }
}
