package com.google.android.apps.signalong.utils;

import com.google.android.apps.signalong.BuildConfig;

/**
 * Created by yonglew@ on 10/19/18
 */
public class ConfigUtils {

    private static final String PROD_DOMAIN_NAME = "signalong.googleminiapps.cn";
    private static final String PROD_AGREEMENTS_URL = "https://signalong.googleminiapps.cn/agreements";

    private static final String STAGING_DOMAIN_NAME = "cslt-211408.appspot.com";
    private static final String STAGING_AGREEMENTS_URL = "https://storage.googleapis.com/cslt-211408.appspot.com/static/agreements";

    private static final String DEV_DOMAIN_NAME = "cslt-211408.appspot.com";
    private static final String DEV_AGREEMENTS_URL = "https://storage.googleapis.com/cslt-211408.appspot.com/static/agreements";


    public static String getDomainName() {
        if (BuildConfig.BUILD_TYPE == "release") {
            return PROD_DOMAIN_NAME;
        } else if (BuildConfig.BUILD_TYPE == "staging") {
            return STAGING_DOMAIN_NAME;
        } else {
            return DEV_DOMAIN_NAME;
        }
    }

    public static String getAgreementsUrl() {
        if (BuildConfig.BUILD_TYPE == "release") {
            return PROD_AGREEMENTS_URL;
        } else if (BuildConfig.BUILD_TYPE == "staging") {
            return STAGING_AGREEMENTS_URL;
        } else {
            return DEV_AGREEMENTS_URL;
        }
    }
}
