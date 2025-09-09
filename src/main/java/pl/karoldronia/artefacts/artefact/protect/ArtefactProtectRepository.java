package pl.karoldronia.artefacts.artefact.protect;

import eu.okaeri.persistence.repository.DocumentRepository;
import eu.okaeri.persistence.repository.annotation.DocumentCollection;

import java.util.UUID;

@DocumentCollection(path = "artefact_protects", keyLength = 36)
public interface ArtefactProtectRepository extends DocumentRepository<UUID, ArtefactProtect> {
}
