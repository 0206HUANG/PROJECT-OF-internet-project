<%-- contentView.jsp --%>
<%@ include file="header.jsp" %>
<style>
   /* Inline CSS for contentView.jsp */
   .view-container {
       max-width: 900px;
       margin: 20px auto;
       padding: 20px;
       background-color: #f9f9f9;
       border-radius: 8px;
       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
       font-family: Arial, sans-serif;
   }
   .view-container h1 {
       text-align: center;
       color: #003366;
       margin-bottom: 20px;
   }
   .view-container h2 {
       color: #003366;
       font-size: 1.2rem;
       margin: 25px 0 10px;
   }
   .video-frame {
       position: relative;
       padding-top: 56.25%; /* 16:9 */
       background-color: #000;
       border-radius: 8px;
       overflow: hidden;
   }
   .video-frame iframe {
       position: absolute;
       top: 0;
       left: 0;
       width: 100%;
       height: 100%;
       border: 0;
   }
   .media video,
   .media img {
       display: block;
       max-width: 100%;
       margin: 0 auto;
       border-radius: 8px;
   }
   .stats {
       display: flex;
       flex-wrap: wrap;
       gap: 15px;
       margin-top: 12px;
       color: #555;
       font-size: 0.95rem;
   }
   .description {
       margin: 20px 0;
       color: #333;
       white-space: pre-line;
   }
   table {
       width: 100%;
       border-collapse: collapse;
       background-color: #fff;
       box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
       border-radius: 8px;
       overflow: hidden;
       margin-bottom: 20px;
   }
   table th,
   table td {
       text-align: left;
       padding: 10px 15px;
       border-bottom: 1px solid #ddd;
       font-size: 0.9rem;
       color: #333;
   }
   table th {
       width: 150px;
       color: #003366;
   }
   table td a {
       color: #0055a5;
   }
   .class-form {
       display: flex;
       gap: 10px;
   }
   .class-form select {
       flex: 1;
       padding: 8px;
       border: 1px solid #ccc;
       border-radius: 5px;
   }
   .actions {
       display: flex;
       flex-wrap: wrap;
       gap: 10px;
       margin-top: 25px;
   }
   .btn {
       display: inline-block;
       padding: 10px 20px;
       background-color: #003366;
       color: white;
       text-decoration: none;
       border: none;
       border-radius: 5px;
       font-size: 1rem;
       cursor: pointer;
       transition: background-color 0.3s;
   }
   .btn:hover {
       background-color: #0055a5;
   }
   .btn.btn-danger {
       background-color: #c62828;
   }
   .btn.btn-danger:hover {
       background-color: #e53935;
   }
</style>
<div class="view-container">
   <h1><c:out value="${content.title}" /></h1>
   <div class="media">
       <c:choose>
           <c:when test="${content.youtube}">
               <div class="video-frame">
                   <iframe src="${content.youtubeEmbedUrl}" title="${fn:escapeXml(content.title)}"
                           allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                           referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
               </div>
           </c:when>
           <c:when test="${content.video}">
               <video src="${pageContext.request.contextPath}/content/file/${content.id}" controls></video>
           </c:when>
           <c:otherwise>
               <img src="${pageContext.request.contextPath}/content/file/${content.id}" alt="${fn:escapeXml(content.title)}">
           </c:otherwise>
       </c:choose>
   </div>

   <%-- Live numbers from the YouTube API (only when an API key is configured) --%>
   <c:if test="${not empty liveVideo}">
       <div class="stats">
           <c:if test="${not empty liveVideo.viewCountLabel}"><span>&#128065; ${liveVideo.viewCountLabel} views</span></c:if>
           <c:if test="${not empty liveVideo.likeCountLabel}"><span>&#128077; ${liveVideo.likeCountLabel} likes</span></c:if>
       </div>
   </c:if>

   <c:if test="${not empty content.description}">
       <p class="description"><c:out value="${content.description}" /></p>
   </c:if>

   <table>
       <tr><th>Type</th><td>${content.typeLabel}</td></tr>
       <c:if test="${content.youtube}">
           <c:set var="video" value="${content.youtubeVideo}" />
           <c:if test="${not empty video.channelTitle}">
               <tr><th>Channel</th><td><c:out value="${video.channelTitle}" /></td></tr>
           </c:if>
           <c:if test="${not empty video.durationLabel}">
               <tr><th>Duration</th><td>${video.durationLabel}</td></tr>
           </c:if>
           <c:if test="${not empty video.publishedAt}">
               <tr><th>Published</th><td><fmt:formatDate value="${video.publishedAt}" pattern="yyyy-MM-dd" /></td></tr>
           </c:if>
           <tr><th>YouTube</th><td><a href="${content.youtubeWatchUrl}" target="_blank" rel="noopener">Watch on YouTube</a></td></tr>
       </c:if>
       <c:if test="${not content.youtube}">
           <tr>
               <th>File</th>
               <td>
                   <a href="${pageContext.request.contextPath}/content/file/${content.id}" target="_blank"><c:out value="${content.originalFilename}" /></a>
                   (<c:out value="${content.sizeLabel}" />)
               </td>
           </tr>
       </c:if>
       <tr><th>Added by</th><td><c:out value="${content.uploaderName}" /></td></tr>
       <tr><th>Added at</th><td><fmt:formatDate value="${content.uploadedAt}" pattern="yyyy-MM-dd HH:mm" /></td></tr>
       <tr>
           <th>Used in classes</th>
           <td>
               <c:forEach var="classroom" items="${inClasses}" varStatus="loop">
                   <c:out value="${classroom.name}" /><c:if test="${not loop.last}">, </c:if>
               </c:forEach>
               <c:if test="${empty inClasses}">Not used in any class yet</c:if>
           </td>
       </tr>
   </table>

   <c:if test="${not empty manageableClasses}">
       <h2>Add to a Class</h2>
       <form action="${pageContext.request.contextPath}/content/${content.id}/class" method="post" class="class-form">
           <select name="classId" required>
               <c:forEach var="classroom" items="${manageableClasses}">
                   <option value="${classroom.id}"><c:out value="${classroom.name}" /></option>
               </c:forEach>
           </select>
           <input type="submit" value="Add to Class" class="btn">
       </form>
   </c:if>

   <div class="actions">
       <a href="${pageContext.request.contextPath}/contentLibrary" class="btn">Back to Content Library</a>
       <c:if test="${canManage}">
           <a href="${pageContext.request.contextPath}/content/edit/${content.id}" class="btn">Edit</a>
           <form action="${pageContext.request.contextPath}/content/delete/${content.id}" method="post"
                 onsubmit="return confirm('Delete this item from the library? It will also be removed from all classes.');">
               <input type="submit" value="Delete" class="btn btn-danger">
           </form>
       </c:if>
   </div>
</div>
<%@ include file="footer.jsp" %>
