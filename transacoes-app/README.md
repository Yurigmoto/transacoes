# Sistema de Transações (projeto didático)

Projeto Maven em Java 17 criado pela equipe para a atividade de **Teste de Software com JUnit 5**.
Segue a mesma organização em camadas dos projetos Spring da disciplina
(entidade, repositório, service e DTO), mas sem framework, para que as regras
de negócio possam ser testadas de forma isolada.

## Regras de negócio
- Valores monetários usam `BigDecimal`, com no máximo 2 casas decimais e sempre maiores que zero.
- Depósito e saque exigem conta ativa; saque exige saldo suficiente.
- Transferência: contas de origem e destino diferentes, existentes e ativas;
  limite de R$ 5.000,00 por transferência (o valor exato do limite é permitido);
  taxa de 0,5% (arredondada HALF_UP, 2 casas) apenas para valores acima de R$ 1.000,00;
  a origem paga valor + taxa e a operação é atômica (se falhar, nenhum saldo muda).
- Extrato: transações da conta, da mais recente para a mais antiga.

## Comandos
```
mvn clean test            # compila e executa os testes
mvn clean package         # gera target/transacoes-app-1.0-SNAPSHOT.jar
java -jar target/transacoes-app-1.0-SNAPSHOT.jar
```
Cobertura (JaCoCo): `target/site/jacoco/index.html`
