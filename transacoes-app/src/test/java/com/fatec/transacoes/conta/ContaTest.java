package com.fatec.transacoes.conta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fatec.transacoes.excecao.SaldoInsuficienteException;

class ContaTest {

    private Conta novaConta(String saldo) {
        return new Conta("Ana", new BigDecimal(saldo));
    }

    // ---------------------------------------------------------------- construtor

    @Test
    @DisplayName("Conta nova começa ativa, com saldo normalizado em 2 casas")
    void criaContaValida() {
        Conta conta = new Conta("  Ana  ", new BigDecimal("100"));

        assertEquals("Ana", conta.getTitular());
        assertEquals(new BigDecimal("100.00"), conta.getSaldo());
        assertTrue(conta.isAtiva());
    }

    @Test
    @DisplayName("Saldo inicial com zeros à direita é aceito (100.100 -> 100.10)")
    void aceitaZerosADireita() {
        assertEquals(new BigDecimal("100.10"), novaConta("100.100").getSaldo());
    }

    @ParameterizedTest(name = "titular [{0}] é inválido")
    @ValueSource(strings = {"", "   "})
    void titularVazioOuEmBranco(String titular) {
        assertThrows(IllegalArgumentException.class, () -> new Conta(titular, BigDecimal.TEN));
    }

    @Test
    void titularNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Conta(null, BigDecimal.TEN));
    }

    @ParameterizedTest(name = "saldo inicial {0} é inválido")
    @ValueSource(strings = {"-0.01", "-100", "10.001"})
    void saldoInicialInvalido(String saldo) {
        assertThrows(IllegalArgumentException.class, () -> novaConta(saldo));
    }

    @Test
    void saldoInicialNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Conta("Ana", null));
    }

    // ---------------------------------------------------------------- depositar

    @ParameterizedTest(name = "saldo {0} + depósito {1} = {2}")
    @CsvSource({
        "100.00, 50.00, 150.00",
        "0.00, 0.01, 0.01",
        "10.50, 0.50, 11.00"
    })
    void depositoAumentaSaldo(String saldo, String valor, String esperado) {
        Conta conta = novaConta(saldo);
        conta.depositar(new BigDecimal(valor));
        assertEquals(new BigDecimal(esperado), conta.getSaldo());
    }

    @ParameterizedTest(name = "depósito de {0} é inválido")
    @ValueSource(strings = {"0", "0.00", "-1", "-0.01", "10.005"})
    void depositoInvalido(String valor) {
        Conta conta = novaConta("100.00");
        assertThrows(IllegalArgumentException.class, () -> conta.depositar(new BigDecimal(valor)));
        assertEquals(new BigDecimal("100.00"), conta.getSaldo());
    }

    @Test
    void depositoNulo() {
        assertThrows(IllegalArgumentException.class, () -> novaConta("100.00").depositar(null));
    }

    // ---------------------------------------------------------------- sacar

    @Test
    void saqueDiminuiSaldo() {
        Conta conta = novaConta("100.00");
        conta.sacar(new BigDecimal("30.50"));
        assertEquals(new BigDecimal("69.50"), conta.getSaldo());
    }

    @Test
    @DisplayName("Sacar exatamente o saldo é permitido e zera a conta")
    void saqueDoSaldoTotal() {
        Conta conta = novaConta("100.00");
        conta.sacar(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("0.00"), conta.getSaldo());
    }

    @Test
    @DisplayName("Sacar 1 centavo a mais que o saldo lança exceção e não altera o saldo")
    void saqueMaiorQueSaldo() {
        Conta conta = novaConta("100.00");
        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(new BigDecimal("100.01")));
        assertEquals(new BigDecimal("100.00"), conta.getSaldo());
    }

    @ParameterizedTest(name = "saque de {0} é inválido")
    @ValueSource(strings = {"0", "-5", "1.999"})
    void saqueInvalido(String valor) {
        Conta conta = novaConta("100.00");
        assertThrows(IllegalArgumentException.class, () -> conta.sacar(new BigDecimal(valor)));
        assertEquals(new BigDecimal("100.00"), conta.getSaldo());
    }

    // ---------------------------------------------------------------- ativar / desativar

    @Test
    void desativarEAtivar() {
        Conta conta = novaConta("10.00");
        conta.desativar();
        assertEquals(false, conta.isAtiva());
        conta.ativar();
        assertTrue(conta.isAtiva());
    }
}
