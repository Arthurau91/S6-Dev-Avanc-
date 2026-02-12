<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Connexion - MasterAnnonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        .login-container { margin-top: 100px; max-width: 400px; }
    </style>
</head>
<body>
<div class="container login-container">
    <div class="panel panel-primary">
        <div class="panel-heading">Connexion</div>
        <div class="panel-body">

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
            <div class="alert alert-danger"><%= error %></div>
            <% } %>

            <form action="login" method="POST">
                <div class="form-group">
                    <label>Username</label>
                    <input type="text" name="username" class="form-control" required autofocus>
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" name="password" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Se connecter</button>
            </form>

            <br>
            <div class="alert alert-info">
                Compte de test : <b>testuser</b> / <b>password123</b>
            </div>
        </div>
    </div>
</div>
</body>
</html>