/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.api.erp.chat.notificacoes;

import br.org.coletivoJava.fw.api.erp.chat.model.ComandoDeAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ErroComandoAtendimentoInvalido;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoComandoAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfNotificacaoUsuarioChat;
import br.org.coletivoJava.fw.api.erp.chat.notificacoes.ItfRetornoDeChamadaDeNotificacao;

/**
 *
 * @author salvio
 */
public class ListenerNotificacaoTestes implements ItfListenerEventoComandoAtendimento {

    @Override
    public void processarComando(ComandoDeAtendimento pComando) throws ErroComandoAtendimentoInvalido {
        System.out.println("processando comando " + pComando.getEvento().getContent().toString());
    }
}
