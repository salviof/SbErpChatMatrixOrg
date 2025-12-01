/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix.listeners;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoChatSalaBean;
import static br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix.ATUALIZACAO_MEMBROS;
import static br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix.DIGITANDO;
import static br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix.LEITURA;
import static br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix.MENSAGEM;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilCRCDataHora;
import br.org.coletivoJava.fw.api.erp.chat.ErroMtxParalizacaoDeProcessamento;
import br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;
import java.util.Date;
import org.coletivojava.fw.api.tratamentoErros.FabErro;

/**
 *
 * @author salvio
 */
public abstract class EscutaSalaMatrixAbst implements ItfListenerEventoMatrix {

    private ComoChatSalaBean sala;

    private final Date dataExpira;

    private final Date datainicial;

    protected ComoChatSalaBean setSalaAtualizada(ComoChatSalaBean pDadosSalaAtualizados) {
        if (!pDadosSalaAtualizados.getCodigoChat().equals(sala.getCodigoChat())) {
            throw new UnsupportedOperationException("a Sala não pde ser alterada, apenas atualizada");
        }
        sala = pDadosSalaAtualizados;
        return sala;
    }

    public EscutaSalaMatrixAbst(ComoChatSalaBean pSala) {
        sala = pSala;
        datainicial = new Date();
        dataExpira = UtilCRCDataHora.incrementaHoras(datainicial, 24);

    }

    public ComoChatSalaBean getSala() {
        return sala;
    }

    public Date getDataExpira() {
        return dataExpira;
    }

    public Date getDatainicial() {
        return datainicial;
    }

    public abstract ComoChatSalaBean atualizarDtoSala();

    public abstract void eventoDigitando(ItfEventoMatix pEvento);

    public abstract void eventoMensagem(ItfEventoMatix pEvento);

    public abstract void eventoLeitura(ItfEventoMatix pEvento);

    public abstract void eventoReacao(ItfEventoMatix pEvento);

    public abstract void inicioProcessamento(ItfEventoMatix pEvento) throws ErroMtxParalizacaoDeProcessamento;

    public abstract void finalProcessamento(ItfEventoMatix pEvento);

    public boolean isElegivel(ItfEventoMatix pEvento) {
        if (pEvento == null) {
            return false;
        }
        if (pEvento.getTipoEvento() == null) {
            return false;
        }
        if (pEvento.getRoom_id() == null) {
            return false;
        }
        if (getSala() == null) {
            return false;
        }
        if (!getSala().getCodigoChat().equals(pEvento.getRoom_id())) {
            return false;
        }
        return true;
    }

    private synchronized void processar(ItfEventoMatix pEvento) throws ErroMtxParalizacaoDeProcessamento {
        String tipo = pEvento.getType();
        FabTipoPacoteDeAcaoMatrix tipoEvento = pEvento.getTipoEvento();
        if (tipoEvento == null) {
            System.out.println("ignorando evento tipo:" + tipo);
            return;
        }

        try {

            inicioProcessamento(pEvento);
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
                case ATUALIZACAO_MEMBROS:
                    ComoChatSalaBean salaDTOAtualizado = atualizarDtoSala();
                    if (salaDTOAtualizado != null) {
                        sala = salaDTOAtualizado;
                    }
                    break;
                case REACAO:
                    eventoReacao(pEvento);
                    break;

                default:
                    throw new AssertionError();
            }
        } finally {
            finalProcessamento(pEvento);
        }

    }

    @Override
    public synchronized void processarEvento(ItfEventoMatix pEvento) throws ErroMtxParalizacaoDeProcessamento {

        if (!SBCore.isEmModoProducao()) {
            System.out.println("Processando evento evento de sala id " + pEvento.getEvent_id() + " eventos");
        }

        try {
            String tipoEvento = pEvento.getType();
            System.out.println(tipoEvento);
            if (pEvento.getRoom_id() != null && getSala().getCodigoChat() != null) {
                if (pEvento.getRoom_id().equals(getSala().getCodigoChat())) {
                    System.out.println("Processando evento " + getSala().getCodigoChat());
                    processar(pEvento);
                }
            }
        } catch (ErroMtxParalizacaoDeProcessamento t) {
            throw t;
        } catch (Throwable t) {
            System.out.println("Falha processando o evento:" + t.getMessage());
            System.out.println(pEvento.getRaw());
        }
    }

    @Override
    public String toString() {
        return "listener de:" + sala.getCodigoChat();
    }

}
