package org.eclipse.objectteams.otredyn.runtime.dynamic.linker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.objectteams.otredyn.runtime.TeamManager;

import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Lookup;

public class TeamsAndCallinIdsLinker implements GuardingDynamicLinker {

	private static MethodHandle getTeamsAndCallinIds = null;

	private static Map<Integer, Object[]> cachedValues = new HashMap<>();

	private static final MethodHandle CACHED;

	static {
		CACHED = Lookup.findOwnStatic(MethodHandles.lookup(), "getCachedTeamsAndCallinIds", Object[].class, int.class);
	}

	private static void setTeamsAndCallinIds(MethodHandles.Lookup lookup) {
		if (getTeamsAndCallinIds == null) {
			try {
				getTeamsAndCallinIds = lookup.findStatic(TeamManager.class, "getTeamsAndCallinIds",
						MethodType.methodType(Object[].class, int.class));
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static MethodHandle getTeamsAndCallinIds(final MethodHandles.Lookup lookup) {
		setTeamsAndCallinIds(lookup);
		return getTeamsAndCallinIds;
	}

	@SuppressWarnings("unused")
	private static Object[] getCachedTeamsAndCallinIds(final int joinpointId) {
		Object[] value = null;
		if(cachedValues.containsKey(Integer.valueOf(joinpointId))) {
			value = cachedValues.get(Integer.valueOf(joinpointId));
		} else {
			value = TeamManager.getTeamsAndCallinIds(joinpointId);
			cachedValues.put(Integer.valueOf(joinpointId), value);
		}
		return value;
	}

	@Override
	public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices)
			throws Exception {
		final TeamsAndCallinIdsCallSiteDescriptor csd;
		if (linkRequest.getCallSiteDescriptor() instanceof TeamsAndCallinIdsCallSiteDescriptor) {
			csd = (TeamsAndCallinIdsCallSiteDescriptor) linkRequest.getCallSiteDescriptor();
		} else {
			throw new IllegalArgumentException();
		}

		final GuardedInvocation result;
		// Check if the callsite is unstable
		if (linkRequest.isCallSiteUnstable()) {
			// Behave as there is no invokedynamic
			final MethodHandle target = getTeamsAndCallinIds(csd.getLookup());
			result = new GuardedInvocation(target);
		} else {
			final int joinpointId = csd.getJoinpointId();
			cachedValues.remove(joinpointId);
			final MethodHandle target = CACHED;
			final SwitchPoint sp = new SwitchPoint();
			TeamManager.registerSwitchPoint(joinpointId, sp);
			result = new GuardedInvocation(target, sp);
		}
		return result;
	}
}
