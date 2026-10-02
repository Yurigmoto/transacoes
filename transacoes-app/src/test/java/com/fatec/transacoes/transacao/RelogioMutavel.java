package com.fatec.transacoes.transacao;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Relógio controlável para os testes: começa em 02/10/2026 12:00 UTC e só anda quando mandamos. */
class RelogioMutavel extends Clock {

    private Instant instante = Instant.parse("2026-10-02T12:00:00Z");

    void avancarMinutos(long minutos) {
        instante = instante.plus(Duration.ofMinutes(minutos));
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
