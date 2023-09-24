package hu.agnos.cube.server.entity;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RefreshInfoHolder {

    private boolean isRefreshInProgress;

    private long lastRefreshTimeInMilliseconds;

    public RefreshInfoHolder() {
        isRefreshInProgress = false;
        lastRefreshTimeInMilliseconds = System.currentTimeMillis();
    }

    public boolean isRefreshInProgress() {
        return isRefreshInProgress;
    }

    public void setRefreshInProgress(boolean refreshInProgress) {
        isRefreshInProgress = refreshInProgress;
    }

    public long getLastRefreshTimeInMilliseconds() {
        return lastRefreshTimeInMilliseconds;
    }

    public void setLastRefreshTimeInMilliseconds(long lastRefreshTimeInMilliseconds) {
        this.lastRefreshTimeInMilliseconds = lastRefreshTimeInMilliseconds;
    }

}
