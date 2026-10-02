package com.fatec.transacoes.conta;

import java.math.BigDecimal;

import com.fatec.transacoes.excecao.SaldoInsuficienteException;

public class Conta {

    private Long id;
    private final String titular;
    private BigDecimal saldo;
    private boolean ativa = true;

    public Conta(String titular, BigDecimal saldoInicial) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException("Titular obrigatório");
        }
        if (saldoInicial == null || saldoInicial.signum() < 0 || temMaisDeDuasCasas(saldoInicial)) {
            throw new IllegalArgumentException("Saldo inicial inválido");
        }
        this.titular = titular.trim();
        this.saldo = saldoInicial.setScale(2);
    }

    public void depositar(BigDecimal valor) {
        validarValor(valor);
        this.saldo = this.saldo.add(valor);
    }

    public void sacar(BigDecimal valor) {
        validarValor(valor);
        if (this.saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente");
        }
        this.saldo = this.saldo.subtract(valor);
    }

    public static void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Valor inválido");
        }
        if (temMaisDeDuasCasas(valor)) {
            throw new IllegalArgumentException("Valor com mais de duas casas decimais");
        }
    }

    private static boolean temMaisDeDuasCasas(BigDecimal valor) {
        return valor.stripTrailingZeros().scale() > 2;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }
    public boolean isAtiva() { return ativa; }
    public void ativar() { this.ativa = true; }
    public void desativar() { this.ativa = false; }
}
