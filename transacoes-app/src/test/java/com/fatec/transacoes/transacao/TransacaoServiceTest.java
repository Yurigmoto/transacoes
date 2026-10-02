package com.fatec.transacoes.transacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fatec.transacoes.conta.Conta;
import com.fatec.transacoes.conta.ContaRepositoryMemoria;
import com.fatec.transacoes.excecao.ContaNaoEncontradaException;
import com.fatec.transacoes.excecao.LimiteExcedidoException;
import com.fatec.transacoes.excecao.SaldoInsuficienteException;

class TransacaoServiceTest {

    private ContaRepositoryMemoria contas;
    private TransacaoRepositoryMemoria transacoes;
    private RelogioMutavel relogio;
    private TransacaoService service;

    @BeforeEach
    void setUp() {
        contas = new ContaRepositoryMemoria();
        transacoes = new TransacaoRepositoryMemoria();
        relogio = new RelogioMutavel();
        service = new TransacaoService(contas, transacoes, relogio);
    }

    private Conta criarConta(String titular, String saldo) {
        return contas.save(new Conta(titular, new BigDecimal(saldo)));
    }

    private BigDecimal saldoDe(Conta conta) {
        return contas.findById(conta.getId()).get().getSaldo();
    }

    private int totalDeTransacoes(Conta conta) {
        return transacoes.findByContaId(conta.getId()).size();
    }

    // ================================================================ depositar

