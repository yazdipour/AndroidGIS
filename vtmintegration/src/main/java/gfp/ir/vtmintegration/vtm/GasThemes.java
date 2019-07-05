/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm;

import org.oscim.backend.AssetAdapter;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeFile;
import org.oscim.theme.XmlRenderThemeMenuCallback;

import java.io.InputStream;

public enum GasThemes implements ThemeFile {

    DEFAULT("vtm/gas_default.xml");

    private final String mPath;

    GasThemes(String path) {
        mPath = path;
    }

    @Override
    public XmlRenderThemeMenuCallback getMenuCallback() {
        return null;
    }

    @Override
    public String getRelativePathPrefix() {
        return "";
    }

    @Override
    public InputStream getRenderThemeAsStream() throws IRenderTheme.ThemeException {
        return AssetAdapter.readFileAsStream(mPath);
    }

    @Override
    public boolean isMapsforgeTheme() {
        return false;
    }

    @Override
    public void setMenuCallback(XmlRenderThemeMenuCallback menuCallback) {
    }
}
