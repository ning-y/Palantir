package io.ningyuan.palantir;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "ningyuan@u.nus.edu")
@AcraDialog(resText=R.string.acra_title)
public class PalantirApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}
