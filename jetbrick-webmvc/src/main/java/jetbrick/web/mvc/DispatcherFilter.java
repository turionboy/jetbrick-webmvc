/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 *   Author: Guoqiang Chen
 *    Email: subchen@gmail.com
 *   WebURL: https://github.com/subchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.web.mvc;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class DispatcherFilter implements Filter {
    private Dispatcher dispatcher;

    @Override
    public void init(FilterConfig fc) throws ServletException {
        dispatcher = new Dispatcher(fc.getServletContext(), fc.getInitParameter("configLocation"));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        boolean serviced = dispatcher.service((HttpServletRequest) req, (HttpServletResponse) resp);
        if (serviced == false) {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void destroy() {
        dispatcher.destroy();
    }
}
