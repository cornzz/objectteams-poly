/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
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
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

import java.util.Collection;
import java.util.Vector;

/** Collects all unmapped and mapped lineinfos in use within a stratum.
 * @author ike
 */
public class LineInfoCollector
{
    private Vector <LineInfo>_lineInfos;

    public LineInfoCollector()
    {
        this._lineInfos = new Vector<LineInfo>();
    }

    public void storeLineInfos(Collection<LineInfo> c)
    {
        this._lineInfos.addAll(c);
    }

    public void storeLineInfo(LineInfo lineInfo)
    {
        this._lineInfos.add(lineInfo);
    }

    /**
     * Does the given output line (as used in byte code) already have a re-mapping?
     */
    public boolean existsLineInfoFor(int outputStartLinenumber)
    {
        if (this._lineInfos == null || (this._lineInfos.size() < 1))
        {
            return false;
        }
        else
        {
            for (int idx = 0; idx < this._lineInfos.size(); idx++)
            {
                LineInfo lineInfo = this._lineInfos.elementAt(idx);
                if ((outputStartLinenumber == lineInfo.getOutputStartLine()) && !lineInfo.hasRepeatCount())
                {
                    return true;
                }
                else
                {
                    int startOutput= lineInfo.getOutputStartLine();
                    int endOutput = lineInfo.hasRepeatCount() ? (startOutput + lineInfo.getRepeatCount() -1 ): startOutput;

                    if((outputStartLinenumber >= startOutput) && (outputStartLinenumber <= endOutput))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
