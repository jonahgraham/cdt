/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class ExpIncludeTab extends AbstractExportTab {
	
	public ICLanguageSettingEntry doAdd(String s1, String s2) {
		int flags = 0;
		if (s1.equals(s2))  
			flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
		return new CIncludePathEntry(s2, flags);
	}

	public ICLanguageSettingEntry doEdit(String s1, String s2) {
		return doAdd(s1, s2);
	}
	
	public int getKind() { return ICSettingEntry.INCLUDE_PATH; }
	public boolean hasValues() { return false; }
}
