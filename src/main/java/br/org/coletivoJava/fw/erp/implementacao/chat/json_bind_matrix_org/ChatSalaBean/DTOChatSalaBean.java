package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ChatSalaBean;

import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ChatSalaBean.ItfDTOChatSalaBean;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ChatSalaBean.JsonBindDTOChatSalaBean;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.DTO_SBGENERICO;

public class DTOChatSalaBean extends DTO_SBGENERICO<ItfDTOChatSalaBean>
        implements
        ItfDTOChatSalaBean {

    public DTOChatSalaBean(String pJson) {
        super(JsonBindDTOChatSalaBean.class, pJson);
    }

    public DTOChatSalaBean() {
        super(null, null);
    }

}
