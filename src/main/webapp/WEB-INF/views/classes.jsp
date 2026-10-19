<%-- classes.jsp --%>
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for classes.jsp */
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
   .content-container h2 {
       color: #003366;
       margin: 30px 0 15px;
   }
   table {
       width: 100%;
       border-collapse: collapse;
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
   table tbody td a {
       color: #0055a5;
       text-decoration: none;
   }
   .class-form {
       background: #fff;
       padding: 20px;
       border-radius: 8px;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
   }
   .class-form .form-group {
       margin-bottom: 15px;
   }
   .class-form label {
       display: block;
       font-weight: bold;
       margin-bottom: 5px;
       color: #333;
   }
   .class-form input[type="text"],
   .class-form select,
   .class-form textarea {
       width: 100%;
       padding: 10px;
       font-size: 1rem;
       border: 1px solid #ccc;
       border-radius: 5px;
       box-sizing: border-box;
   }
   .class-form textarea {
       resize: vertical;
       min-height: 70px;
   }
   .btn {
       padding: 10px 20px;
       font-size: 1rem;
       color: white;
       background-color: #003366;
       border: none;
       border-radius: 5px;
       cursor: pointer;
       transition: background-color 0.3s;
   }
   .btn:hover {
       background-color: #0055a5;
   }
</style>
<div class="content-container">
   <h1>${sessionScope.currentUser.admin ? 'All Classes' : 'My Classes'}</h1>
   <table>
       <thead>
           <tr>
               <th>Class</th>
               <th>School</th>
               <th>Teacher</th>
               <th>Content</th>
               <th>Created</th>
           </tr>
       </thead>
       <tbody>
           <c:forEach var="classroom" items="${classes}">
               <tr>
                   <td><a href="${pageContext.request.contextPath}/classes/${classroom.id}"><c:out value="${classroom.name}" /></a></td>
                   <td><c:out value="${empty classroom.schoolName ? '-' : classroom.schoolName}" /></td>
                   <td><c:out value="${classroom.teacherName}" /></td>
                   <td>${classroom.contentCount}</td>
                   <td><fmt:formatDate value="${classroom.createdAt}" pattern="yyyy-MM-dd" /></td>
               </tr>
           </c:forEach>
           <c:if test="${empty classes}">
               <tr>
                   <td colspan="5">No classes yet. Create one below.</td>
               </tr>
           </c:if>
       </tbody>
   </table>

   <h2>Create a Class</h2>
   <form action="${pageContext.request.contextPath}/classes/save" method="post" class="class-form">
       <div class="form-group">
           <label for="name">Class Name:</label>
           <input type="text" name="name" id="name" value="${fn:escapeXml(name)}" placeholder="e.g. Grade 5 Science" required>
       </div>
       <div class="form-group">
           <label for="schoolName">School (optional):</label>
           <select name="schoolName" id="schoolName">
               <option value="">None</option>
               <c:forEach var="school" items="${schools}">
                   <option value="${fn:escapeXml(school.name)}" ${school.name == schoolName ? 'selected' : ''}><c:out value="${school.name}" /></option>
               </c:forEach>
           </select>
       </div>
       <div class="form-group">
           <label for="description">Description (optional):</label>
           <textarea name="description" id="description"><c:out value="${description}" /></textarea>
       </div>
       <input type="submit" value="Create Class" class="btn">
   </form>
</div>
<%@ include file="footer.jsp" %>
