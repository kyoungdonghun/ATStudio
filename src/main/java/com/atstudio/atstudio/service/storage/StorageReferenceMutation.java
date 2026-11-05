package com.atstudio.atstudio.service.storage;

import java.lang.annotation.*;

/** Admission must precede the business transaction, including null replacements and cascades. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StorageReferenceMutation { }
