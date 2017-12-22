/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.widget;

public class AoColumnDef {
	private boolean bVisible;
	private boolean bSortable;
	private String sClass = "";
	private String sWidth;
	private int[] aTargets = new int[1];
	private String mDataProp;

	public boolean isbVisible() {
		return bVisible;
	}

	public boolean isbSortable() {
		return bSortable;
	}

	public String getsClass() {
		return sClass;
	}

	public String getsWidth() {
		return sWidth;
	}

	public int[] getaTargets() {
		return aTargets;
	}

	public String getmDataProp() {
		return mDataProp;
	}

	public AoColumnDef(boolean bVisible, boolean bSortable, String sClass, String sWidth, 
			String mDataProp) {
		super();
		this.bVisible = bVisible;
		this.bSortable = bSortable;
		this.sClass = sClass;
		this.sWidth = sWidth;
		
		this.mDataProp = mDataProp;
	}

	public void setbVisible(boolean bVisible) {
		this.bVisible = bVisible;
	}

	public void setbSortable(boolean bSortable) {
		this.bSortable = bSortable;
	}

	public void setsClass(String sClass) {
		this.sClass = sClass;
	}

	public void setsWidth(String sWidth) {
		this.sWidth = sWidth;
	}

	public void setaTargets(int[] aTargets) {
		this.aTargets = aTargets.clone();
	}

	public void setmDataProp(String mDataProp) {
		this.mDataProp = mDataProp;
	}
	

}
