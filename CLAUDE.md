# SbErpChatMatrixOrg

Implementação Matrix.org (Synapse) da API de chat `SbErpChat` do ERP coletivoJava.
Java 8, Maven, jar. Comunicação e comentários em português.

## Build
- `mvn -o -q compile` (offline funciona; sem saída = sucesso).
- Testes em `src/test` dependem de servidor Matrix real configurado; não rodar sem pedir.
- Commits "Atualizacao automática" vêm do `sincronizaGit.sh`; não commitar sem o usuário pedir.

## Projetos relacionados (fora deste repo)
- API: `../SbErpChat` (`ComoChatSalaBean`, `ComoUsuarioChat`, `ItfListenerEventoMatrix`, `ItfEventoMatix`, `FabTipoPacoteDeAcaoMatrix`).
- Integração REST: `/home/superBits/projetos/coletivoJava/source/integracao/intMatrixChat`
  - `FabConfigApiMatrixChat` (URL_MATRIX_SERVER, USUARIO_ADMIN, DOMINIO_FEDERADO, SEGREDO).
  - `FabApiRestIntMatrixChatUsuarios` / `...Salas` (endpoints; `USUARIOS_DA_SALA.getGestaoToken()` = token do admin).
  - `GestaoTokenRestIntmatrixchat`: login por senha, cada JVM gera seu próprio device/token.

## Código principal (`src/main/java/br/org/coletivoJava/fw/erp/implementacao/chat`)
- `ChatMatrixOrgimpl` (~1900 linhas): serviço. Obter com
  `(ChatMatrixOrgimpl) ERPChat.MATRIX_ORG.getImplementacaoDoContexto()`.
  Caches estáticos de usuários (email/código/telefone) e salas. `getCodigoUsuarioAdmin()` é estático.
  - `getSalaByCodigo` é CARO (carrega DTO de cada membro via HTTP) e pode EXCLUIR a sala se houver
    divergência de permissão. Não usar em fluxos de monitoramento.
  - Convenção de ids: contato (Whatsapp) contém `.ct:`, atendente contém `.at:`.
- `sessaoMatrix/`: integração Matrix ↔ Whatsapp (roda em outra aplicação/máquina).
  - `SessaoMatrix` (thread de login) → `ClientMatrix` → `SincronizacaoSalasMatrix`/`SincronizacaoAbstrata`
    (loop /sync do admin; persiste since em `next_batch_matrix`; comandos de atendimento quando o admin é mencionado).
  - Despacho por `findFirst`: apenas UM listener por sala. Salas sem monitoramento automático são ignoradas.
- `atendente/`: escuta de atendentes por e-mail para apps como o CRM (independente de `sessaoMatrix`).
  - `EscutaAtendenteMatrixAbst` (fica no bean @SessionScoped; `iniciar()`/`encerrar()`; abstrato `atualizacaoSala`).
  - `CentralAtendentesMatrix`: estado estático compartilhado (só Strings), WeakReference das escutas,
    pendências por atendente (1 notificação por sala), thread única de despacho.
  - `SincronizacaoAtendentesMatrix`: /sync próprio do admin, filtro enxuto, since só em memória;
    a carga inicial só monta membros/nome/alias da sala, sem notificar.
  - Regras: mensagem de contato → NOVA_INTERACAO (ignora > 6h); mensagem/reação de atendente → RESPONDIDA
    para todos da sala; recibo `m.read` → LIDA só para quem leu; admin e edições ignorados.
  - `AtualizacaoSalaAtendente.getLinkElement(urlBase)` → `{url}/#/room/{alias ou código}`.
- `json_bind_matrix_org/`: DTOs + desserializadores Jackson (`JsonBind*`).

## Convenções
- Log: `CarameloCode.getServicoLogEventos().registrarLogDeEvento(FabMensagens, "[TAG] msg")` dentro de try,
  com fallback em `System.out`.
- Erros: `SBCore.RelatarErro(FabErro.SOLICITAR_REPARO, msg, ex)`.
- Config: `SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(...)`.
- JSON: `org.json` para eventos do /sync; `jakarta.json` (`UtilCRCJson`) nas respostas REST.
