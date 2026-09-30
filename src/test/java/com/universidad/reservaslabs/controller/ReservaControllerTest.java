package com.universidad.reservaslabs.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Checkpoints del Paso 9: 201, 409 por solapamiento, 400 por horario o duracion, 404 y cancelacion
@SpringBootTest
@AutoConfigureMockMvc
class ReservaControllerTest {

    @Autowired
    private MockMvc mvc;

    private Integer crearLaboratorio(String nombre) throws Exception {
        String json = mvc.perform(post("/api/laboratorios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\",\"ubicacion\":\"Bloque B\",\"capacidad\":30,\"tipo\":\"COMPUTO\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    private String reserva(Integer laboratorioId, String inicio, String fin) {
        return "{\"laboratorio\":{\"id\":" + laboratorioId + "},\"nombreSolicitante\":\"Ana Torres\","
            + "\"correoSolicitante\":\"ana@udes.edu.co\",\"inicio\":\"" + inicio + "\",\"fin\":\"" + fin + "\","
            + "\"motivo\":\"Practica\"}";
    }

    @Test
    void reservaEnHorarioLibreRetorna201Confirmada() throws Exception {
        Integer lab = crearLaboratorio("Lab REST 1");
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T09:00:00", "2030-08-10T11:00:00")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    void reservaSolapadaRetorna409() throws Exception {
        Integer lab = crearLaboratorio("Lab REST 2");
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T09:00:00", "2030-08-10T11:00:00")))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T10:00:00", "2030-08-10T12:00:00")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("El laboratorio Lab REST 2 ya tiene una reserva en ese horario"));
    }

    @Test
    void reservaFueraDelHorarioDeAtencionRetorna400() throws Exception {
        Integer lab = crearLaboratorio("Lab REST 3");
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T22:00:00", "2030-08-10T23:00:00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("La reserva debe estar dentro del horario de atención (07:00 - 21:00)"));
    }

    @Test
    void reservaConDuracionMayorATresHorasRetorna400() throws Exception {
        Integer lab = crearLaboratorio("Lab REST 4");
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T08:00:00", "2030-08-10T12:00:00")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void reservaSinSolicitanteRetorna400PorValidacion() throws Exception {
        Integer lab = crearLaboratorio("Lab REST 5");
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T09:00:00", "2030-08-10T10:00:00").replace("Ana Torres", "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errores").exists());
    }

    @Test
    void laboratorioInexistenteRetorna404() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(999999, "2030-08-10T09:00:00", "2030-08-10T10:00:00")))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/laboratorios/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void cancelarLiberaElHorario() throws Exception {
        Integer lab = crearLaboratorio("Lab REST 6");
        String json = mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T09:00:00", "2030-08-10T11:00:00")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(json, "$.id");

        mvc.perform(delete("/api/reservas/" + id)).andExpect(status().isNoContent());
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(reserva(lab, "2030-08-10T10:00:00", "2030-08-10T11:00:00")))
            .andExpect(status().isCreated());
    }
}
