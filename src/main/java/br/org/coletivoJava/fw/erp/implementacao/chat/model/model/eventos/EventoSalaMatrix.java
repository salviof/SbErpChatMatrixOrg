package br.org.coletivoJava.fw.erp.implementacao.chat.model.model.eventos;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;
import br.org.coletivoJava.fw.api.erp.chat.model.FabTipoPacoteDeAcaoMatrix;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import org.json.JSONObject;

/**
 *
 * @author salvio
 */
public class EventoSalaMatrix extends RoomEvent implements ItfEventoMatix {

    private final FabTipoPacoteDeAcaoMatrix tipoEvento;

    public EventoSalaMatrix(JSONObject raw, String type, String event_id, String sender, String room_id, JSONObject content) {
        super(raw, type, event_id, sender, room_id, content);
        tipoEvento = FabTipoPacoteDeAcaoMatrix.getTipoEventoByTypeStr(type);

    }

    @Override
    public FabTipoPacoteDeAcaoMatrix getTipoEvento() {
        return tipoEvento;
    }

}
