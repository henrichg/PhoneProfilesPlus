package sk.henrichg.phoneprofilesplus;

import android.support.annotation.NonNull;

class Application {
    int type = TYPE_APPLICATION;
    String appLabel = "";
    String packageName = "";
    String activityName = "";
    long shortcutId = 0;
    long intentId = 0;
    boolean checked = false;
    int startApplicationDelay;

    static final int TYPE_APPLICATION = 1;
    static final int TYPE_SHORTCUT = 2;
    static final int TYPE_INTENT = 3;

    Application() {
    }

    @NonNull
    public String toString() {
        return appLabel;
    }

    void toggleChecked() {
        checked = !checked;
    }

    static boolean isShortcut(String value) {
        if (value.length() > 2) {
            String shortcut = value.substring(0, 3);
            return shortcut.equals("(s)");
        }
        return false;
    }

    static boolean isIntent(String value) {
        if (value.length() > 2) {
            String intent = value.substring(0, 3);
            return intent.equals("(i)");
        }
        return false;
    }

    public static String getPackageName(String value) {
        PPApplication.logE("@ Application.getPackageName", "value="+value);
        if (value.length() > 2) {
            String packageName = "";
            String shortcutIntent;
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                shortcutIntent = packageNameActivity[0].substring(0, 3);
                packageName = packageNameActivity[0];
                if (shortcutIntent.equals("(s)")) {
                    // shortcut
                    packageName = packageNameActivity[0].substring(3);
                }
                else
                if (shortcutIntent.equals("(i)")) {
                    // intent
                    packageName = "";
                }
            }
            else {
                // activity not exists
                shortcutIntent = value.substring(0, 3);
                if (!shortcutIntent.equals("(s)") && !shortcutIntent.equals("(i)"))
                    // application
                    packageName = value;
            }
            PPApplication.logE("@ Application.getPackageName", "packageName="+packageName);
            return packageName;
        }
        else
            return "";
    }

    static String getActivityName(String value) {
        PPApplication.logE("@ Application.getActivityName", "value="+value);
        if (value.length() > 2) {
            String activityName = "";
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                String shortcutIntent = packageNameActivity[0].substring(0, 3);
                if (!shortcutIntent.equals("(i)")) {
                    // application, shortcut
                    String[] activityShortcutIdDelay = packageNameActivity[1].split("#");
                    activityName = activityShortcutIdDelay[0];
                }
            }
            PPApplication.logE("@ Application.getActivityName", "activityName="+activityName);
            return activityName;
        }
        else
            return "";
    }

    static long getShortcutId(String value) {
        PPApplication.logE("@ Application.getShortcutId", "value="+value);
        if (value.length() > 2) {
            long shortcutId = 0;
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                String shortcut = packageNameActivity[0].substring(0, 3);
                String[] activityShortcutIdDelay = packageNameActivity[1].split("#");
                if (shortcut.equals("(s)")) {
                    // shortcut
                    if (activityShortcutIdDelay.length >= 2)
                        try {
                            shortcutId = Long.parseLong(activityShortcutIdDelay[1]);
                        } catch (Exception ignored) {}
                }
            }
            PPApplication.logE("@ Application.getShortcutId", "shortcutId="+shortcutId);
            return shortcutId;
        }
        else
            return 0;
    }

    static long getIntentId(String value) {
        PPApplication.logE("@ Application.getIntentId", "value="+value);
        if (value.length() > 2) {
            long intentId = 0;
            String[] intentIdDelay = value.split("#");
            String intent = intentIdDelay[0].substring(0, 3);
            if (intent.equals("(i)")) {
                // intent
                try {
                    intentId = Long.parseLong(intentIdDelay[0].substring(3));
                } catch (Exception ignored) {}
            }
            PPApplication.logE("@ Application.getIntentId", "intentId="+intentId);
            return intentId;
        }
        else
            return 0;
    }

    static int getStartApplicationDelay(String value) {
        PPApplication.logE("@ Application.getStartApplicationDelay", "value="+value);
        if (value.length() > 2) {
            String shortcutIntent;
            int startApplicationDelay = 0;
            String[] packageNameActivity = value.split("/");
            if (packageNameActivity.length == 2) {
                // activity exists
                shortcutIntent = packageNameActivity[0].substring(0, 3);
                String[] activityShortcutIdDelay = packageNameActivity[1].split("#");
                if (shortcutIntent.equals("(s)")) {
                    // shortcut
                    if (activityShortcutIdDelay.length >= 3) {
                        try {
                            startApplicationDelay = Integer.parseInt(activityShortcutIdDelay[2]);
                        } catch (Exception ignored) {
                        }
                    }
                    else
                    if (activityShortcutIdDelay.length >= 2) {
                        try {
                            startApplicationDelay = Integer.parseInt(activityShortcutIdDelay[1]);
                        } catch (Exception ignored) {
                        }
                    }
                }
                else
                if (!shortcutIntent.equals("(i)")) {
                    // application
                    if (activityShortcutIdDelay.length >= 2) {
                        try {
                            startApplicationDelay = Integer.parseInt(activityShortcutIdDelay[1]);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                // activity not exists
                shortcutIntent = value.substring(0, 3);
                if (shortcutIntent.equals("(i)")) {
                    try {
                        startApplicationDelay = Integer.parseInt(packageNameActivity[0]);
                    } catch (Exception ignored) {}
                }
                else {
                    String[] packageNameDelay = value.split("#");
                    if (packageNameDelay.length >= 2) {
                        try {
                            startApplicationDelay = Integer.parseInt(packageNameDelay[1]);
                        } catch (Exception ignored) {}
                    }
                }
            }
            PPApplication.logE("@ Application.getStartApplicationDelay", "startApplicationDelay="+startApplicationDelay);
            return startApplicationDelay;
        }
        else
            return 0;
    }

}