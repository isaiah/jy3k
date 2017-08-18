package org.python.core.linker;

import jdk.dynalink.beans.BeansLinker;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;

public class JyBeansLinker implements TypeBasedGuardingDynamicLinker {
    private final BeansLinker beansLinker;

    public JyBeansLinker(BeansLinker beansLinker) {
        this.beansLinker = beansLinker;
    }

    @Override
    public boolean canLinkType(Class<?> type) {
        return false;
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        return null;
    }
}
