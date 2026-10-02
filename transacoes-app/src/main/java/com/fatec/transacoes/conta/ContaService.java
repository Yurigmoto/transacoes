package com.fatec.transacoes.conta;

import java.math.BigDecimal;
import java.util.List;

import com.fatec.transacoes.excecao.ContaNaoEncontradaException;

public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    public Conta criar(DadosCadastroConta dados) {
        BigDecimal saldoInicial = dados.saldoInicial() == null ? BigDecimal.ZERO : dados.saldoInicial();
        Conta conta = new Conta(dados.titular(), saldoInicial);
        return contaRepository.save(conta);
    }

    public Conta buscar(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    public List<Conta> listar() {
        return contaRepository.findAll();
    }

    public void desativar(Long id) {
        Conta conta = buscar(id);
        conta.desativar();
        contaRepository.save(conta);
    }

    public void ativar(Long id) {
        Conta conta = buscar(id);
        conta.ativar();
        contaRepository.save(conta);
    }
}
