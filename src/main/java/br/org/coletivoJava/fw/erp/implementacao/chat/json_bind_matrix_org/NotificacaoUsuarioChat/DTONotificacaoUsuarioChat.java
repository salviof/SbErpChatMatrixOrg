package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.NotificacaoUsuarioChat;

import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.NotificacaoUsuarioChat.ItfDTONotificacaoUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.NotificacaoUsuarioChat.JsonBindDTONotificacaoUsuarioChat;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.DTO_SBGENERICO;

public class DTONotificacaoUsuarioChat
		extends
			DTO_SBGENERICO<ItfDTONotificacaoUsuarioChat>
		implements
			ItfDTONotificacaoUsuarioChat {

	public DTONotificacaoUsuarioChat(String pJson) {
		super(JsonBindDTONotificacaoUsuarioChat.class, pJson);
	}

	public DTONotificacaoUsuarioChat() {
		super(null, null);
	}
}