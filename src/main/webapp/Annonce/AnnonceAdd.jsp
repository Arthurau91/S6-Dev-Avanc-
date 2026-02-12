<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Ajouter une annonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <style>.container { margin-top: 30px; max-width: 600px; }</style>
</head>
<body>

<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">Nouvelle Annonce</div>
        <div class="panel-body">

            <c:if test="${not empty errors}">
                <div class="alert alert-danger">
                    <strong>Erreur(s) :</strong>
                    <ul>
                        <c:forEach items="${errors}" var="err">
                            <li>${err}</li>
                        </c:forEach>
                    </ul>
                </div>
            </c:if>

            <form action="annonce-add" method="POST">

                <div class="form-group">
                    <label>Titre *</label>
                    <input type="text" name="title" class="form-control"
                           value="${formAnnonce.title}" required>
                </div>

                <div class="form-group">
                    <label>Description *</label>
                    <textarea name="description" class="form-control" rows="3">${formAnnonce.description}</textarea>
                </div>

                <div class="form-group">
                    <label>Adresse</label>
                    <input type="text" name="adress" class="form-control"
                           value="${formAnnonce.adress}">
                </div>

                <div class="form-group">
                    <label>Mail</label>
                    <input type="email" name="mail" class="form-control"
                           value="${formAnnonce.mail}">
                </div>

                <div class="form-group">
                    <label>Catégorie *</label>
                    <select name="categoryId" class="form-control" required>
                        <option value="">-- Choisir --</option>
                        <c:forEach items="${categories}" var="cat">
                            <option value="${cat.id}" ${cat.id == selectedCatId ? 'selected' : ''}>
                                    ${cat.label}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <button type="submit" class="btn btn-primary">Enregistrer</button>
                <a href="annonce-list" class="btn btn-default">Retour à la liste</a>

            </form>
        </div>
    </div>
</div>
</body>
</html>