<!-- teacherDashboard.jsp -->
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for teacherDashboard.jsp */
   .dashboard-container {
       max-width: 1200px;
       margin: 20px auto;
       padding: 20px;
       background-color: #f9f9f9;
       border-radius: 8px;
       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       font-family: Arial, sans-serif;
   }
   .dashboard-container h1 {
       text-align: center;
       color: #003366;
       margin-bottom: 20px;
   }
   .dashboard-container h2 {
       color: #003366;
       margin-top: 20px;
       margin-bottom: 15px;
   }
   .my-classes ul {
       list-style: none;
       padding: 0;
   }
   .my-classes ul li {
       background: #fff;
       padding: 10px 15px;
       margin-bottom: 10px;
       border-radius: 5px;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       font-size: 1rem;
       color: #333;
   }
   .btn {
       display: inline-block;
       padding: 10px 20px;
       margin-bottom: 15px;
       background-color: #003366;
       color: white;
       text-decoration: none;
       border-radius: 5px;
       font-size: 1rem;
       transition: background-color 0.3s;
   }
   .btn:hover {
       background-color: #0055a5;
   }
</style>
<div class="dashboard-container">
   <h1>Teacher Dashboard</h1>
   <div class="my-classes">
       <h2>My Classes</h2>
       <ul>
           <c:forEach var="classItem" items="${myClasses}">
               <li>${classItem}</li>
           </c:forEach>
       </ul>
   </div>
   <!-- Remove content management or assume some content is available -->
   <div class="content-management">
       <h2>Content Management</h2>
       <a href="${pageContext.request.contextPath}/content/upload" class="btn">Upload New Content</a>
       <!-- If myContent is not provided, remove or mock -->
       <ul>
           <li>No content available</li>
       </ul>
   </div>
</div>
<%@ include file="footer.jsp" %>

