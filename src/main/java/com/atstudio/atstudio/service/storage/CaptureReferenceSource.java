package com.atstudio.atstudio.service.storage;

import java.util.List;

@FunctionalInterface
public interface CaptureReferenceSource {
    List<Reference> snapshot();

    record Reference(String root, String key, boolean required, boolean present) { }
}
