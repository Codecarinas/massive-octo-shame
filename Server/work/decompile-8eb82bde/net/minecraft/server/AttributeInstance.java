package net.minecraft.server;

import java.util.Collection;
import java.util.UUID;

public interface AttributeInstance {

    IAttribute getAttribute();

    double b();

    void setValue(double d0);

    Collection a(int i);

    Collection c();

    boolean a(AttributeModifier attributemodifier);

    AttributeModifier a(UUID uuid);

    void b(AttributeModifier attributemodifier);

    void c(AttributeModifier attributemodifier);

    double getValue();
}
