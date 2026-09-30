package com.universidad.reservaslabs.exception;

// Datos de la reserva que violan una regla propia (horario de atencion, duracion, rango de fechas).
// Se distingue de ReservaConflictException porque no choca con otra reserva: la peticion misma es invalida (400).
public class ReservaInvalidaException extends RuntimeException {
    public ReservaInvalidaException(String mensaje) { super(mensaje); }
}
