/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package br.org.coletivoJava.fw.api.erp.chat.notificacoes;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;

/**
 *
 * @author salvio
 */
public interface ItfRetornoDeChamadaDeNotificacao {

    public void onEventReceived(ItfNotificacaoUsuarioChat pNotificacao);
}