    @Test
    @DisplayName("depositar: aumenta o saldo e registra a transação")
    void depositoOk() {
        Conta ana = criarConta("Ana", "100.00");

        Transacao t = service.depositar(ana.getId(), new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), saldoDe(ana));
        assertNotNull(t.getId());
        assertEquals(TipoTransacao.DEPOSITO, t.getTipo());
        assertEquals(new BigDecimal("50.00"), t.getValor());
        assertEquals(new BigDecimal("0.00"), t.getTaxa());
        assertNull(t.getContaOrigemId());
        assertEquals(ana.getId(), t.getContaDestinoId());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 0), t.getDataHora());
    }

    @ParameterizedTest(name = "depositar {0} é inválido")
    @ValueSource(strings = {"0", "-10", "5.555"})
    void depositoValorInvalido(String valor) {
        Conta ana = criarConta("Ana", "100.00");

        assertThrows(IllegalArgumentException.class,
                () -> service.depositar(ana.getId(), new BigDecimal(valor)));

        assertEquals(new BigDecimal("100.00"), saldoDe(ana));
        assertEquals(0, totalDeTransacoes(ana));
    }

    @Test
    void depositoContaInexistente() {
        assertThrows(ContaNaoEncontradaException.class,
                () -> service.depositar(99L, new BigDecimal("10.00")));
    }

    @Test
    void depositoContaInativa() {
        Conta ana = criarConta("Ana", "100.00");
        ana.desativar();

        assertThrows(IllegalStateException.class,
                () -> service.depositar(ana.getId(), new BigDecimal("10.00")));

        assertEquals(new BigDecimal("100.00"), saldoDe(ana));
        assertEquals(0, totalDeTransacoes(ana));
    }

    // ================================================================ sacar

    @Test
    @DisplayName("sacar: diminui o saldo e registra a transação")
    void saqueOk() {
        Conta ana = criarConta("Ana", "100.00");

        Transacao t = service.sacar(ana.getId(), new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), saldoDe(ana));
        assertEquals(TipoTransacao.SAQUE, t.getTipo());
        assertEquals(ana.getId(), t.getContaOrigemId());
        assertNull(t.getContaDestinoId());
    }

    @Test
    void saqueDeTodoOSaldo() {
        Conta ana = criarConta("Ana", "100.00");
        service.sacar(ana.getId(), new BigDecimal("100.00"));
        assertEquals(new BigDecimal("0.00"), saldoDe(ana));
    }

    @Test
    void saqueComSaldoInsuficiente() {
        Conta ana = criarConta("Ana", "100.00");

        assertThrows(SaldoInsuficienteException.class,
                () -> service.sacar(ana.getId(), new BigDecimal("100.01")));

        assertEquals(new BigDecimal("100.00"), saldoDe(ana));
        assertEquals(0, totalDeTransacoes(ana));
    }

    @Test
    void saqueContaInativa() {
        Conta ana = criarConta("Ana", "100.00");
        ana.desativar();
        assertThrows(IllegalStateException.class,
                () -> service.sacar(ana.getId(), new BigDecimal("10.00")));
    }

    @Test
    void saqueContaInexistente() {
        assertThrows(ContaNaoEncontradaException.class,
                () -> service.sacar(99L, new BigDecimal("10.00")));
    }

    // ================================================================ calcularTaxa

    @ParameterizedTest(name = "taxa de {0} = {1}")
    @CsvSource({
        "0.01, 0.00",
        "100.00, 0.00",
        "1000.00, 0.00",
        "1000.01, 5.00",
        "1234.56, 6.17",
        "1999.99, 10.00",
        "2000.00, 10.00",
        "5000.00, 25.00"
    })
    @DisplayName("calcularTaxa: 0,5% só acima de R$ 1.000,00, arredondada HALF_UP")
    void taxaPorFaixa(String valor, String taxaEsperada) {
        assertEquals(new BigDecimal(taxaEsperada), service.calcularTaxa(new BigDecimal(valor)));
    }

    // ================================================================ transferir

    @Test
    @DisplayName("transferir: valor até R$ 1.000,00 não paga taxa")
    void transferenciaSemTaxa() {
        Conta ana = criarConta("Ana", "500.00");
        Conta bruno = criarConta("Bruno", "100.00");

        Transacao t = service.transferir(ana.getId(), bruno.getId(), new BigDecimal("200.00"));

        assertEquals(new BigDecimal("300.00"), saldoDe(ana));
        assertEquals(new BigDecimal("300.00"), saldoDe(bruno));
        assertEquals(TipoTransacao.TRANSFERENCIA, t.getTipo());
        assertEquals(new BigDecimal("200.00"), t.getValor());
        assertEquals(new BigDecimal("0.00"), t.getTaxa());
        assertEquals(ana.getId(), t.getContaOrigemId());
        assertEquals(bruno.getId(), t.getContaDestinoId());
    }

    @Test
    @DisplayName("transferir: exatamente R$ 1.000,00 ainda é isento de taxa")
    void transferenciaNoLimiteDaFaixaIsenta() {
        Conta ana = criarConta("Ana", "1000.00");
        Conta bruno = criarConta("Bruno", "0.00");

        service.transferir(ana.getId(), bruno.getId(), new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("0.00"), saldoDe(ana));
        assertEquals(new BigDecimal("1000.00"), saldoDe(bruno));
    }

    @Test
    @DisplayName("transferir: a origem paga valor + taxa e o destino recebe só o valor")
    void transferenciaComTaxa() {
        Conta ana = criarConta("Ana", "3000.00");
        Conta bruno = criarConta("Bruno", "100.00");

        Transacao t = service.transferir(ana.getId(), bruno.getId(), new BigDecimal("2000.00"));

        assertEquals(new BigDecimal("990.00"), saldoDe(ana));
        assertEquals(new BigDecimal("2100.00"), saldoDe(bruno));
        assertEquals(new BigDecimal("10.00"), t.getTaxa());
    }

    @Test
    @DisplayName("transferir: saldo exatamente igual a valor + taxa é suficiente")
    void transferenciaComSaldoExato() {
        Conta ana = criarConta("Ana", "2010.00");
        Conta bruno = criarConta("Bruno", "0.00");

        service.transferir(ana.getId(), bruno.getId(), new BigDecimal("2000.00"));

        assertEquals(new BigDecimal("0.00"), saldoDe(ana));
        assertEquals(new BigDecimal("2000.00"), saldoDe(bruno));
    }

    @Test
    void transferenciaComSaldoInsuficienteNaoAlteraNada() {
        Conta ana = criarConta("Ana", "100.00");
        Conta bruno = criarConta("Bruno", "50.00");

        assertThrows(SaldoInsuficienteException.class,
                () -> service.transferir(ana.getId(), bruno.getId(), new BigDecimal("200.00")));

        assertEquals(new BigDecimal("100.00"), saldoDe(ana));
        assertEquals(new BigDecimal("50.00"), saldoDe(bruno));
        assertEquals(0, totalDeTransacoes(ana));
    }

    @Test
    void transferenciaParaMesmaConta() {
        Conta ana = criarConta("Ana", "100.00");
        assertThrows(IllegalArgumentException.class,
                () -> service.transferir(ana.getId(), ana.getId(), new BigDecimal("10.00")));
        assertEquals(new BigDecimal("100.00"), saldoDe(ana));
    }

    @Test
    void transferenciaComIdNulo() {
        Conta ana = criarConta("Ana", "100.00");
        assertThrows(IllegalArgumentException.class,
                () -> service.transferir(null, ana.getId(), new BigDecimal("10.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.transferir(ana.getId(), null, new BigDecimal("10.00")));
    }

    @ParameterizedTest(name = "transferir {0} é inválido")
    @ValueSource(strings = {"0", "-1", "10.001"})
    void transferenciaValorInvalido(String valor) {
        Conta ana = criarConta("Ana", "100.00");
        Conta bruno = criarConta("Bruno", "0.00");
        assertThrows(IllegalArgumentException.class,
                () -> service.transferir(ana.getId(), bruno.getId(), new BigDecimal(valor)));
    }

    @Test
    void transferenciaOrigemInexistente() {
        Conta bruno = criarConta("Bruno", "0.00");
        assertThrows(ContaNaoEncontradaException.class,
                () -> service.transferir(99L, bruno.getId(), new BigDecimal("10.00")));
    }

    @Test
    void transferenciaDestinoInexistente() {
        Conta ana = criarConta("Ana", "100.00");
        assertThrows(ContaNaoEncontradaException.class,
                () -> service.transferir(ana.getId(), 99L, new BigDecimal("10.00")));
        assertEquals(new BigDecimal("100.00"), saldoDe(ana));
    }

    @Test
    void transferenciaOrigemInativa() {
        Conta ana = criarConta("Ana", "100.00");
        Conta bruno = criarConta("Bruno", "0.00");
        ana.desativar();

        assertThrows(IllegalStateException.class,
                () -> service.transferir(ana.getId(), bruno.getId(), new BigDecimal("10.00")));

        assertEquals(new BigDecimal("100.00"), saldoDe(ana));
        assertEquals(new BigDecimal("0.00"), saldoDe(bruno));
    }

    @Test
    @DisplayName("transferir: acima de R$ 5.000,00 lança LimiteExcedidoException")
    void transferenciaAcimaDoLimite() {
        Conta ana = criarConta("Ana", "10000.00");
        Conta bruno = criarConta("Bruno", "0.00");

        assertThrows(LimiteExcedidoException.class,
                () -> service.transferir(ana.getId(), bruno.getId(), new BigDecimal("5000.01")));

        assertEquals(new BigDecimal("10000.00"), saldoDe(ana));
        assertEquals(new BigDecimal("0.00"), saldoDe(bruno));
    }

    @Test
    @DisplayName("transferir: se o saldo cobre o valor mas não a taxa, nada é debitado")
    void transferenciaEhAtomica() {
        Conta ana = criarConta("Ana", "2005.00");   // cobre 2000.00, mas não 2000.00 + 10.00 de taxa
        Conta bruno = criarConta("Bruno", "0.00");

        assertThrows(SaldoInsuficienteException.class,
                () -> service.transferir(ana.getId(), bruno.getId(), new BigDecimal("2000.00")));

        assertEquals(new BigDecimal("2005.00"), saldoDe(ana));
        assertEquals(new BigDecimal("0.00"), saldoDe(bruno));
        assertEquals(0, totalDeTransacoes(ana));
    }

    @Test
    @DisplayName("transferir: conta de destino inativa é rejeitada")
    void transferenciaDestinoInativo() {
        Conta ana = criarConta("Ana", "500.00");
        Conta bruno = criarConta("Bruno", "0.00");
        bruno.desativar();

        assertThrows(IllegalStateException.class,
                () -> service.transferir(ana.getId(), bruno.getId(), new BigDecimal("100.00")));

        assertEquals(new BigDecimal("500.00"), saldoDe(ana));
        assertEquals(new BigDecimal("0.00"), saldoDe(bruno));
    }

    @Test
    @DisplayName("transferir: exatamente R$ 5.000,00 é permitido (limite inclusivo)")
    void transferenciaNoValorExatoDoLimite() {
        Conta ana = criarConta("Ana", "6000.00");
        Conta bruno = criarConta("Bruno", "0.00");

        service.transferir(ana.getId(), bruno.getId(), new BigDecimal("5000.00"));

        // 5000.00 + taxa de 25.00 saem da origem
        assertEquals(new BigDecimal("975.00"), saldoDe(ana));
        assertEquals(new BigDecimal("5000.00"), saldoDe(bruno));
    }

    // ================================================================ extrato

    @Test
    @DisplayName("extrato: lista da mais recente para a mais antiga, incluindo recebidas e enviadas")
    void extratoOrdenado() {
        Conta ana = criarConta("Ana", "1000.00");
        Conta bruno = criarConta("Bruno", "1000.00");

        service.depositar(ana.getId(), new BigDecimal("100.00"));          // 12:00
        relogio.avancarMinutos(10);
        service.transferir(ana.getId(), bruno.getId(), new BigDecimal("50.00")); // 12:10
        relogio.avancarMinutos(10);
        service.transferir(bruno.getId(), ana.getId(), new BigDecimal("20.00")); // 12:20

        List<Transacao> extrato = service.extrato(ana.getId());

        assertEquals(3, extrato.size());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 20), extrato.get(0).getDataHora());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 10), extrato.get(1).getDataHora());
        assertEquals(LocalDateTime.of(2026, 10, 2, 12, 0), extrato.get(2).getDataHora());
        assertEquals(TipoTransacao.DEPOSITO, extrato.get(2).getTipo());
    }

    @Test
    void extratoDeContaSemMovimentacaoEhVazio() {
        Conta ana = criarConta("Ana", "10.00");
        assertEquals(0, service.extrato(ana.getId()).size());
    }

    @Test
    void extratoNaoMostraTransacoesDeOutrasContas() {
        Conta ana = criarConta("Ana", "100.00");
        Conta bruno = criarConta("Bruno", "100.00");
        service.depositar(bruno.getId(), new BigDecimal("10.00"));
        assertEquals(0, service.extrato(ana.getId()).size());
    }

    @Test
    void extratoDeContaInexistente() {
        assertThrows(ContaNaoEncontradaException.class, () -> service.extrato(99L));
    }
}
