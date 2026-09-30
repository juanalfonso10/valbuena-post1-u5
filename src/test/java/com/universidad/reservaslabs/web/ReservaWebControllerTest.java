package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// La vista MVC usa el mismo ReservaService: las mismas reglas y el mismo mensaje de negocio que la API REST
@SpringBootTest
@AutoConfigureMockMvc
class ReservaWebControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LaboratorioRepository laboratorioRepo;

    private String crearLaboratorio(String nombre) {
        return laboratorioRepo.save(new Laboratorio(null, nombre, "Bloque C", 25, "REDES")).getId().toString();
    }

    @Test
    void listaYFormularioSeRenderizan() throws Exception {
        crearLaboratorio("Lab Web 1");
        mvc.perform(get("/reservas")).andExpect(status().isOk()).andExpect(view().name("reservas/lista"));
        mvc.perform(get("/reservas/nueva")).andExpect(status().isOk()).andExpect(view().name("reservas/nueva"));
    }

    @Test
    void crearDesdeElFormularioRedirigeConMensaje() throws Exception {
        String lab = crearLaboratorio("Lab Web 2");
        mvc.perform(post("/reservas")
                .param("laboratorio.id", lab)
                .param("nombreSolicitante", "Luis Gomez")
                .param("correoSolicitante", "luis@udes.edu.co")
                .param("inicio", "2030-09-01T09:00")
                .param("fin", "2030-09-01T10:30")
                .param("motivo", "Proyecto de Redes"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reservas"))
            .andExpect(flash().attribute("mensaje", "Reserva creada correctamente"));
    }

    @Test
    void reservaSolapadaMuestraElMismoMensajeQueLaApiRest() throws Exception {
        String lab = crearLaboratorio("Lab Web 3");
        mvc.perform(post("/reservas").param("laboratorio.id", lab)
                .param("nombreSolicitante", "Ana").param("correoSolicitante", "ana@udes.edu.co")
                .param("inicio", "2030-09-02T09:00").param("fin", "2030-09-02T11:00"))
            .andExpect(redirectedUrl("/reservas"));

        mvc.perform(post("/reservas").param("laboratorio.id", lab)
                .param("nombreSolicitante", "Carlos").param("correoSolicitante", "carlos@udes.edu.co")
                .param("inicio", "2030-09-02T10:00").param("fin", "2030-09-02T12:00"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reservas/nueva"))
            .andExpect(flash().attribute("error", "El laboratorio Lab Web 3 ya tiene una reserva en ese horario"));
    }

    @Test
    void reservaFueraDeHorarioVuelveAlFormularioConElError() throws Exception {
        String lab = crearLaboratorio("Lab Web 4");
        mvc.perform(post("/reservas").param("laboratorio.id", lab)
                .param("nombreSolicitante", "Ana").param("correoSolicitante", "ana@udes.edu.co")
                .param("inicio", "2030-09-03T22:00").param("fin", "2030-09-03T23:00"))
            .andExpect(redirectedUrl("/reservas/nueva"))
            .andExpect(flash().attribute("error", "La reserva debe estar dentro del horario de atención (07:00 - 21:00)"));
    }
}
