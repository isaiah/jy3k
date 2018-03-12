package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;

/**
 * Linkable by a dynamic linker
 */
public interface DynLinkable {
    /**
     * Find the __call__ method of the object
     *
     * @param desc
     * @param linkRequest
     * @return
     */
    GuardedInvocation findCallMethod(CallSiteDescriptor desc, LinkRequest linkRequest);
}
