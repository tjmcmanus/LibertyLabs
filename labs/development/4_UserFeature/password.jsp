<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<html>
<head>
<title>index</title>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
You are <%=request.getUserPrincipal().getName() %>

<form method="post" action="ChangePassword">
  Change Password <input type="password" name="password"/> <br />
  <input type="submit" name="submit" />
</form>
</body>
</html>