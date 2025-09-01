package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix;

import br.org.coletivoJava.fw.erp.implementacao.chat.ChatMatrixOrgimpl;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import de.jojii.matrixclientserver.Networking.HttpHelper;

/**
 *
 * @author salvio
 */
public class SincronizacaoSalasMatrix extends SincronizacaoAbstrata {

    private static final int LONG_POLLING_TIMEOUT = 40000;

    private static final String filter = "{\n"
            + "  \"event_fields\": [\n"
            + "    \"type\",\n"
            + "    \"content\",\n"
            + "    \"sender\",\n"
            + "    \"room_id\",\n"
            + "    \"event_id\"\n"
            + "  ],\n"
            + "\"presence\": {\n"
            + "    \"types\": []\n"
            + "  },"
            + "  \"room\": {\n"
            + "    \"state\": {\n"
            + "      \"types\": [\"m.room.*\"]\n"
            + "    },\n"
            + "    \"ephemeral\": {\n"
            + "     \"types\": [\"m.typing\", \"m.receipt\"] \n"
            + "    }\n"
            + "  }\n"
            + "}";

    public SincronizacaoSalasMatrix(SessaoMatrix pSessao, HttpHelper httpHelper) {
        super(pSessao, httpHelper, LONG_POLLING_TIMEOUT, filter);

    }

    @Override
    public boolean isEventoCompativelListener(ItfListenerEventoMatrix pListener, ItfEventoMatix pEvento) {
        if (pEvento.getRoom_id() != null && pListener.getSala() != null) {
            if (pEvento.getRoom_id().equals(pListener.getSala().getCodigoChat())) {
                return true;
            }
        }
        return false;

    }

}
