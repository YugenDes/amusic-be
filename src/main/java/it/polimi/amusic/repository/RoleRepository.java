package it.polimi.amusic.repository;

import it.polimi.amusic.model.document.RoleDocument;

public interface RoleRepository {

    RoleDocument findByAuthority(RoleDocument.RoleEnum roleEnum);

}
