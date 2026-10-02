package com.fatec.transacoes.conta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ContaRepositoryMemoria implements ContaRepository {

    private final Map<Long, Conta> contas = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Conta save(Conta conta) {
        if (conta.getId() == null) {
            conta.setId(proximoId++);
        }
        contas.put(conta.getId(), conta);
        return conta;
    }

    @Override
    public Optional<Conta> findById(Long id) {
        return Optional.ofNullable(contas.get(id));
    }

    @Override
    public List<Conta> findAll() {
        return new ArrayList<>(contas.values());
    }
}
