/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreDataHora;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 *
 * @author salvio
 */
public abstract class EscutaSalaMatrixAbst implements RoomEventsCallback {

    private final ItfChatSalaBean sala;

    private final Date dataExpira;

    private final Date datainicial;

    public EscutaSalaMatrixAbst(ItfChatSalaBean pSala) {
        sala = pSala;
        datainicial = new Date();
        dataExpira = UtilSBCoreDataHora.incrementaHoras(datainicial, 24);

    }

    public ItfChatSalaBean getSala() {
        return sala;
    }

    public Date getDataExpira() {
        return dataExpira;
    }

    public Date getDatainicial() {
        return datainicial;
    }

    public void provessarEvento(RoomEvent pEvento) {
        String tipo = pEvento.getType();
        FabTipoEventoMatrix tipoEvento = FabTipoEventoMatrix.getTipoEventoByTypeStr(tipo);
        if (tipoEvento == null) {
            System.out.println("ignorando evento tipo:" + tipo);
        }
        switch (tipoEvento) {
            case MENSAGEM:
                eventoMensagem(pEvento);
                break;
            case DIGITANDO:
                eventoDigitando(pEvento);
                break;
            case LEITURA:
                eventoLeitura(pEvento);
                break;

            default:
                throw new AssertionError();
        }

    }

    @Override
    public synchronized void onEventReceived(List<RoomEvent> list) throws IOException {
        try {
            if (!list.isEmpty()) {
                if (!SBCore.isEmModoProducao()) {
                    System.out.println("Processando evento evento de sala com " + list.size() + " eventos");
                }
            }
            if (!list.isEmpty()) {
                for (RoomEvent evento : list) {
                    try {
                        String tipoEvento = evento.getType();
                        System.out.println(tipoEvento);
                        if (evento.getRoom_id() != null && getSala().getCodigoChat() != null) {
                            if (evento.getRoom_id().equals(getSala().getCodigoChat())) {
                                System.out.println("Processando evento " + getSala().getCodigoChat());
                                provessarEvento(evento);
                            }
                        }
                    } catch (Throwable t) {
                        System.out.println("Falha processando o evento:" + t.getMessage());
                        System.out.println(evento.getRaw());
                    }
                }
            }
        } catch (Throwable t) {

        }

    }

    public abstract void eventoDigitando(RoomEvent pEvento);

    public abstract void eventoMensagem(RoomEvent pEvento);

    public abstract void eventoLeitura(RoomEvent pEvento);

    @Override
    public String toString() {
        return "listener de:" + sala.getCodigoChat();
    }

}
