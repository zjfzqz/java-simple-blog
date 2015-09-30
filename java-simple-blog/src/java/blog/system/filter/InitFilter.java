/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.system.filter;

import blog.model.auth.AuthModel;
import blog.system.loader.Load;
import blog.system.tools.ErrorPage;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author petroff
 */
@WebFilter("/*")
public class InitFilter implements Filter {

    private ServletContext context;

    public void init(FilterConfig fConfig) throws ServletException {
        this.context = fConfig.getServletContext();
    }

    public void doFilter(ServletRequest requestS, ServletResponse responseS, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) requestS;
        HttpServletResponse response = (HttpServletResponse) responseS;
        ErrorPage errorPage = new ErrorPage(request, response);
        Load load = new Load(request, response, errorPage);
        context.setAttribute("Load", load);
        setSession();
        chain.doFilter(request, response);
    }

    public void destroy() {
        //we can close resources here
    }

    private void setSession() {
        AuthModel authModel = new AuthModel();
        authModel.setSession();
    }

}