/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.pacotematrix;

import br.org.coletivoJava.fw.api.erp.chat.model.ComandoDeAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ErroComandoAtendimentoInvalido;
import br.org.coletivoJava.fw.api.erp.chat.ItfErpChatService;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.eventos.EventoSalaMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author salvio
 */
public class PacoteMatrixParsing {

    private final static String[] POINTS = {"timeline", "state", "account_data", "ephemeral", "invite_state"};
    private final static String[] POINTS_TOP = {"join", "invite", "leave"};

    public static PacoteDeEventosMatrix parseEventoSalas(JSONObject object, ItfErpChatService pChatSErvice) {
        List<ItfEventoMatix> roomEvents = new ArrayList<>();
        List<ComandoDeAtendimento> comandos = new ArrayList<>();
        for (String pointTop : POINTS_TOP) {

            if (!object.has("rooms")) {
                continue;
            }

            final JSONObject rooms = object.getJSONObject("rooms");

            if (!rooms.has(pointTop)) {
                continue;
            }

            JSONObject object1 = rooms.getJSONObject(pointTop);
            Iterator<String> keys = object1.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject room = object1.getJSONObject(key);

                for (String point : POINTS) {
                    if (!room.has(point)) {
                        continue;
                    }

                    JSONArray timeline = room.getJSONObject(point).getJSONArray("events");
                    for (int i = 0; i < timeline.length(); i++) {
                        JSONObject event = timeline.getJSONObject(i);
                        ItfEventoMatix evento = fetchRoomEvent(event, key);
                        boolean eventoComandoAtendimento = false;

                        if (evento.getContent().has("m.mentions")) {
                            JSONObject mensoes = evento.getContent().getJSONObject("m.mentions");
                            if (!mensoes.isEmpty()) {
                                JSONArray usuariosmensoes = mensoes.getJSONArray("user_ids");
                                for (int ii = 0; ii < usuariosmensoes.length(); ii++) {
                                    String valor = usuariosmensoes.getString(ii);
                                    if (valor.equals(pChatSErvice.getUsuarioAdmin().getCodigoUsuario())) {
                                        eventoComandoAtendimento = true;
                                    }
                                }
                            }

                        }
                        if (eventoComandoAtendimento) {
                            try {
                                ComandoDeAtendimento comando = new ComandoDeAtendimento((EventoSalaMatrix) evento);
                                comandos.add(comando);
                            } catch (ErroComandoAtendimentoInvalido ex) {
                                System.out.println("Comando inválido enviado por " + evento.getSender());
                                System.out.println(evento.getContent().toString(4));
                            }
                        } else {
                            roomEvents.add(evento);
                        }

                    }
                }
            }
        }
        return new PacoteDeEventosMatrix(roomEvents, comandos);
    }

    public static ItfEventoMatix fetchRoomEvent(JSONObject event) {
        return fetchRoomEvent(event, "");
    }

    public static ItfEventoMatix fetchRoomEvent(JSONObject event, String key) {
        String event_id = "", sender = "";
        try {
            event_id = event.getString("event_id");
        } catch (Exception ignore) {
        }
        try {
            sender = event.getString("sender");
        } catch (Exception ignore) {
        }

        return new EventoSalaMatrix(
                event,
                event.getString("type"),
                event_id,
                sender,
                key,
                event.getJSONObject("content")
        );
    }
}
