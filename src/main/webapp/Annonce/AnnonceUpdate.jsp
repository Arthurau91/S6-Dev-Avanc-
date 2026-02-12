<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Modifier l'annonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <style>.container { margin-top: 30px; max-width: 600px; }</style>
</head>
<body>
<div class="container">
    <div class="panel panel-info">
        <div class="panel-heading">Modifier l'annonce</div>
        <div class="panel-body">

            <form action="annonce-update" method="POST">
                <input type="hidden" name="id" value="${annonce.id}">

                <div class="form-group">
                    <label>Title</label>
                    <input type="text" name="title" class="form-control" value="${annonce.title}" required>
                </div>

                <div class="form-group">
                    <label>Description</label>
                    <textarea name="description" class="form-control" rows="3">${annonce.description}</textarea>
                </div>

                <div class="form-group">
                    <label>Adress</label>
                    <input type="text" name="adress" class="form-control" value="${annonce.adress}">
                </div>

                <div class="form-group">
                    <label>Mail</label>
                    <input type="email" name="mail" class="form-control" value="${annonce.mail}">
                </div>

                <div class="form-group">
                    <label>Catégorie</label>
                    <select name="categoryId" class="form-control" required>
                        <c:forEach items="${categories}" var="cat">
                            <option value="${cat.id}" ${annonce.category.id == cat.id ? 'selected' : ''}>
                                    ${cat.label}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <button type="submit" class="btn btn-info">Modifier</button>
                <a href="annonce-list" class="btn btn-default">Retour à la liste</a>
            </form>

        </div>
    </div>
</div>
</body>
</html>