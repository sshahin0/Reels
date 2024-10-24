package com.sunfun.slideshow.pojo;

public class Subtitle
{
    private String dummy;

    private String size;

    private String text;

    public String getDummy ()
    {
        return dummy;
    }

    public void setDummy (String dummy)
    {
        this.dummy = dummy;
    }

    public String getSize ()
    {
        return size;
    }

    public void setSize (String size)
    {
        this.size = size;
    }

    public String getText ()
    {
        return text;
    }

    public void setText (String text)
    {
        this.text = text;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [dummy = "+dummy+", size = "+size+", text = "+text+"]";
    }
}

