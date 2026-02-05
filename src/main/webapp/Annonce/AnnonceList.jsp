<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Liste des annonces</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        .container { margin-top: 30px; max-width: 800px; }
        .list-group-item { overflow: hidden; }
        .add-btn { margin-top: 15px; }
    </style>
</head>
<body>

<div class="container">

    <div class="list-group">

        <c:forEach items="${listeAnnonces}" var="annonce">

            <div class="list-group-item clearfix">

                <div class="pull-left">
                    <h4 class="list-group-item-heading">${annonce.title}</h4>
                    <p class="list-group-item-text text-muted">
                            ${annonce.mail} - ${annonce.date}
                    </p>
                </div>

                <div class="pull-right" style="margin-top: 10px;">

                    <a href="annonce-update?id=${annonce.id}" class="btn btn-xs btn-warning" title="Modifier">
                        <span class="glyphicon glyphicon-pencil"></span>
                    </a>

                    <a href="annonce-delete?id=${annonce.id}" class="btn btn-xs btn-danger"
                       onclick="return confirm('Êtes-vous sûr de vouloir supprimer cette annonce ?');" title="Supprimer">
                        <span class="glyphicon glyphicon-trash"></span>
                    </a>

                </div>
            </div>

        </c:forEach>

        <c:if test="${empty listeAnnonces}">
            <div class="list-group-item">Aucune annonce disponible.</div>
        </c:if>

    </div>

    <a href="annonce-add" class="btn btn-default add-btn">
        <span class="glyphicon glyphicon-plus"></span> Add an item
    </a>

</div>

</body>
</html>