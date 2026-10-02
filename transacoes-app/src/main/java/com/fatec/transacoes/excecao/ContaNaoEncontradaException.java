package com.fatec.transacoes.excecao;

public class ContaNaoEncontradaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ContaNaoEncontradaException(Long id) {
        super("Conta não encontrada: " + id);
    }
}
