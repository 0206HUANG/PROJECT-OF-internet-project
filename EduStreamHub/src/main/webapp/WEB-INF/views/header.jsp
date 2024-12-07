<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
   <title>EduStream Hub</title>
   <style>
       body {
           margin: 0;
           font-family: Arial, sans-serif;
           background-color: #f4f6f8;
       }
       .header {
           background-color: #003366;
           color: white;
           padding: 10px 0;
           box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       }
       .logo {
           text-align: center;
       }
       .logo h1 {
           margin: 0;
           font-size: 2rem;
       }
       .nav {
           margin-top: 10px;
           text-align: center;
       }
       .nav ul {
           list-style: none;
           padding: 0;
           margin: 0;
           display: inline-block;
       }
       .nav ul li {
           display: inline;
           margin: 0 15px;
       }
       .nav ul li a {
           text-decoration: none;
           color: white;
           font-size: 1rem;
           font-weight: bold;
           transition: color 0.3s;
       }
       .nav ul li a:hover {
           color: #ffa726;
       }
   </style>
</head>
<body>
   <div class="header">
       <div class="logo">
           <h1>EduStream Hub</h1>
       </div>
       <div class="nav">
           <ul>
               <li><a href="${pageContext.request.contextPath}/adminDashboard">Dashboard</a></li>
               <li><a href="${pageContext.request.contextPath}/schools">Schools</a></li>
               <li><a href="${pageContext.request.contextPath}/contentLibrary">Content</a></li>
               <li><a href="${pageContext.request.contextPath}/reports">Reports</a></li>
               <li><a href="${pageContext.request.contextPath}/logout">Logout</a></li>
           </ul>
       </div>
   </div>
