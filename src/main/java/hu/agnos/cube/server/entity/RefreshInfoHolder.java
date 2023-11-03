package hu.agnos.cube.server.entity;

import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RefreshInfoHolder {

    private boolean refreshInProgress;

    @Getter
    private long lastRefreshTimeInMilliseconds;

    public RefreshInfoHolder() {
        refreshInProgress = false;
        lastRefreshTimeInMilliseconds = System.currentTimeMillis();
    }

    public boolean isRefreshInProgress() {
        return refreshInProgress;
    }

    public void setRefreshInProgress(boolean refreshInProgress) {
        this.refreshInProgress = refreshInProgress;
    }

    public void setLastRefreshTimeInMilliseconds(long lastRefreshInMilliseconds) {
        this.lastRefreshTimeInMilliseconds = lastRefreshInMilliseconds;
    }

}
