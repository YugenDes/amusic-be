package it.polimi.amusic.service.persistance;

import it.polimi.amusic.model.document.RoleDocument;

public interface RoleService {

    RoleDocument findByAuthority(RoleDocument.RoleEnum roleEnum);

}
