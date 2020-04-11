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
 * $Id: AbstractSmapGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;

/**
 * @author ike
 */
public abstract class AbstractSmapGenerator
{

    protected TypeDeclaration _type;
    protected List <SmapStratum>_strata;
	private String _defaultStratum;

	private static String DEFAULT_PACKAGE=""; //$NON-NLS-1$

    public AbstractSmapGenerator(TypeDeclaration type)
    {
        this._type = type;
        this._strata =  new ArrayList<SmapStratum>();
    }

    public void addStratum(String stratum)
    {
        this._strata.add(new SmapStratum(stratum));
    }

    public abstract char[] generate();

    @SuppressWarnings("nls")
	public String getSMAP()
    {
        String generatedFileName = getClassFileNameForType(this._type);
        StringBuffer out = new StringBuffer();

        // print Header
        out.append("SMAP\n");
        out.append(generatedFileName + "\n");

        // print defaultstratum
        if (this._defaultStratum != null)
        	out.append(this._defaultStratum + "\n");
        else
        	out.append(ISMAPConstants.OTJ_STRATUM_NAME + "\n");

        // print strata
        for (int idx = 0; idx < this._strata.size(); idx++)
        {
            SmapStratum stratum = this._strata.get(idx);
            if (stratum.hasFileInfos())
            {
                stratum.optimize();
                out.append(stratum.getSmapAsString());
            }
        }

        // print EndSection
        out.append("*E");

        return out.toString();
    }

    private String getClassFileNameForType(TypeDeclaration type)
    {
        String generatedFileName = String.valueOf(type.binding.getRealClass().constantPoolName())
        						   + ISMAPConstants.OTJ_CLASS_ENDING;

        String [] tmp = generatedFileName.split("/"); //$NON-NLS-1$

        if (tmp.length > 0)
            return tmp[tmp.length-1];

        return generatedFileName;
    }

    public List<SmapStratum> getStrata()
    {
        return this._strata;
    }

	public void setDefaultStratum(String defaultStratum)
	{
		this._defaultStratum = defaultStratum;
	}

	/** Find the nearest enclosing type (possibly type itself) that is a toplevel type in any compilation unit. */
	protected ReferenceBinding getCUType(ReferenceBinding type) {
		ReferenceBinding currentType = type;
		ReferenceBinding enclosingType = type.enclosingType();
		while (true) {
			if (currentType.roleModel != null && currentType.roleModel.isRoleFile())
				return currentType;	// found a role file
			enclosingType = currentType.enclosingType();
			if (enclosingType == null)
				return currentType; // found a true toplevel type
			currentType = enclosingType;
		}
	}

	protected String getPackagePathFromRefBinding(ReferenceBinding toplevelBinding) {
	    PackageBinding pkgBinding = null;
	    if (toplevelBinding.enclosingType() != null)
	    	pkgBinding = toplevelBinding.enclosingType().teamPackage;
	    if (pkgBinding == null)
	    	pkgBinding = toplevelBinding.getPackage();
	    String pkgName = String.valueOf(pkgBinding.readableName());
	    pkgName = pkgName.replace('.',ISMAPConstants.OTJ_PATH_DELIMITER_CHAR);

	    if (pkgName != null &&  pkgName.length() > 0)
	        return pkgName + ISMAPConstants.OTJ_PATH_DELIMITER;

	    return DEFAULT_PACKAGE;
	}

	protected FileInfo getOrCreateFileInfoForType(SmapStratum stratum, ReferenceBinding typeBinding) {
		String sourceName = String.valueOf(typeBinding.sourceName()) + ISMAPConstants.OTJ_JAVA_ENDING;
		String absoluteSourceName = getPackagePathFromRefBinding(typeBinding) + sourceName;
		return stratum.getOrCreateFileInfo(sourceName, absoluteSourceName);
	}
}
