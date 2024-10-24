package com.sunfun.slideshow.pojo;

public class PurchaseInfo
{
    private String inAppSubsStartIndex;

    private String[] subscriptions;

    private String isLaunchRandom;

    private String buttonTitle;

    private String count;

    private String subsOP;

    private String launchSubscription;

    private NewSubscription newSubscription;

    private String launchScreenBool;

    private String isInAppRandom;

    private String inAppSubscription;

    private String showSubscriptionInFirstLaunch;

    private String exportRestriction;

    private String subscriptionToView;

    private String skpop;

    public String getInAppSubsStartIndex ()
    {
        return inAppSubsStartIndex;
    }

    public void setInAppSubsStartIndex (String inAppSubsStartIndex)
    {
        this.inAppSubsStartIndex = inAppSubsStartIndex;
    }

    public String[] getSubscriptions ()
    {
        return subscriptions;
    }

    public void setSubscriptions (String[] subscriptions)
    {
        this.subscriptions = subscriptions;
    }

    public String getIsLaunchRandom ()
    {
        return isLaunchRandom;
    }

    public void setIsLaunchRandom (String isLaunchRandom)
    {
        this.isLaunchRandom = isLaunchRandom;
    }

    public String getButtonTitle ()
    {
        return buttonTitle;
    }

    public void setButtonTitle (String buttonTitle)
    {
        this.buttonTitle = buttonTitle;
    }

    public String getCount ()
    {
        return count;
    }

    public void setCount (String count)
    {
        this.count = count;
    }

    public String getSubsOP ()
    {
        return subsOP;
    }

    public void setSubsOP (String subsOP)
    {
        this.subsOP = subsOP;
    }

    public String getLaunchSubscription ()
    {
        return launchSubscription;
    }

    public void setLaunchSubscription (String launchSubscription)
    {
        this.launchSubscription = launchSubscription;
    }

    public NewSubscription getNewSubscription ()
    {
        return newSubscription;
    }

    public void setNewSubscription (NewSubscription newSubscription)
    {
        this.newSubscription = newSubscription;
    }

    public String getLaunchScreenBool ()
    {
        return launchScreenBool;
    }

    public void setLaunchScreenBool (String launchScreenBool)
    {
        this.launchScreenBool = launchScreenBool;
    }

    public String getIsInAppRandom ()
    {
        return isInAppRandom;
    }

    public void setIsInAppRandom (String isInAppRandom)
    {
        this.isInAppRandom = isInAppRandom;
    }

    public String getInAppSubscription ()
    {
        return inAppSubscription;
    }

    public void setInAppSubscription (String inAppSubscription)
    {
        this.inAppSubscription = inAppSubscription;
    }

    public String getShowSubscriptionInFirstLaunch ()
    {
        return showSubscriptionInFirstLaunch;
    }

    public void setShowSubscriptionInFirstLaunch (String showSubscriptionInFirstLaunch)
    {
        this.showSubscriptionInFirstLaunch = showSubscriptionInFirstLaunch;
    }

    public String getExportRestriction ()
    {
        return exportRestriction;
    }

    public void setExportRestriction (String exportRestriction)
    {
        this.exportRestriction = exportRestriction;
    }

    public String getSubscriptionToView ()
    {
        return subscriptionToView;
    }

    public void setSubscriptionToView (String subscriptionToView)
    {
        this.subscriptionToView = subscriptionToView;
    }

    public String getSkpop ()
    {
        return skpop;
    }

    public void setSkpop (String skpop)
    {
        this.skpop = skpop;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [inAppSubsStartIndex = "+inAppSubsStartIndex+", subscriptions = "+subscriptions+", isLaunchRandom = "+isLaunchRandom+", buttonTitle = "+buttonTitle+", count = "+count+", subsOP = "+subsOP+", launchSubscription = "+launchSubscription+", newSubscription = "+newSubscription+", launchScreenBool = "+launchScreenBool+", isInAppRandom = "+isInAppRandom+", inAppSubscription = "+inAppSubscription+", showSubscriptionInFirstLaunch = "+showSubscriptionInFirstLaunch+", exportRestriction = "+exportRestriction+", subscriptionToView = "+subscriptionToView+", skpop = "+skpop+"]";
    }
}

