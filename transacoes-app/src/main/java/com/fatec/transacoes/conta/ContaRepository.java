package com.fatec.transacoes.conta;

import java.util.List;
import java.util.Optional;

public interface ContaRepository {
    Conta save(Conta conta);
    Optional<Conta> findById(Long id);
    List<Conta> findAll();
}
