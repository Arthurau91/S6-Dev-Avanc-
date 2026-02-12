<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Liste des annonces</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        .container { margin-top: 20px; max-width: 900px; }
        .label-status { font-size: 0.8em; margin-left: 10px; }
        .header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
    </style>
</head>
<body>

<div class="container">

    <div class="header-row">
        <div>
            <span class="glyphicon glyphicon-user"></span> Connecté en tant que :
            <strong>${sessionScope.user.username}</strong>
            <a href="logout" class="btn btn-danger btn-xs" style="margin-left: 10px;">Se déconnecter</a>
        </div>
        <div>
            <a href="annonce-add" class="btn btn-success">
                <span class="glyphicon glyphicon-plus"></span> Nouvelle annonce
            </a>
        </div>
    </div>

    <div class="well well-sm">
        <form action="annonce-list" method="GET" class="form-inline">
            <div class="form-group">
                <input type="text" name="keyword" class="form-control" placeholder="Mot-clé..." value="${currentKeyword}">
            </div>
            <div class="form-group">
                <select name="categoryId" class="form-control">
                    <option value="">Toutes les catégories</option>
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.id}" ${cat.id == currentCategoryId ? 'selected' : ''}>${cat.label}</option>
                    </c:forEach>
                </select>
            </div>
            <button type="submit" class="btn btn-primary">Rechercher</button>
            <a href="annonce-list" class="btn btn-default">Reset</a>
        </form>
    </div>

    <div class="list-group">
        <c:forEach items="${listeAnnonces}" var="annonce">
            <div class="list-group-item clearfix">
                <div class="pull-left">
                    <h4 class="list-group-item-heading">
                            ${annonce.title}
                        <c:choose>
                            <c:when test="${annonce.status == 'PUBLISHED'}"><span class="label label-success label-status">Publiée</span></c:when>
                            <c:when test="${annonce.status == 'ARCHIVED'}"><span class="label label-default label-status">Archivée</span></c:when>
                            <c:otherwise><span class="label label-warning label-status">Brouillon</span></c:otherwise>
                        </c:choose>
                    </h4>
                    <p class="list-group-item-text text-muted">
                        <span class="glyphicon glyphicon-tag"></span> ${annonce.category.label} |
                        <span class="glyphicon glyphicon-user"></span> ${annonce.author.username} <br>
                            ${annonce.description}
                    </p>
                </div>
                <div class="pull-right" style="margin-top: 10px;">
                    <a href="annonce-update?id=${annonce.id}" class="btn btn-xs btn-warning"><span class="glyphicon glyphicon-pencil"></span></a>
                    <a href="annonce-delete?id=${annonce.id}" class="btn btn-xs btn-danger" onclick="return confirm('Confirmer la suppression ?');"><span class="glyphicon glyphicon-trash"></span></a>
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty listeAnnonces}">
            <div class="alert alert-warning">Aucune annonce trouvée pour ces critères.</div>
        </c:if>
    </div>

    <nav aria-label="Page navigation">
        <ul class="pager">
            <li class="${currentPage <= 1 ? 'disabled' : ''}">
                <a href="${currentPage <= 1 ? '#' : 'annonce-list?page='}${currentPage - 1}&keyword=${currentKeyword}&categoryId=${currentCategoryId}">
                    <span aria-hidden="true">&larr;</span> Précédent
                </a>
            </li>

            <li><span>Page ${currentPage} / ${maxPage}</span></li>

            <li class="${currentPage >= maxPage ? 'disabled' : ''}">
                <a href="${currentPage >= maxPage ? '#' : 'annonce-list?page='}${currentPage + 1}&keyword=${currentKeyword}&categoryId=${currentCategoryId}">
                    Suivant <span aria-hidden="true">&rarr;</span>
                </a>
            </li>
        </ul>
    </nav>
</div>

</body>
</html>