package org.opencds.cqf.library;

import org.hl7.elm.r1.VersionedIdentifier;

import java.io.InputStream;
import java.util.HashMap;

public class LibraryHashMap extends HashMap<VersionedIdentifier, InputStream> {

    @Override
    public boolean containsKey(Object id) {
        if (id instanceof VersionedIdentifier) {
            VersionedIdentifier identifier = (VersionedIdentifier) id;
            for (Entry<VersionedIdentifier, InputStream> entry : this.entrySet()) {
                if (entry.getKey().getId().equals(identifier.getId())
                        && entry.getKey().getVersion().equals(identifier.getVersion()))
                {
                    return true;
                }
            }
        }

        return false;
    }
}