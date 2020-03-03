/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.util;

import java.util.Vector;
import java.util.Iterator;

import org.eclipse.objectteams.example.flightbooking.model.Segment;


/**
 * This class implements a list of <code>Segment</code>s.
 */
public class SegmentList
{
	private Vector<Segment> _data;

	public SegmentList()
	{
		 _data = new Vector<Segment>();
	}
	
	public void add(Segment seg)
	{
		if (!_data.contains(seg))
		{
			_data.add(seg);
		}
	}

	public void remove(Segment seg)
	{
		_data.remove(seg);
	}

	public Segment get(int idx)
	{
		return _data.elementAt(idx);
	}

	/** How many segments does this list contain? */
	public int getSegmentCount() {
	    return _data.size();
	}

	public SegmentIterator getIterator()
	{
		final Iterator<Segment> rawIter = _data.iterator();

		return new SegmentIterator()
		{

			public boolean hasNext()
			{
				return rawIter.hasNext();
			}

			public Segment getNext()
			{
				return rawIter.next();
			}
		};
	}
}
