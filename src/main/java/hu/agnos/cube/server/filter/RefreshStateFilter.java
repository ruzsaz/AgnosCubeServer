package hu.agnos.cube.server.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import hu.agnos.cube.server.entity.RefreshInfoHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RefreshStateFilter implements Filter {

    private final static long MILLISBETWEENREFRESHES = 0;

    @Autowired
    private RefreshInfoHolder refreshInfoHolder;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (refreshInfoHolder.isRefreshInProgress()) {
            res.sendError(503);
        } else if (req.getRequestURI().endsWith("/refresh")
                && System.currentTimeMillis() < refreshInfoHolder.getLastRefreshTimeInMilliseconds() + MILLISBETWEENREFRESHES ) {
            res.sendError(503);
        } else {
            chain.doFilter(req, res);
        }
    }

}
