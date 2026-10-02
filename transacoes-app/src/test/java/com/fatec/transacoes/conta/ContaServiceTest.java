package com.fatec.transacoes.conta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fatec.transacoes.excecao.ContaNaoEncontradaException;

class ContaServiceTest {

    private ContaService service;

    @BeforeEach
    void setUp() {
        service = new ContaService(new ContaRepositoryMemoria());
    }

    @Test
    @DisplayName("criar: atribui id e guarda a conta")
    void criaContaComId() {
        Conta conta = service.criar(new DadosCadastroConta("Ana", new BigDecimal("100.00")));

        assertNotNull(conta.getId());
        assertEquals(new BigDecimal("100.00"), service.buscar(conta.getId()).getSaldo());
    }

    @Test
    @DisplayName("criar: saldo inicial nulo vira 0.00")
    void saldoInicialNuloViraZero() {
        Conta conta = service.criar(new DadosCadastroConta("Ana", null));
        assertEquals(new BigDecimal("0.00"), conta.getSaldo());
    }

    @Test
    @DisplayName("criar: ids diferentes para contas diferentes")
    void idsDiferentes() {
        Conta a = service.criar(new DadosCadastroConta("Ana", BigDecimal.ONE));
        Conta b = service.criar(new DadosCadastroConta("Bruno", BigDecimal.ONE));
        assertFalse(a.getId().equals(b.getId()));
    }

    @Test
    void criarComTitularVazioLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(new DadosCadastroConta("", BigDecimal.TEN)));
    }

    @Test
    void criarComSaldoNegativoLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(new DadosCadastroConta("Ana", new BigDecimal("-1"))));
    }

    @Test
    void buscarInexistenteLancaExcecao() {
        ContaNaoEncontradaException ex =
                assertThrows(ContaNaoEncontradaException.class, () -> service.buscar(99L));
        assertEquals("Conta não encontrada: 99", ex.getMessage());
    }

    @Test
    void listarRetornaTodasAsContas() {
        assertEquals(0, service.listar().size());
        service.criar(new DadosCadastroConta("Ana", BigDecimal.ONE));
        service.criar(new DadosCadastroConta("Bruno", BigDecimal.ONE));
        assertEquals(2, service.listar().size());
    }

    @Test
    void desativarEReativar() {
        Conta conta = service.criar(new DadosCadastroConta("Ana", BigDecimal.ONE));

        service.desativar(conta.getId());
        assertFalse(service.buscar(conta.getId()).isAtiva());

        service.ativar(conta.getId());
        assertTrue(service.buscar(conta.getId()).isAtiva());
    }

    @Test
    void desativarInexistenteLancaExcecao() {
        assertThrows(ContaNaoEncontradaException.class, () -> service.desativar(42L));
    }
}
