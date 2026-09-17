# Desenvolvimento local com SQL Server

## Comportamento

O profile `local` é exclusivo do desenvolvimento. No primeiro startup em uma máquina, a aplicação conecta ao banco `master` e executa `CREATE DATABASE StudyConnect` somente se o banco não existir. Depois, o Hibernate usa as entidades JPA para criar/atualizar as tabelas.

Os dados não são apagados em reinicializações. O profile `production` e suas configurações não são alterados.

## Requisitos

- SQL Server local em execução.
- TCP/IP habilitado e porta `1433` (ou ajuste em `app.local.database.port`).
- Login `sa` habilitado com a senha local configurada.
- Permissão para criar banco de dados.

## Execução

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
./mvnw.cmd spring-boot:run
```

O arquivo `application-local.properties` contém a configuração da máquina e não deve ser versionado. Para outra máquina, copie `application-local.example.properties` e preencha os valores.

## Segurança

A senha do SQL Server e o segredo JWT local não devem ser publicados. Em caso de erro de conexão, confirme o serviço, a porta, o login `sa` e a permissão `CREATE DATABASE`.
