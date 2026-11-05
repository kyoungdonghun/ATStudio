package com.atstudio.atstudio.service.storage;

import java.lang.annotation.*;

/** Admission-only phase: no business transaction/catalog lock during analysis or file staging. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StorageReferencePreparation { }
