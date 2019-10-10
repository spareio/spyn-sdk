# Spyn Android SDK
Spyn Android SDK allows you to offer Spyn Launcher in your own app. SpynSDK is our Android module which will allow your Android app to communicate with Spyn.

# Help

Integration questions can be answered by email (contact@spare.io) or by slack channel (request an invite from your account manager).

# Installation

- Open project level gradle and make following changes
```appgradle
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
- Open Project level gradle and make following changes
```projectlevel
dependencies {
		implementation 'com.github.User:Repo:0.0.5'
	}
```
# Initialisation
- Create Application class or Open if you have already. 
You can find more details to create application class from here https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class

Make Following changes :
```
public class MyApplication extends Application {

    public static String dealId = "place your deal id here";

    @Override
    public void onCreate() {
        super.onCreate();
          Spyn.initSpyn(null, dealId, "en", this);
    }

}
```

# Usage Overview

You can start using it now. Whenver necessary get instance of spyne and call the functions.

```java
Spyn.getInstance();
```


## Offering Spyn

Offering Spyn is the process in which the user will be prompted to Install Spyn Launcher in order to unlock a specific reward. When and where you decide to offer Spyn is up to you, and can be fully customizable. You can launch the Spyn offer flow using the `offerSpyn` function:

```java
Spyn.getInstance().offerSpyn();
```

Once the Spyn offer process is initiated with the function above. The SpynSDK takes over and will guide the user through out install flow. Currently the SpynSDK has 2 installation flow:

- [Standard Flow](#standard-flow)
- [Salvation Abandon Flow](#salvation-abandon-flow)

### Standard Flow

Standard flow can be integrated into any call-to-action requesting that the user unlock your reward. The most common application is to add a call-to-action (i.e. a button specifying 'Free! Upgrade using Spyn'), that when clicked should trigger a call to `spynSDK.offerSpyn();` which will initiate the Spyn standard installation flow.

The Spyn install flow consists of a few different screens. The first screen `Interstitial`, displays the Spyn offer along with some information, prompting the user to accept and install Spyn (Fig. 1).

Once the user clicks on the download call-to-action, they are taken to the PlayStore (Fig. 2) to install Spyn Launcher. Once the launcher is installed, a success screen `Success` is shown (Fig. 3).

Clicking on the "Continue to Spyn" button opens the Spyn Launcher. Once the Spyn Launcher is installed and run for the first time, the deal becomes activated and your app can detect this.

![Standard Flow](./docs/standard-flow.png?raw=true "Standard Flow")

### Salvation Abandon Flow

The Salvation Abandon flow helps capture users who have been sent to the play purchase screen but cancel or fail to pay.

![Abandon Flow](./docs/abandon-flow.png?raw=true "Abandon Flow")

By overriding the `onPurchasesUpdated()` method, Spyn can be offered to users who have failed to purchase, and spyn will be shown after the user returns unsuccessfully from the play purchase screen. Below is an example of how this method can be overridden:

```java
@Override
void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
    if (billingResult.getResponseCode() == BillingResponse.OK
            && purchases != null) {
        for (Purchase purchase : purchases) {
            handlePurchase(purchase);
        }
    } else if (billingResult.getResponseCode() == BillingResponse.USER_CANCELED) {
        Spyn.getInstance().salvageAbandon();
    } else {
        // Handle any other error codes.
    }
}
```

# Special Requirements
Spyn uses an sqlite database locally on each device to store information about the users device and deal. It is important that this information persist in the event that the user deletes your app. If a user uninstalls, and reinstalls, we need to know that this is the same user on the same device. To accomplish this, the sqlite database is included in the standard Android backup procedure. The app therefore needs to have backups enabled in the manifest file. This can be accomplished by using the following code:
```xml
<manifest ... >
    <application android:allowBackup="true">
    </application>
</manifest>
```

