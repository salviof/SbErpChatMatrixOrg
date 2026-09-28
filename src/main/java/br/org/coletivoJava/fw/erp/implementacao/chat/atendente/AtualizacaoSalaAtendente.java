package br.org.coletivoJava.fw.erp.implementacao.chat.atendente;

/**
 * Atualização de uma sala para um atendente.
 *
 * Nas remoções (LIDA, RESPONDIDA, SAIU_DA_SALA) os dados são os da última
 * mensagem que gerou a notificação, para que ela possa ser localizada e
 * retirada.
 *
 * @author salvio
 */
public class AtualizacaoSalaAtendente {

    private final FabTipoAtualizacaoSalaAtendente tipo;
    private final String codigoAtendente;
    private final String codigoSala;
    private final String aliasSala;
    private final String nomeSala;
    private final String codigoRemetente;
    private final String nomeRemetente;
    private final String textoUltimaMensagem;
    private final String eventId;
    private final long dataHora;

    /**
     * O e-mail exige uma consulta ao Matrix, por isso só é buscado quando
     * solicitado, fora da thread de sincronização.
     */
    private volatile String emailRemetente;
    private volatile boolean emailRemetenteResolvido;

    AtualizacaoSalaAtendente(FabTipoAtualizacaoSalaAtendente pTipo, String pCodigoAtendente,
            String pCodigoSala, String pAliasSala, String pNomeSala,
            String pCodigoRemetente, String pNomeRemetente,
            String pTextoUltimaMensagem, String pEventId, long pDataHora) {
        tipo = pTipo;
        codigoAtendente = pCodigoAtendente;
        codigoSala = pCodigoSala;
        aliasSala = pAliasSala;
        nomeSala = pNomeSala;
        codigoRemetente = pCodigoRemetente;
        nomeRemetente = pNomeRemetente;
        textoUltimaMensagem = pTextoUltimaMensagem;
        eventId = pEventId;
        dataHora = pDataHora;
    }

    AtualizacaoSalaAtendente comTipo(FabTipoAtualizacaoSalaAtendente pTipo) {
        AtualizacaoSalaAtendente copia = new AtualizacaoSalaAtendente(pTipo, codigoAtendente, codigoSala,
                aliasSala, nomeSala, codigoRemetente, nomeRemetente, textoUltimaMensagem, eventId, dataHora);
        copia.emailRemetente = emailRemetente;
        copia.emailRemetenteResolvido = emailRemetenteResolvido;
        return copia;
    }

    public FabTipoAtualizacaoSalaAtendente getTipo() {
        return tipo;
    }

    public boolean isRemoverNotificacao() {
        return tipo.isRemoverNotificacao();
    }

    public String getCodigoAtendente() {
        return codigoAtendente;
    }

    /**
     * @return código interno da sala, ex: !abcdef:dominio.com.br
     */
    public String getCodigoSala() {
        return codigoSala;
    }

    /**
     * @return alias canônico da sala, ex: #chamadocliente169_ct:dominio.com.br,
     * ou null quando a sala não possui alias
     */
    public String getAliasSala() {
        return aliasSala;
    }

    /**
     * @return o alias da sala quando existir, senão o código da sala
     */
    public String getIdentificadorSala() {
        return aliasSala != null ? aliasSala : codigoSala;
    }

    public String getNomeSala() {
        return nomeSala;
    }

    public String getCodigoRemetente() {
        return codigoRemetente;
    }

    public String getNomeRemetente() {
        return nomeRemetente;
    }

    /**
     * Opcional: pode retornar null quando o remetente não tem e-mail no
     * Matrix. A primeira chamada pode consultar o Matrix.
     */
    public String getEmailRemetente() {
        if (!emailRemetenteResolvido) {
            emailRemetente = CentralAtendentesMatrix.getEmailUsuario(codigoRemetente);
            emailRemetenteResolvido = true;
        }
        return emailRemetente;
    }

    public String getTextoUltimaMensagem() {
        return textoUltimaMensagem;
    }

    public String getEventId() {
        return eventId;
    }

    /**
     * @return origin_server_ts da mensagem em milissegundos
     */
    public long getDataHora() {
        return dataHora;
    }

    /**
     * @param pUrlBaseElement ex: https://element.dominio.com.br
     * @return ex: https://element.dominio.com.br/#/room/#chamadocliente169_ct:dominio.com.br
     */
    public String getLinkElement(String pUrlBaseElement) {
        String base = pUrlBaseElement == null ? "" : pUrlBaseElement.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/#/room/" + getIdentificadorSala();
    }

    @Override
    public String toString() {
        return tipo + " atendente=" + codigoAtendente + " sala=" + getIdentificadorSala()
                + " remetente=" + codigoRemetente + " evento=" + eventId;
    }

}
