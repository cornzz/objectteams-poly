/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2008 Fraunhofer Gesellschaft, Munich, Germany,
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
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.help;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.objectteams.otdt.internal.ui.help.views.OTJLDView;
import org.eclipse.objectteams.otdt.ui.help.OTHelpPlugin;
import org.osgi.framework.Bundle;


/**
 * @author gis
 */
@SuppressWarnings("nls")
public class OTJLDError
{
	private ArrayList<OTURL> m_urls;
    private static final Pattern OTLD_MARKER_PATTERN = Pattern.compile(".*\\(OTJLD\\s*(.*)\\s*\\).*", Pattern.DOTALL);
    private static final Pattern SUBSECTION_PATTERN = Pattern.compile("(.*)\\s*\\(\\s*(.*)\\s*\\).*");
    public static final String URL_PATH = "guide/otjld/xdef/";
    private static final String IMAGES_URL_PATH = "guide/otjld/images/"; // just for initialization
    private static final String CSS_URL_PATH = "guide/otjld/css/"; // just for initialization
    
    static boolean contentInitialized = false;
    
    public static class OTURL
    {
        private URL m_url; // the plugin-relative url to the document
        private String m_anchor;
        
        private OTURL(URL url)
        {
            m_url = url;
            m_anchor = "";
        }
        
        public OTURL(URL url, String anchor)
        {
            m_url = url;
            
	        if (anchor != null && anchor.length() > 0)
	            m_anchor = "#" + anchor;
            else
                m_anchor = "";
        }
        
        public String toString()
        {
            return getURL();
        }
        
        public String getURL()
        {
            if (OTJLDView.hasBrowser())
                return getBrowserURL();
            
            return getHelpSystemURL();
        }
        
        private String getBrowserURL()
        {
            String result = null;
            try
            {
            	if (!contentInitialized) {
            		// need to touch the directories whose content we need extracted:
            		Bundle docPlugin = OTHelpPlugin.getDocPlugin();
					FileLocator.toFileURL(docPlugin.getEntry(OTJLDError.URL_PATH));
            		FileLocator.toFileURL(docPlugin.getEntry(OTJLDError.IMAGES_URL_PATH));
            		FileLocator.toFileURL(docPlugin.getEntry(OTJLDError.CSS_URL_PATH));
            		contentInitialized = true;
            	}
                result = FileLocator.toFileURL(m_url).toString();
                result += m_anchor;
            }
            catch (IOException ex)
            {
                OTHelpPlugin.logException("Unable to resolve url: + url", ex);
            }

            return result;
        }

        private String getHelpSystemURL()
        {
            return "/" + OTHelpPlugin.OT_DOC_BUNDLE + m_url.getPath() + m_anchor;
        }
        
    }
    
    public OTJLDError(String message)
    {
        parseMarkerMessage(message);
    }

    public OTURL[] getURLs()
    {
        if (m_urls == null || m_urls.isEmpty())
            return new OTURL[0];
        
        return m_urls.toArray(new OTURL[m_urls.size()]);
    }

    private void parseMarkerMessage(String text)
    {
        Matcher matcher = OTLD_MARKER_PATTERN.matcher(text);
        if (!matcher.matches())
            return;
        
        m_urls = new ArrayList<OTURL>();
        
        for (int i = 0; i < matcher.groupCount(); i++)
        {
            String decl = matcher.group(i + 1); // 1-based!
            parseParagraphs(decl.trim());
        }
    }

    // expected (but not guaranteed) input: something like 'x.y.z (a)'
    // TODO (possibly more than one reference! Maybe separated via commas?
    private void parseParagraphs(String decl)
    {
        String paragraph = null;
        String subSection = null;
            
        Matcher matcher = SUBSECTION_PATTERN.matcher(decl);
        if (matcher.matches())
        {
            assert (matcher.groupCount() == 2);
            paragraph  = matcher.group(1); // 1-based!
            subSection = matcher.group(2);
        }
        else
            paragraph = decl; // the entire message is the paragraph (i.e. '3.1.2')

        OTURL url = lookupURL(paragraph, subSection);
        if (url != null)
            m_urls.add(url);
    }

    private OTURL lookupURL(String paragraph, String subSection)
    {
        String ssFileNameAddon = "";
        if (subSection != null && subSection.length() > 0)
        	ssFileNameAddon = "." + subSection;
        String variablePart = paragraph + ssFileNameAddon;
        URL url = null;
        while (url == null) {
	        
			String file = URL_PATH + "s" + variablePart + ".html";
	        
	        url = OTHelpPlugin.getDocPlugin().getEntry(file);
	        
	        // in case an exact matching file is not found, use the parent section:
	        int lastDot = variablePart.lastIndexOf('.');
	        if (lastDot > -1)
	        	variablePart = variablePart.substring(0, lastDot);
	        else
	        	break;
        } 
        if (url == null)
            return null;

        return new OTURL(url); // no anchor, it's in the filename already
    }

    public static String getHome() {
    	return OTJLDError.URL_PATH+"index.html";
    }

    public static OTURL getHomepageURL() {
    	return new OTURL(OTHelpPlugin.getDocPlugin().getEntry(getHome()));
    }
    
    public static boolean isOTJProblem(String text) {
    	return OTLD_MARKER_PATTERN.matcher(text).matches();
    }
}
