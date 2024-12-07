<!-- schoolList.jsp -->
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for schoolList.jsp */
   .content-container {
       max-width: 1200px;
       margin: 20px auto;
       padding: 20px;
       background-color: #f9f9f9;
       border-radius: 8px;
       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       font-family: Arial, sans-serif;
   }
   .content-container h1 {
       text-align: center;
       color: #003366;
       margin-bottom: 20px;
   }
   .btn {
       display: inline-block;
       padding: 10px 20px;
       margin-bottom: 20px;
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
   table {
       width: 100%;
       border-collapse: collapse;
       margin-top: 10px;
       background-color: #fff;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       border-radius: 8px;
       overflow: hidden;
   }
   table thead {
       background-color: #003366;
       color: white;
   }
   table thead th {
       text-align: left;
       padding: 12px 15px;
       font-size: 1rem;
   }
   table tbody td {
       padding: 12px 15px;
       border-bottom: 1px solid #ddd;
       font-size: 0.9rem;
       color: #333;
   }
   table tbody tr:hover {
       background-color: #f1f1f1;
   }
</style>
<div class="content-container">
   <h1>Schools List</h1>
   <a href="${pageContext.request.contextPath}/schools/add" class="btn">Add New School</a>
   <table>
       <thead>
           <tr>
               <th>School Name</th>
           </tr>
       </thead>
       <tbody>
           <c:forEach var="school" items="${schools}">
               <tr>
                   <td>${school}</td>
               </tr>
           </c:forEach>
       </tbody>
   </table>
</div>
<%@ include file="footer.jsp" %>

