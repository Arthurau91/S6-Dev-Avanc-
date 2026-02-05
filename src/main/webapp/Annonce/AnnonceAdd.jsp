<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Ajouter une annonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <style>
        .container { margin-top: 30px; max-width: 600px; }
        .form-group { margin-bottom: 15px; }
    </style>
</head>
<body>

<div class="container">
    <div class="panel panel-default">
        <div class="panel-body">
            <%
                String message = (String) request.getAttribute("message");
                String alertType = (String) request.getAttribute("alertType");

                if (message != null) {
            %>
            <div class="alert <%= alertType %> alert-dismissible" role="alert">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <strong>Information :</strong> <%= message %>
            </div>
            <%
                }
            %>
            <form action="annonce-add" method="POST">

                <div class="form-group">
                    <label>Title</label>
                    <input type="text" name="title" class="form-control" placeholder="Enter title" required>
                </div>

                <div class="form-group">
                    <label>Description</label>
                    <textarea name="description" class="form-control" rows="3" placeholder="Description"></textarea>
                </div>

                <div class="form-group">
                    <label>Adress</label>
                    <input type="text" name="adress" class="form-control" placeholder="Enter adress">
                </div>

                <div class="form-group">
                    <label>Mail</label>
                    <input type="email" name="mail" class="form-control" placeholder="Enter mail">
                </div>

                <button type="submit" class="btn btn-primary">Save</button>

            </form>
        </div>
    </div>
</div>

</body>
</html>