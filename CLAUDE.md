# Awpy — Contexto do Projeto

## Sobre o projeto

Awpy é um app de fidelidade/recompensas. Usuários acumulam **pontos**, trocam por
**benefícios** (gerando um **cupom** com QR Code), e **parceiros** (estabelecimentos)
validam o uso do cupom escaneando o QR Code. Há também um perfil **administrativo**
que gerencia usuários internos, parceiros e benefícios/cupons.

Stack: Java + Spring Boot (Maven). Pacote raiz: `com.awpy.awpy`.

## Como quero receber as respostas

- Explique o "porquê" antes do código (sou e estou aprendendo arquitetura Spring/Java
  a fundo, não só copiando código).
- Prefira explicações didáticas e incrementais a grandes blocos de código de uma vez.
- Code review e diffs pequenos e revisáveis por etapa/fase.

## Perfis de usuário

1. **Usuário comum** — cria conta, acumula pontos, troca por cupons, exibe QR Code
   do cupom para o parceiro.
2. **Parceiro** — login próprio, separado do usuário comum; escaneia QR Code do
   cupom, confirma ou cancela o uso.
3. **Administrador/Funcionário** — perfil interno; cadastra outros admins/funcionários,
   cadastra parceiros, gerencia benefícios/cupons (incluindo desconto e cashback).

Regra geral: os três perfis têm permissões e telas completamente separadas. Nenhuma
ação de um perfil deve ser visível/acessível a outro perfil sem autorização explícita.

## Fluxo 1 — Usuário novo (onboarding)

1. Ícone do Home → abre o app.
2. Boas-vindas → identidade visual + mensagem, prepara redirecionamento.
3. Login/Cadastro → ponto de decisão: usuário novo segue para cadastro; usuário
   existente segue para login (com biometria/autenticação fácil quando cadastrado).
4. Cadastro de Usuário (multi-etapa):
   - Clique em "Criar conta".
   - Preenchimento: nome completo, CPF/CNPJ, e-mail, senha, telefone, endereço, CEP
     — **todos obrigatórios**.
   - Validação: nenhum campo obrigatório vazio; e-mail e CPF/CNPJ em formato válido;
     **sem duplicidade** de dados críticos (e-mail/CPF-CNPJ já cadastrados).
   - Conclusão: cria a conta, usuário ganha identificação própria no sistema e acesso
     à área logada, é direcionado à Home.
5. Home do Usuário — exibe: foto, saldo em pontos, QR Code do **usuário** (diferente
   do QR Code do cupom), botão "resgatar cupom", ranking mensal top 5. É a tela
   central de navegação do usuário comum.

## Fluxo 2 — Resgate de cupom

Usuário logado, na Home, troca pontos por um benefício.

1. **Lista de Benefícios** — lista benefícios ativos com custo em pontos.
   - Regras: só benefícios ativos aparecem; só pode resgatar com pontos suficientes;
     **não pode ter mais de um cupom ativo por vez**; benefício indisponível é
     bloqueado.
2. **Meus Cupons** — mostra o cupom ativo do usuário, validade e status.
   - Status possíveis: `Ativo`, `Utilizado`, `Expirado`.
   - Só pode gerar novo cupom quando o atual estiver `Utilizado` ou `Expirado`.
3. **QR Code do Cupom** — QR único por cupom, diferente do QR do usuário, validade
   de **30 dias**. Exibir o QR **não** baixa pontos.
4. **Confirmação de Resgate** — tela final, confirma sucesso; também não baixa
   pontos. Cupom permanece ativo até confirmação do parceiro ou expiração; usuário
   fica bloqueado para novo resgate enquanto esse cupom estiver ativo.

Pontos só são baixados quando o **parceiro confirma o uso** (Fluxo 3).

## Fluxo 3 — Parceiro

Login próprio → Scanner QR Code → Card de Validação → Confirmar/Cancelar.

1. **Login do Parceiro** — só parceiros cadastrados e ativos; perfil separado do
   usuário comum; vê só suas funções operacionais.
2. **Scanner de QR Code** — lê o QR do **cupom** (nunca o do usuário); aceita
   digitação manual como alternativa. Validações antes de prosseguir:
   - QR Code inválido
   - cupom inexistente
   - cupom expirado
   - cupom já utilizado
   - cupom não pertence ao parceiro logado
3. **Card de Validação** — mostra dados do usuário (nome, foto, ID) e do cupom
   (benefício, parceiro, pontos, validade).
   - **Confirmar uso**: cupom → `Utilizado`, pontos baixados, operação registrada,
     usuário liberado para novo cupom.
   - **Cancelar**: nenhuma baixa de pontos; cupom permanece `Ativo` se ainda válido.
   - Nunca permitir confirmar cupom expirado, já utilizado, ou de outro parceiro
     (impedir dupla confirmação).

## Fluxo 4 — Administrativo

Login Administrativo → Home do Admin → Cadastro de Admin/Funcionário → Cadastro de
Parceiro → Gestão de Benefícios/Cupons.

- Permissões do admin são distintas das de usuário comum e parceiro; só perfis
  autorizados acessam funções internas.
- Cadastra administradores, funcionários e parceiros.
- Cadastra/edita benefícios e cupons, incluindo desconto, cashback e foto do parceiro.
- Benefícios/cupons devem estar sempre vinculados a um parceiro.
- Home do Admin é exclusiva para perfis internos; conteúdo exibido varia conforme
  permissão (ex.: funcionário pode ter menos acesso que admin).

## Entidades principais (modelagem sugerida)

- `Usuario` (comum): nome, CPF/CNPJ, e-mail, senha, telefone, endereço, CEP, saldoPontos
- `Parceiro`: dados de login próprios, estabelecimento, desconto, cashback, foto
- `AdminFuncionario`: perfil interno, nível de permissão (admin vs funcionário)
- `Beneficio`: nome, custoEmPontos, ativo/inativo, parceiro vinculado, desconto, cashback
- `Cupom`: usuário, benefício, parceiro, status (`ATIVO`/`UTILIZADO`/`EXPIRADO`),
  dataGeracao, dataExpiracao (+30 dias), qrCodeUnico
- `Ranking`: calculado mensalmente a partir do saldo de pontos

Regras de integridade centrais a proteger em código:
- 1 cupom ativo por usuário por vez.
- Baixa de pontos só ocorre na confirmação do parceiro, nunca antes.
- QR Code do usuário ≠ QR Code do cupom — não confundir endpoints/entidades.
- Cupom só pode ser confirmado pelo parceiro ao qual pertence.

## Convenções de código

- Seguir convenções padrão Spring Boot: `controller`, `service`, `repository`,
  `model`/`entity`, `dto` por perfil quando fizer sentido (usuário, parceiro, admin).
- Validações de regra de negócio (duplicidade, pontos suficientes, status de cupom)
  pertencem à camada de service, não ao controller.
- Sem código morto, sem abstração prematura — implementar fase a fase conforme o
  roadmap abaixo.

## Roadmap (fases)

1. Estrutura de pacotes + entidades principais (Usuario, Parceiro, Beneficio, Cupom,
   AdminFuncionario).
2. Cadastro/login de usuário comum (Fluxo 1).
3. Resgate de cupom (Fluxo 2): listagem de benefícios, geração de cupom, QR Code.
4. Fluxo do parceiro (Fluxo 3): login, scanner, validação, confirmação/cancelamento.
5. Fluxo administrativo (Fluxo 4): cadastros internos e gestão de benefícios/cupons.
6. Ranking mensal e regras de pontuação.

Próximo passo atual: **Fase 1** — estrutura de pacotes e entidades.
