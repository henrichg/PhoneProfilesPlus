package sk.henrichg.phoneprofilesplus;

import java.util.concurrent.ScheduledFuture;

class SheduledFutureWidgetData {

    final int appWidgetId;
    ScheduledFuture<?> scheduledFutures;

    SheduledFutureWidgetData(int appWidgetId, ScheduledFuture<?> scheduledFutures)
    {
        this.appWidgetId = appWidgetId;
        this.scheduledFutures = scheduledFutures;
    }

}
