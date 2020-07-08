package org.eclipse.objectteams.otredyn.runtime.dynamic.linker;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkerServices;

public final class CallinBootstrap {

	private final static int unstableRelinkThreshold = 8;

	private final static GuardingDynamicLinker prioritizedLinkers;

	static {
		prioritizedLinkers = new CallinLinker();
	}

	private CallinBootstrap() {
	}

	static GuardedInvocation asTypeSafeReturn(final GuardedInvocation inv, final LinkerServices linkerServices,
			final CallSiteDescriptor desc) {
		return inv == null ? null : inv.asTypeSafeReturn(linkerServices, desc.getMethodType());
	}

	public static CallSite bootstrap(final Lookup lookup, final String name, final MethodType type, final int flags,
			final String joinpointDesc, final int boundMethodId) {
		return createDynamicLinker(lookup.lookupClass().getClassLoader(), unstableRelinkThreshold)
				.link(CallinCallSite.newCallinCallSite(lookup, name, type, flags, joinpointDesc, boundMethodId));
	}

	public static DynamicLinker createDynamicLinker(final ClassLoader classLoader, final int unstableRelinkThreshold) {
		final DynamicLinkerFactory factory = new DynamicLinkerFactory();
		factory.setPrioritizedLinker(prioritizedLinkers);
		factory.setUnstableRelinkThreshold(unstableRelinkThreshold);
		factory.setClassLoader(classLoader);
		return factory.createLinker();
	}
}