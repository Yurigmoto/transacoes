package com.fatec.transacoes.transacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fatec.transacoes.conta.Conta;
import com.fatec.transacoes.conta.ContaRepository;
import com.fatec.transacoes.excecao.ContaNaoEncontradaException;
import com.fatec.transacoes.excecao.LimiteExcedidoException;
import com.fatec.transacoes.excecao.SaldoInsuficienteException;

public class TransacaoService {

    public static final BigDecimal LIMITE_TRANSFERENCIA = new BigDecimal("5000.00");
    public static final BigDecimal FAIXA_ISENTA = new BigDecimal("1000.00");
    public static final BigDecimal PERCENTUAL_TAXA = new BigDecimal("0.005");

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final Clock clock;

    public TransacaoService(ContaRepository contaRepository,
            TransacaoRepository transacaoRepository, Clock clock) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.clock = clock;
    }

    public Transacao depositar(Long contaId, BigDecimal valor) {
        Conta.validarValor(valor);
        Conta conta = buscarConta(contaId);
        exigirAtiva(conta, "Conta inativa");

        conta.depositar(valor);
        contaRepository.save(conta);
        return registrar(TipoTransacao.DEPOSITO, valor, semTaxa(), null, contaId);
    }

    public Transacao sacar(Long contaId, BigDecimal valor) {
        Conta.validarValor(valor);
        Conta conta = buscarConta(contaId);
        exigirAtiva(conta, "Conta inativa");

        conta.sacar(valor);
        contaRepository.save(conta);
        return registrar(TipoTransacao.SAQUE, valor, semTaxa(), contaId, null);
    }

    public Transacao transferir(Long origemId, Long destinoId, BigDecimal valor) {
        Conta.validarValor(valor);
        if (origemId == null || destinoId == null) {
            throw new IllegalArgumentException("Conta inválida");
        }
        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("Conta de origem e destino devem ser diferentes");
        }
        if (valor.compareTo(LIMITE_TRANSFERENCIA) > 0) {
            throw new LimiteExcedidoException("Valor acima do limite por transferência");
        }

        Conta origem = buscarConta(origemId);
        Conta destino = buscarConta(destinoId);
        exigirAtiva(origem, "Conta de origem inativa");
        exigirAtiva(destino, "Conta de destino inativa");

        BigDecimal taxa = calcularTaxa(valor);
        BigDecimal total = valor.add(taxa);

        // valida tudo ANTES de alterar qualquer saldo (operação atômica)
        if (origem.getSaldo().compareTo(total) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente");
        }
        origem.sacar(total);
        destino.depositar(valor);

        contaRepository.save(origem);
        contaRepository.save(destino);
        return registrar(TipoTransacao.TRANSFERENCIA, valor, taxa, origemId, destinoId);
    }

    public BigDecimal calcularTaxa(BigDecimal valor) {
        if (valor.compareTo(FAIXA_ISENTA) > 0) {
            return valor.multiply(PERCENTUAL_TAXA).setScale(2, RoundingMode.HALF_UP);
        }
        return semTaxa();
    }

    public List<Transacao> extrato(Long contaId) {
        buscarConta(contaId);
        List<Transacao> lista = new ArrayList<>(transacaoRepository.findByContaId(contaId));
        lista.sort(Comparator.comparing(Transacao::getDataHora).reversed());
        return lista;
    }

    private Conta buscarConta(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    private void exigirAtiva(Conta conta, String mensagem) {
        if (!conta.isAtiva()) {
            throw new IllegalStateException(mensagem);
        }
    }

    private BigDecimal semTaxa() {
        return BigDecimal.ZERO.setScale(2);
    }

    private Transacao registrar(TipoTransacao tipo, BigDecimal valor, BigDecimal taxa,
            Long origemId, Long destinoId) {
        Transacao t = new Transacao(tipo, valor, taxa, origemId, destinoId, LocalDateTime.now(clock));
        return transacaoRepository.save(t);
    }
}
