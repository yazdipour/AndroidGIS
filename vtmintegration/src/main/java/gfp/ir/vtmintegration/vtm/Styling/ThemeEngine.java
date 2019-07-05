/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.Styling;

import com.github.lzyzsd.randomcolor.RandomColor;

import org.oscim.backend.canvas.Color;
import org.oscim.theme.RenderTheme;
import org.oscim.theme.rule.RuleBuilder;
import org.oscim.theme.styles.RenderStyle;

import java.util.ArrayList;
import java.util.List;

import gfp.ir.vtmintegration.geolibrary.style.Style;
import gfp.ir.vtmintegration.geolibrary.util.types.EDataType;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import gfp.ir.vtmintegration.vtm.Styling.enums.Type;

public class ThemeEngine extends Style {

    private double minvalue;

    private double maxvalue;

    private int classes;

    private Type styletype;


    private List<Color> colorList;
    private int[] colors;

    private RandomColor.Color color;
    RandomColor randomColor;

    private List<cat> catList= new ArrayList();
    private List<String> singleclassnames= new ArrayList();

//    public Style(Type type_,double min,double max,Color start,Color end,int classes_,RandomColor.Color cl,
//                 List<String> singleclassnames_){
//        styletype=type_;
//        minvalue=min;
//        maxvalue=max;
//        startcolor=start;
//        endtcolor=end;
//        classes=classes_;
//        color=cl;
//        randomColor = new RandomColor();
//        colors=randomColor.random(cl, classes);
//        singleclassnames=singleclassnames_;
//        init();
//    }

    SpatialVectorTable mVectorTable;
    public ThemeEngine(SpatialiteDatabaseHandler dbhandeldr, SpatialVectorTable vectorTable){

        mVectorTable=vectorTable;
        EDataType stylefieldtype=mVectorTable.getTableFieldType(mVectorTable.getStyle().themeField);

        switch (stylefieldtype){
            case TEXT:
                styletype=Type.SINGLECOLOR;

                singleclassnames= dbhandeldr.getdistinctfield(mVectorTable,mVectorTable.getStyle().themeField);
                minvalue=0;
                maxvalue=1;
                classes=singleclassnames.size();
                color=RandomColor.Color.PINK;
                init();

        }



    }

    public ThemeEngine(double min,double max,int classes_,RandomColor.Color cl){
        styletype=Type.CATEGOURIZE;
        minvalue=min;
        maxvalue=max;
        classes=classes_;
        color=cl;
        init();
    }

    public ThemeEngine(double min,double max,int classes_){
        this(min,max,classes_, RandomColor.Color.PINK);
    }

    public ThemeEngine( List<String> singleclassnames_,RandomColor.Color cl){
        styletype=Type.SINGLECOLOR;
        minvalue=0;
        maxvalue=1;
        classes=singleclassnames_.size();

        color=cl;

        singleclassnames=singleclassnames_;
        init();
    }


    public ThemeEngine(List<String> singleclassnames_){

        this(singleclassnames_,RandomColor.Color.PINK);
    }


    public cat getCat(String value){

        if(styletype==Type.SINGLECOLOR){

            for(int i=0;i<catList.size();i++){
                if(catList.get(i).title.equals(value))
                    return catList.get(i);
            }

        }
            return null;

    }

    public cat getCat(double value){

        if(styletype!=Type.SINGLECOLOR){

            for(int i=0;i<catList.size();i++){
                if(catList.get(i).blongs(value))
                    return catList.get(i);
            }

        }
        return null;

    }

    private void init() {
        randomColor = new RandomColor();
        colors=randomColor.random(color, classes);
        double distance=(maxvalue-minvalue)/classes;
        double min=minvalue;
        double max=min+distance;

        switch (styletype){
            case GRADIENT:
            case CATEGOURIZE:
                //for numeric fields

                for(int i=0;i<classes;i++){

                    cat cat=new cat(min,(max),colors[i],Double.toString(max)+"-"+Double.toString(min));
                    min=max;
                    max+=distance;
                    catList.add(cat);
                }

                break;

            case SINGLECOLOR:
                //for string fields
                for (int i=0;i<singleclassnames.size();i++){

                    cat cat=new cat(min,(max),colors[i],singleclassnames.get(i));
                    min=max;
                    max+=distance;
                    catList.add(cat);
                }
                break;
        }
    }


   public static class Theme extends ThemeBuilder {
        public Theme() {}
    }

    public Theme getThem() {


        for (int i=0;i<catList.size();i++){

        }
        return null;
    }



    String mThemField;

    public List<RuleBuilder> getCatRules(RenderStyle.StyleBuilder Basestyle){
        List<RuleBuilder> list=new ArrayList<>();
        for (int i=0;i<catList.size();i++){

            RuleBuilder r=  new RuleBuilder(RuleBuilder.RuleType.POSITIVE,
                    new String[]{mThemField},
                    new String[]{catList.get(i).getTitle()});

            RenderStyle.StyleBuilder rstyle=Basestyle;
            rstyle.fillColor=catList.get(i).getCl();
            r.style(rstyle);
            list.add(r);
        }
        return list;
    }

    class cat{

        double up;
        double down;
        int cl;
        String title;

        public cat(double up_, double down_, int cl_,String title_)
        {
            up=up_;
            down=down_;
            cl=cl_;
            title=title_;
        }

        double getUp(){
            return up;
        }

        String getTitle(){
            return title;
        }

        double getDown(){
            return  down;
        }
        public boolean blongs(double value){

            if(up<value || down<value ||down==value){
                return true;
            }
            return false;
        }

        public int getCl(){
            return cl;
        }
    }

}
