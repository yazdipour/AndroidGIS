/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.maplibui.api;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import com.nextgis.maplib.datasource.Field;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Interface for formbuilder controls
 */
public interface IFormControl extends IControl {
    void init(JSONObject element, List<Field> fields, Bundle savedState, Cursor featureCursor, SharedPreferences lastValue) throws JSONException;
    void saveLastValue(SharedPreferences preferences);
    boolean isShowLast();
}
