/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.model.ComoChatSalaBean;
import br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix.listeners.EscutaSalaMatrixAbst;
import br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;

/**
 *
 * @author salvio
 */
public class SalaListenerTeste extends EscutaSalaMatrixAbst {

    public SalaListenerTeste(ComoChatSalaBean pSala) {
        super(pSala);
    }

    @Override
    public void eventoDigitando(ItfEventoMatix pEvento) {
        System.out.println(pEvento.getType());
        System.out.println(pEvento.getRoom_id());
        System.out.println(pEvento.getSender());
        System.out.println(pEvento.getEvent_id());
        System.out.println(pEvento.getContent());
        System.out.println(pEvento.getRaw());
    }

    @Override
    public void eventoMensagem(ItfEventoMatix pEvento) {
        System.out.println(pEvento.getType());
        System.out.println(pEvento.getRoom_id());
        System.out.println(pEvento.getSender());
        System.out.println(pEvento.getEvent_id());
        System.out.println(pEvento.getContent());
        System.out.println(pEvento.getRaw());
    }

    @Override
    public void eventoLeitura(ItfEventoMatix pEvento) {
        System.out.println(pEvento.getType());
        System.out.println(pEvento.getRoom_id());
        System.out.println(pEvento.getSender());
        System.out.println(pEvento.getEvent_id());
        System.out.println(pEvento.getContent());
        System.out.println(pEvento.getRaw());
    }

    @Override
    public boolean isElegivel(ItfEventoMatix pEvento) {
        System.out.println("Analizando elegivel evento:");
        System.out.println(pEvento.getType());
        System.out.println(pEvento.getRoom_id());
        System.out.println(pEvento.getSender());
        System.out.println(pEvento.getEvent_id());
        System.out.println(pEvento.getContent());
        System.out.println(pEvento.getRaw());
        return true;
    }

    @Override
    public void inicioProcessamento(ItfEventoMatix pEvento) {
        System.out.println("Inicio Processamento " + pEvento.getEvent_id());

    }

    @Override
    public void finalProcessamento(ItfEventoMatix pEvento) {
        System.out.println("Fim Processamento " + pEvento.getEvent_id());
    }

    @Override
    public void eventoReacao(ItfEventoMatix pEvento) {
        System.out.println("Evento reação disparado");
    }

    @Override
    public ComoChatSalaBean atualizarDtoSala() {
        System.out.println("Atualização de membros da sala requisitado");
        return null;
    }

    @Override
    public boolean isSalaComAutoMonitoramento(String pNomeSAla) {
        return false;
    }

}
