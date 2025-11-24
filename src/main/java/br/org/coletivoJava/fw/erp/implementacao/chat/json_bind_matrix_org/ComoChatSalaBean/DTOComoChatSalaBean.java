package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ComoChatSalaBean;

import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ComoChatSalaBean.ItfDTOChatSalaBean;
import br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.ComoChatSalaBean.JsonBindDTOChatSalaBean;
import com.super_bits.modulosSB.SBCore.integracao.libRestClient.api.erp.dto.DTO_SBGENERICO;

public class DTOComoChatSalaBean extends DTO_SBGENERICO<ItfDTOChatSalaBean>
        implements
        ItfDTOChatSalaBean {

    public DTOComoChatSalaBean(String pJson) {
        super(JsonBindDTOChatSalaBean.class, pJson);
    }

    public DTOComoChatSalaBean() {
        super(null, null);
    }

}
