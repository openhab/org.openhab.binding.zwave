package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ZWaveResponseHandler {
    int id();

    String name();
}
