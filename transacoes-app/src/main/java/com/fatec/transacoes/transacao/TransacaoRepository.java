package com.fatec.transacoes.transacao;

import java.util.List;

public interface TransacaoRepository {
    Transacao save(Transacao transacao);
    List<Transacao> findByContaId(Long contaId);
}
