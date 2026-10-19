<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
   <meta charset="UTF-8">
   <meta name="viewport" content="width=device-width, initial-scale=1">
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
       .user-info {
           text-align: center;
           font-size: 0.9rem;
           margin-top: 5px;
           color: #cfd8e3;
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
       .alert {
           max-width: 1200px;
           margin: 20px auto 0;
           padding: 12px 20px;
           border-radius: 5px;
           box-sizing: border-box;
       }
       .alert-success {
           background-color: #e6f4ea;
           color: #1e6b34;
           border: 1px solid #b7dfc3;
       }
       .alert-error {
           background-color: #fdecea;
           color: #a12a1f;
           border: 1px solid #f5c2bd;
       }
       .footer {
           text-align: center;
           padding: 20px 0;
           margin-top: 40px;
           color: #666;
           font-size: 0.9rem;
       }
   </style>
</head>
<body>
   <div class="header">
       <div class="logo">
           <h1>EduStream Hub</h1>
       </div>
       <c:if test="${not empty sessionScope.currentUser}">
           <div class="user-info">
               Signed in as <c:out value="${sessionScope.currentUser.fullName}" /> (${sessionScope.currentUser.role})
           </div>
       </c:if>
       <div class="nav">
           <ul>
               <c:choose>
                   <c:when test="${sessionScope.currentUser.admin}">
                       <li><a href="${pageContext.request.contextPath}/adminDashboard">Dashboard</a></li>
                       <li><a href="${pageContext.request.contextPath}/schools">Schools</a></li>
                       <li><a href="${pageContext.request.contextPath}/contentLibrary">Content</a></li>
                       <li><a href="${pageContext.request.contextPath}/reports">Reports</a></li>
                   </c:when>
                   <c:otherwise>
                       <li><a href="${pageContext.request.contextPath}/teacherDashboard">Dashboard</a></li>
                       <li><a href="${pageContext.request.contextPath}/contentLibrary">Content</a></li>
                       <li><a href="${pageContext.request.contextPath}/content/upload">Upload</a></li>
                   </c:otherwise>
               </c:choose>
               <li><a href="${pageContext.request.contextPath}/logout">Logout</a></li>
           </ul>
       </div>
   </div>
   <c:if test="${not empty message}">
       <div class="alert alert-success"><c:out value="${message}" /></div>
   </c:if>
   <c:if test="${not empty error}">
       <div class="alert alert-error"><c:out value="${error}" /></div>
   </c:if>
