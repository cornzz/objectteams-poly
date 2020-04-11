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
package org.eclipse.objectteams.otdt.tests.otmodel.role.unbound;

import junit.framework.Test;

/**
 * @author mkr
 * @version $Id: Test5b.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting
 * with a role class in a role file.
 */
public class Test5b extends Test5a
{

    public Test5b(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test5b.class);
        }
        junit.framework.TestSuite suite = new Suite(Test5b.class
            .getName());
        return suite;
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_5b");
    }

}
