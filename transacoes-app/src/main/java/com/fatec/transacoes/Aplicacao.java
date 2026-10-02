package com.fatec.transacoes;

import java.math.BigDecimal;
import java.time.Clock;

import com.fatec.transacoes.conta.Conta;
import com.fatec.transacoes.conta.ContaRepositoryMemoria;
import com.fatec.transacoes.conta.ContaService;
import com.fatec.transacoes.conta.DadosCadastroConta;
import com.fatec.transacoes.transacao.Transacao;
import com.fatec.transacoes.transacao.TransacaoRepositoryMemoria;
import com.fatec.transacoes.transacao.TransacaoService;

public class Aplicacao {

    public static void main(String[] args) {
        ContaRepositoryMemoria contas = new ContaRepositoryMemoria();
        ContaService contaService = new ContaService(contas);
        TransacaoService transacaoService =
                new TransacaoService(contas, new TransacaoRepositoryMemoria(), Clock.systemDefaultZone());

        Conta ana = contaService.criar(new DadosCadastroConta("Ana", new BigDecimal("3000.00")));
        Conta bruno = contaService.criar(new DadosCadastroConta("Bruno", new BigDecimal("100.00")));

        transacaoService.depositar(ana.getId(), new BigDecimal("500.00"));
        transacaoService.transferir(ana.getId(), bruno.getId(), new BigDecimal("2000.00"));

        System.out.println("Saldo Ana:   " + contaService.buscar(ana.getId()).getSaldo());
        System.out.println("Saldo Bruno: " + contaService.buscar(bruno.getId()).getSaldo());
        System.out.println("Extrato da Ana:");
        for (Transacao t : transacaoService.extrato(ana.getId())) {
            System.out.println("  " + t.getDataHora() + " " + t.getTipo() + " " + t.getValor() + " taxa " + t.getTaxa());
        }
    }
}
