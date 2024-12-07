<!-- contentLibrary.jsp -->
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for contentLibrary.jsp */
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
   ul.content-list {
       list-style: none;
       padding: 0;
   }
   ul.content-list li {
       background: #fff;
       padding: 10px 15px;
       margin-bottom: 10px;
       border-radius: 5px;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       font-size: 1rem;
       color: #333;
   }
</style>
<div class="content-container">
   <h1>Content Library</h1>
   <ul class="content-list">
       <c:forEach var="item" items="${contents}">
           <li>${item}</li>
       </c:forEach>
   </ul>
</div>
<%@ include file="footer.jsp" %>

