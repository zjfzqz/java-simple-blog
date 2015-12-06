<!DOCTYPE html>
<%@tag description="Template" pageEncoding="UTF-8"%>
<%@taglib prefix="a" tagdir="/WEB-INF/tags/" %>
<%@attribute name="title"%>
<%@attribute name="head_area" fragment="true" %>
<%@attribute name="body_area" fragment="true" %>
<%@attribute name="navigator_area" fragment="true" %>
<%@attribute name="auth_area" fragment="true" %>
<%@attribute name="i18n_area" fragment="true" %>

<html>
    <head>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="/static/css/jquery-ui.min.css">
        <link rel="stylesheet" href="/static/css/jquery-ui.theme.min.css">
        <link rel="stylesheet" href="/static/css/jquery-ui.structure.min.css">
        <script src="/static/js/jquery-2.1.4.min.js" type="text/javascript" encoding="UTF-8"></script>
        <script src="/static/js/jquery-ui.min.js" type="text/javascript" encoding="UTF-8"></script>
        <script src="/static/js/common.js" type="text/javascript" encoding="UTF-8"></script>
        <!-- Bootstrap -->
        <link href="/static/b/css/bootstrap.min.css" rel="stylesheet">
        <script src="/static/b/js/bootstrap.min.js"></script>

        <title>${title}</title>
        <jsp:invoke fragment="head_area"/>
    </head>
    <body>

        <div class="row">
            <div class="col-md-2"><a:auth auth="${Load}"/></div>
            <div class="col-md-1"><a:i18n /></div>
        </div>

        <jsp:invoke fragment="navigator_area"/>
        <jsp:invoke fragment="body_area"/>

    </body>

</html>