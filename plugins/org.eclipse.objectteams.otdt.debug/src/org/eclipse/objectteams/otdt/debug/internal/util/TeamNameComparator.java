/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.internal.util;

import java.util.Comparator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.objectteams.otdt.debug.TeamInstance;

public class TeamNameComparator implements Comparator
{
	public int compare(Object o1, Object o2)
	{
		TeamInstance team1 = (TeamInstance)o1;
		TeamInstance team2 = (TeamInstance)o2;
		
		int order = 0;
		try
		{
			String teamName1 = team1.getReferenceTypeName();
			String teamName2 = team2.getReferenceTypeName();
			order = teamName1.compareTo(teamName2);
		}
		catch (DebugException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return order;
	}
}