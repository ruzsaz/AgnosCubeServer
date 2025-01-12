package hu.agnos.cube.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is used to store information about the last refresh time and the
 * current refresh status.
 */
@Component
@Getter
@Setter
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RefreshInfoHolder {

    private boolean refreshInProgress;
    private long lastRefreshTimeInMilliseconds;

    /**
     * Default constructor.
     */
    public RefreshInfoHolder() {
        refreshInProgress = false;
        lastRefreshTimeInMilliseconds = System.currentTimeMillis();
    }

    /**
     * Sets the refreshInProgress flag to true and updates the last refresh time.
     */
    public void setRefreshInProgress(boolean refreshInProgress) {
        setLastRefreshTimeInMilliseconds(System.currentTimeMillis());
        this.refreshInProgress = refreshInProgress;
    }

}
