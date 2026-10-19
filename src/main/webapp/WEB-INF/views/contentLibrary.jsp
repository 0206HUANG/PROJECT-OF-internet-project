<%-- contentLibrary.jsp --%>
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
   .btn-youtube {
       background-color: #cc0000;
       margin-left: 10px;
   }
   .btn-youtube:hover {
       background-color: #e62117;
   }
   .filter-form {
       display: flex;
       flex-wrap: wrap;
       gap: 10px;
       margin-bottom: 10px;
   }
   .filter-form input[type="text"] {
       flex: 1;
       min-width: 200px;
   }
   .filter-form input[type="text"],
   .filter-form select {
       padding: 8px;
       font-size: 1rem;
       border: 1px solid #ccc;
       border-radius: 5px;
   }
   .thumb {
       display: block;
       width: 120px;
       margin-bottom: 6px;
       border-radius: 4px;
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
       vertical-align: top;
   }
   table tbody tr:hover {
       background-color: #f1f1f1;
   }
   table tbody td a {
       color: #0055a5;
       text-decoration: none;
   }
</style>
<div class="content-container">
   <h1>Content Library</h1>
   <a href="${pageContext.request.contextPath}/content/upload" class="btn">Upload New Content</a>
   <a href="${pageContext.request.contextPath}/youtube" class="btn btn-youtube">Add YouTube Video</a>
   <form action="${pageContext.request.contextPath}/contentLibrary" method="get" class="filter-form">
       <input type="text" name="q" value="${fn:escapeXml(query)}" placeholder="Search by title or description">
       <select name="type">
           <option value="">All types</option>
           <option value="upload" ${type == 'upload' ? 'selected' : ''}>Uploaded files</option>
           <option value="youtube" ${type == 'youtube' ? 'selected' : ''}>YouTube videos</option>
       </select>
       <input type="submit" value="Search" class="btn-small">
   </form>
   <table>
       <thead>
           <tr>
               <th>Title</th>
               <th>Description</th>
               <th>Type</th>
               <th>Size / Length</th>
               <th>Uploaded By</th>
               <th>Uploaded At</th>
               <th></th>
           </tr>
       </thead>
       <tbody>
           <c:forEach var="item" items="${contents}">
               <tr>
                   <td>
                       <c:if test="${item.youtube}">
                           <img src="${item.youtubeThumbnailUrl}" alt="" class="thumb">
                       </c:if>
                       <c:out value="${item.title}" />
                   </td>
                   <td><c:out value="${item.description}" /></td>
                   <td>${item.typeLabel}</td>
                   <td>${item.youtube and not empty item.youtubeVideo.durationLabel ? item.youtubeVideo.durationLabel : item.sizeLabel}</td>
                   <td><c:out value="${item.uploaderName}" /></td>
                   <td><fmt:formatDate value="${item.uploadedAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                   <td><a href="${pageContext.request.contextPath}/content/view/${item.id}">View</a></td>
               </tr>
           </c:forEach>
           <c:if test="${empty contents}">
               <tr>
                   <td colspan="7">${empty query and empty type ? 'No content has been added yet.' : 'No content matches your search.'}</td>
               </tr>
           </c:if>
       </tbody>
   </table>
</div>
<%@ include file="footer.jsp" %>
