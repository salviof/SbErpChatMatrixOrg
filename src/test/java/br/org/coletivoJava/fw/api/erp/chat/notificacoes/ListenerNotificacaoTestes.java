/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.api.erp.chat.notificacoes;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.notificacoes.ItfRetornoDeChamadaDeNotificacao;

/**
 *
 * @author salvio
 */
public class ListenerNotificacaoTestes implements ItfRetornoDeChamadaDeNotificacao {

    @Override
    public void onEventReceived(ItfNotificacaoUsuarioChat pNotificacao) {
        System.out.println(pNotificacao.getCodigoNotificacao());
        System.out.println(pNotificacao.getCodigoSalaOrigem());
        System.out.println(pNotificacao.getRemetente());
        System.out.println(pNotificacao.getTipoEvento());
        System.out.println(pNotificacao.getConteudo());
    }
}
