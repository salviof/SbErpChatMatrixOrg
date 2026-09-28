package br.org.coletivoJava.fw.erp.implementacao.chat.atendente;

/**
 * Tipos de atualização entregues ao {@link EscutaAtendenteMatrixAbst}.
 *
 * Apenas NOVA_INTERACAO cria uma notificação, todos os outros tipos indicam
 * que a notificação da sala deve ser retirada.
 *
 * @author salvio
 */
public enum FabTipoAtualizacaoSalaAtendente {

    /**
     * Mensagem de um contato em uma sala onde o atendente não tinha
     * notificação pendente.
     */
    NOVA_INTERACAO(false),
    /**
     * O próprio atendente enviou recibo de leitura da sala.
     */
    LIDA(true),
    /**
     * Um atendente (ele ou outro) respondeu ou reagiu na sala.
     */
    RESPONDIDA(true),
    /**
     * O atendente saiu da sala, ou o admin deixou de monitorá-la.
     */
    SAIU_DA_SALA(true);

    private final boolean removerNotificacao;

    private FabTipoAtualizacaoSalaAtendente(boolean pRemoverNotificacao) {
        removerNotificacao = pRemoverNotificacao;
    }

    public boolean isRemoverNotificacao() {
        return removerNotificacao;
    }

}
