/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2016  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gfp.ir.vtmintegration.geolibrary.database;

/**
 * The default helper classes used by geopaparazzi.
 * <p/>
 * <p>These can be substituted through own implementations following the interfaces.</p>
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DefaultHelperClasses {

    /**
     * Helper class for images.
     * <p/>
     * <P>Follows interface {@link IImagesDbHelper}</P>
     */
    public static String IMAGE_HELPER_CLASS = "eu.geopaparazzi.core.database.DaoImages";

    /**
     * Helper class for gps logs.
     * <p/>
     * <P>Follows interface {@link IGpsLogDbHelper}</P>
     */
    public static String GPSLOG_HELPER_CLASS = "eu.geopaparazzi.core.database.DaoGpsLog";

    public static IImagesDbHelper getDefaulfImageHelper() throws Exception {
        Class<?> logHelper = Class.forName(IMAGE_HELPER_CLASS);
        IImagesDbHelper imagesDbHelper = (IImagesDbHelper) logHelper.newInstance();
        return imagesDbHelper;
    }
}
