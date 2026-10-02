package com.fatec.transacoes.transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transacao {

    private Long id;
    private final TipoTransacao tipo;
    private final BigDecimal valor;
    private final BigDecimal taxa;
    private final Long contaOrigemId;
    private final Long contaDestinoId;
    private final LocalDateTime dataHora;

    public Transacao(TipoTransacao tipo, BigDecimal valor, BigDecimal taxa,
            Long contaOrigemId, Long contaDestinoId, LocalDateTime dataHora) {
        this.tipo = tipo;
        this.valor = valor;
        this.taxa = taxa;
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
        this.dataHora = dataHora;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoTransacao getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getTaxa() { return taxa; }
    public Long getContaOrigemId() { return contaOrigemId; }
    public Long getContaDestinoId() { return contaDestinoId; }
    public LocalDateTime getDataHora() { return dataHora; }
}
