package com.sunfun.slideshow.pojo;

public class Data
{
    private PurchaseInfo purchaseInfo;

    private String numberofFreeVideo;

    private Songfeatures songfeatures;

    private String issubscription;

    public PurchaseInfo getPurchaseInfo ()
    {
        return purchaseInfo;
    }

    public void setPurchaseInfo (PurchaseInfo purchaseInfo)
    {
        this.purchaseInfo = purchaseInfo;
    }

    public String getNumberofFreeVideo ()
    {
        return numberofFreeVideo;
    }

    public void setNumberofFreeVideo (String numberofFreeVideo)
    {
        this.numberofFreeVideo = numberofFreeVideo;
    }

    public Songfeatures getSongfeatures ()
    {
        return songfeatures;
    }

    public void setSongfeatures (Songfeatures songfeatures)
    {
        this.songfeatures = songfeatures;
    }

    public String getIssubscription ()
    {
        return issubscription;
    }

    public void setIssubscription (String issubscription)
    {
        this.issubscription = issubscription;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [purchaseInfo = "+purchaseInfo+", numberofFreeVideo = "+numberofFreeVideo+", songfeatures = "+songfeatures+", issubscription = "+issubscription+"]";
    }
}