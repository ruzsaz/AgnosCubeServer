package hu.agnos.cube.server.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import hu.agnos.cube.server.entity.RefreshInfoHolder;

/**
 * Filter to check if a refresh is in progress or if the refresh is too frequent.
 */
@Component
@Order(1)
public class RefreshStateFilter implements Filter {

    private static final long MILLISBETWEENREFRESHES = 0;
    private static final int HTML_ERROR_CODE = 503;

    @Autowired
    private RefreshInfoHolder refreshInfoHolder;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        if (refreshInfoHolder.isRefreshInProgress()) {
            res.sendError(HTML_ERROR_CODE);
        } else if (req.getRequestURI().endsWith("/refresh")
                && System.currentTimeMillis() < refreshInfoHolder.getLastRefreshTimeInMilliseconds() + MILLISBETWEENREFRESHES) {
            res.sendError(HTML_ERROR_CODE);
        } else {
            filterChain.doFilter(req, res);
        }
    }

}
