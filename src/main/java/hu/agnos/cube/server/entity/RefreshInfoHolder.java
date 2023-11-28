package hu.agnos.cube.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RefreshInfoHolder {

    private boolean refreshInProgress;
    private long lastRefreshTimeInMilliseconds;

    public RefreshInfoHolder() {
        refreshInProgress = false;
        lastRefreshTimeInMilliseconds = System.currentTimeMillis();
    }
    
}
