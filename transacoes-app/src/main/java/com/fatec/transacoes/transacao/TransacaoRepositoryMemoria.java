package com.fatec.transacoes.transacao;

import java.util.ArrayList;
import java.util.List;

public class TransacaoRepositoryMemoria implements TransacaoRepository {

    private final List<Transacao> transacoes = new ArrayList<>();
    private long proximoId = 1;

    @Override
    public Transacao save(Transacao transacao) {
        if (transacao.getId() == null) {
            transacao.setId(proximoId++);
        }
        transacoes.add(transacao);
        return transacao;
    }

    @Override
    public List<Transacao> findByContaId(Long contaId) {
        List<Transacao> resultado = new ArrayList<>();
        for (Transacao t : transacoes) {
            if (contaId.equals(t.getContaOrigemId()) || contaId.equals(t.getContaDestinoId())) {
                resultado.add(t);
            }
        }
        return resultado;
    }
}
